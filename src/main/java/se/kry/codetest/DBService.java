package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import se.kry.codetest.DTO.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DBService extends AbstractVerticle {

  private DBConnector connector;

  private List<Service> servicesList;

  public DBService(DBConnector connector) {
    this.connector = connector;
    this.servicesList = new ArrayList<>();
  }

  public Future<Integer> insertService(String url) throws InterruptedException {
    Future<Integer> futureResult = connector.insertService(url);

    return futureResult;
  }

  public void getAllServices() {
    String sql = "SELECT * FROM service;";

    connector.query(sql).setHandler(fr -> {
      if(fr.succeeded()){
        System.out.println("Success Rows: " + fr.result().getRows().size());
        servicesList = fr.result().getRows().stream().map(Service::new).collect(Collectors.toList());
      }
      else{
        System.out.println(fr.cause().getMessage());
      }
    });
  }

  public void addServices(HashMap<String, String> services){
   servicesList.stream().forEach(s -> services.put(s.getUrl(), "UNKNOWN"));
  }

}
