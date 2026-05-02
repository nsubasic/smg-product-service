package com.smg.productservice;

import com.smg.productservice.domain.ProductRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTest {

    private static final String PRODUCT_NAME = "Integration Product";
    private static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(12345.67);
    private static final String PRODUCT_CREATED_TOPIC = "product.created";
    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String DATABASE_NAME = "productdb";
    private static final String DATABASE_USERNAME = "productuser";
    private static final String DATABASE_PASSWORD = "productpass";
    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka:4.1.2")
    );

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer(POSTGRES_IMAGE)
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void createProduct_persistsProductAndPublishesEvent() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "price": %s
                                }
                                """.formatted(PRODUCT_NAME, PRODUCT_PRICE)))
                .andExpect(status().isCreated());

        var products = productRepository.findAll();

        assertThat(products).hasSize(1);

        var product = products.get(0);

        assertThat(product.getName()).isEqualTo(PRODUCT_NAME);
        assertThat(product.getPrice()).isEqualByComparingTo(PRODUCT_PRICE);

        ConsumerRecord<String, String> record = consumeMessage();

        String value = record.value();

        assertThat(value).contains(PRODUCT_NAME);
        assertThat(value).contains(PRODUCT_PRICE.toString());
    }

    private ConsumerRecord<String, String> consumeMessage() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(ProductIntegrationTest.PRODUCT_CREATED_TOPIC));

            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofSeconds(5));

            if (records.isEmpty()) {
                throw new AssertionError("No Kafka message received from topic: " + ProductIntegrationTest.PRODUCT_CREATED_TOPIC);
            }

            return records.iterator().next();
        }
    }
}