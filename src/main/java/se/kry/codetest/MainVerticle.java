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

  private HashMap<String, String> servicesStatus = new HashMap<>();

  private BackgroundPoller poller;
  private DBConnector connector;
  private DBService service;

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    service = new DBService(connector);
    poller = new BackgroundPoller(vertx);

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
                service.addServices(servicesStatus);
                poller.pollServices(servicesStatus);
              } else {
                startFuture.fail(result.cause());
              }
            });
  }

  private void setRoutes(Router router) {
    router.route("/*").handler(StaticHandler.create());
    router.get("/services").handler(req -> {
      List<JsonObject> jsonServices = servicesStatus
              .entrySet()
              .stream()
              .map(service ->
                      new JsonObject()
                              .put("name", service.getKey())
                              .put("status", service.getValue()))
              .collect(Collectors.toList());
      poller.pollServices(servicesStatus);
      req.response()
              .putHeader("content-type", "application/json")
              .end(new JsonArray(jsonServices).encode());
    });
    router.post("/services").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();
      try {
        String url = jsonBody.getString("url");
        String name = jsonBody.getString("name");

        final String cleanedUrl = DBService.removePrefixFromUrl(url);
        service.insertService(name, cleanedUrl).setHandler(result -> {
          if (result.succeeded()) {
            System.out.println("Rows added: " + result.result());
            String serviceURL = jsonBody.getString("url");
            servicesStatus.put(cleanedUrl, "UNKNOWN");
            poller.pollService(cleanedUrl, servicesStatus).setHandler(done ->{
              System.out.println("Completed process to poll service " + serviceURL);
              req.response()
                      .putHeader("content-type", "text/plain")
                      .end("OK");
            });
          } else {
            System.out.println(result.cause().getMessage());
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end(result.cause().getMessage());
          }
        });
      } catch (Exception ex) {
        System.out.println(ex.getMessage());
        req.response()
                .putHeader("content-type", "text/plain")
                .end("FAILED");
      }
    });
    router.delete("/services/:url").handler(req -> {
      String url = req.request().getParam("url");
      try{
        service.deleteService(url).setHandler(result -> {
          if(result.succeeded()){
            req.response()
                    .putHeader("content-type", "text/plain")
                    .setStatusCode(200)
                    .end("OK");
            servicesStatus.remove(url);
          }else{
            System.out.println(result.cause().getMessage());
            req.response()
                    .putHeader("content-type", "text/plain")
                    .setStatusCode(500)
                    .end("FAILED");
          }
        });
      }catch (Exception ex){
        System.out.println(ex.getMessage());
        req.response()
                .putHeader("content-type", "text/plain")
                .setStatusCode(500)
                .end("FAILED");
      }
    });
  }
}



