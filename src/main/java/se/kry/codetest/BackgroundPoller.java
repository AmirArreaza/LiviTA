package se.kry.codetest;

import io.vertx.core.Future;

import java.util.Map;

public class BackgroundPoller {



  public Future<Void> pollServices(DBService service, Map<String, String> servicesMap) {
    /*List<Service> servicesList = service.getAllServices();
    servicesList.stream().forEach(s -> servicesMap.put(s.getUrl(), "UNKNOWN"));

*/
    //TODO

     return Future.succeededFuture();
  }

}
