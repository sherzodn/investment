package com.xm.crypto.investment.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AbstractIntegrationTest {
  static PostgreSQLContainer<?> postgreSQLContainer =
    new PostgreSQLContainer<>("postgres:14-alpine");
  static GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
    .withExposedPorts(6379);

  @DynamicPropertySource
  public static void properties(DynamicPropertyRegistry registry) {
    Startables.deepStart(postgreSQLContainer, redis).join();
    registry.add("spring.redis.host", redis::getHost);
    registry.add("spring.redis.port", redis::getFirstMappedPort);
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
  }

  @BeforeAll
  public static void setUp() {
    postgreSQLContainer.withReuse(true);
    postgreSQLContainer.start();
  }

  @AfterAll
  public static void tearDown() {
    postgreSQLContainer.stop();
    redis.stop();
  }

}
