package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private HashMap<String, String> services = new HashMap<>();
  //TODO use this
  private DBConnector connector;
  private DBService service;
  private BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    service = new DBService(connector);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    service.getAllServices();

    setRoutes(router);
    vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(8080, result -> {
              if (result.succeeded()) {
                System.out.println("KRY code test service started");
                startFuture.complete();
                service.addServices(services);
              } else {
                startFuture.fail(result.cause());
              }
            });
  }

  private void setRoutes(Router router) {
    router.route("/*").handler(StaticHandler.create());
    router.get("/service").handler(req -> {

      poller.pollServices(service, services);

      List<JsonObject> jsonServices = services
              .entrySet()
              .stream()
              .map(service ->
                      new JsonObject()
                              .put("name", service.getKey())
                              .put("status", service.getValue()))
              .collect(Collectors.toList());
      req.response()
              .putHeader("content-type", "application/json")
              .end(new JsonArray(jsonServices).encode());
    });
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      try {
        service.insertService((jsonBody.getString("url"))).setHandler(result -> {
          if (result.succeeded()) {
            System.out.println("Rows added: " + result.result());
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("OK");
            services.put(jsonBody.getString("url"), "UNKNOWN");
          } else {
            System.out.println(result.cause().getMessage());
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("FAILED");
          }
        });
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
        req.response()
                .putHeader("content-type", "text/plain")
                .end("FAILED");
      }
    });
  }

}



