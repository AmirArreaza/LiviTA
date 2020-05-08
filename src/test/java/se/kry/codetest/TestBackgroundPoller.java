package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestBackgroundPoller {

  @Test
  @DisplayName("Poll a list of services")
  public void poll_services(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    BackgroundPoller poller = new BackgroundPoller(vertx);

    HashMap<String, String> map = new HashMap<>();

    map.put("Google.com","UNKNOWN");
    map.put("Facebook.com","UNKNOWN");
    map.put("LoasadasLocasdsadadadados.com","UNKNOWN");
    map.put("Amazon.com","UNKNOWN");
    map.put("Kry.se","UNKNOWN");

    poller.pollServices(map).setHandler(result -> {
      assertTrue(result.succeeded());
      map.entrySet().stream().forEach(s -> {
        System.out.println(s.getKey() + ":" + s.getValue());
      });
      testContext.completeNow();
    });
    assertTrue(testContext.awaitCompletion(15, TimeUnit.SECONDS));
  }

  @Test
  @DisplayName("Poll for a single service")
  public void poll_service(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    BackgroundPoller poller = new BackgroundPoller(vertx);

    HashMap<String, String> map = new HashMap<>();

    map.put("Google.com","UNKNOWN");

    poller.pollService("Google.com", map).setHandler(done -> {
      assertTrue(done.succeeded());
      testContext.completeNow();
    });
    assertTrue(testContext.awaitCompletion(15, TimeUnit.SECONDS));
  }
}
