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
    connector.query("CREATE TABLE IF NOT EXISTS service (url VARCHAR(128) NOT NULL)");
    service = new DBService(connector);

  }

  @Test
  @DisplayName("Insert a new service into the DB")
  void insert_service(VertxTestContext testContext) throws InterruptedException {

    service.insertService("Test.com").setHandler(result -> {
      assertTrue(result.succeeded());
      assertEquals(new Integer(1), result.result());
    });
    service.insertService("Test2.com").setHandler(result -> {
      assertTrue(result.succeeded());
      assertEquals(new Integer(1), result.result());
    });
    service.insertService("Test3.com").setHandler(result -> {
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
}