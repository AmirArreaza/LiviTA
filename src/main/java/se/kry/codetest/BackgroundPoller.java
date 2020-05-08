package se.kry.codetest;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackgroundPoller {

  private Vertx vertx;
  private WebClient client;

  public BackgroundPoller(Vertx vertx){
    this.vertx = vertx;
    this.client = WebClient.create(vertx);
  }

  public Future<Void> pollServices(Map<String, String> servicesMap) {
    HashMap<String, Future> requests = new HashMap<>();

    servicesMap.entrySet().stream().forEach(s -> {
      Future<Void> future = Future.future();
      requests.put(s.getKey(), future);
      System.out.println("Reaching -> " + s.getKey());
      client
              .get(80, s.getKey(), "/")
              .send(ar -> {
                System.out.println(s.getKey() + " request completed!");
                if(ar.succeeded()){
                  System.out.println("Status Code: " + ar.result().statusCode());
                  s.setValue("OK");
                }else{
                  s.setValue("FAIL");
                  System.out.println(ar.cause());
                }
                requests.get(s.getKey()).complete();
              });
    });

    Future<Void> pollerResult = Future.future();
    List<Future> pollerFutures = new ArrayList<Future>(requests.values());
    CompositeFuture.all(pollerFutures).setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("All calls completed");
        pollerResult.complete();
      } else {
        System.out.println(done.cause().getMessage());
        pollerResult.fail(done.cause());
      }
    });

    return pollerResult;
  }

  public Future<Void> pollService(String url, HashMap<String, String> map) {
    Future<Void> future = Future.future();
    client
            .get(80, url, "/")
            .send(ar -> {
              System.out.println(url + " request completed!");
              if(ar.succeeded()){
                System.out.println("Status Code: " + ar.result().statusCode());
                map.put(url, "OK");
              }else{
                map.put(url, "FAIL");
                System.out.println(ar.cause());
              }
              future.complete();
            });

    return future;
  }


}
