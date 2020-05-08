package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestDBConnector {

  DBConnector connector;
  DBService service;

  @BeforeEach
  void load_test_database(Vertx vertx) {
    connector = new DBConnector(vertx, "poller.test.db");
    connector.query("CREATE TABLE IF NOT EXISTS service (name VARCHAR(128), url VARCHAR(128), created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
    service = new DBService(connector);
  }

  @Test
  @DisplayName("Insert a new service into the DB")
  void insert_service(VertxTestContext testContext) throws InterruptedException {

    service.insertService("Test Page 1", "dev.co").setHandler(result -> {
      assertTrue(result.succeeded());
      assertEquals(new Integer(1), result.result());
    });
    service.insertService("Test Page 2", "https://www.google.com").setHandler(result -> {
      assertTrue(result.succeeded());
      assertEquals(new Integer(1), result.result());
    });
    service.insertService("Test Page 3", "http://www.google.com").setHandler(result -> {
      assertTrue(result.succeeded());
      assertEquals(new Integer(1), result.result());
      testContext.completeNow();
    });

    assertTrue(testContext.awaitCompletion(1, TimeUnit.SECONDS));

  }

  @Test
  @DisplayName("Get all services")
  void get_all_services(Vertx vertx, VertxTestContext testContext){

    HashMap<String, String> map = new HashMap<>();
    service.getAllServices();

    vertx.setPeriodic(500 * 1, timeId -> {
      service.addServices(map);
      assertTrue(map.size() > 0);
      testContext.completeNow();
    });

  }

  @Test
  @DisplayName("Remove a service")
  void delete_service_by_url(VertxTestContext testContext) throws InterruptedException {
    service.insertService("Test Page X", "testx.com").setHandler(result -> {
      assertTrue(result.succeeded());
      assertEquals(new Integer(1), result.result());
    });

    service.deleteService("Test Page X").setHandler(result -> {
      assertTrue(result.succeeded());
      assertTrue(result.result() > 0);
      testContext.completeNow();
    });

    assertTrue(testContext.awaitCompletion(1, TimeUnit.SECONDS));
  }

}