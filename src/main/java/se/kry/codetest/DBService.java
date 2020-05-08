package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import se.kry.codetest.DTO.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBService extends AbstractVerticle {

  private DBConnector connector;
  private List<Service> servicesList;
  private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);

  private final Pattern urlPattern
          = Pattern.compile("^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?" +
          "[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$");

  public DBService(DBConnector connector) {
    this.connector = connector;
    this.servicesList = new ArrayList<>();
  }

  private Service serviceDataMapper(JsonObject object) {
    Service newService = new Service();
    newService.setName(object.getString("name"));
    newService.setUrl(object.getString("url"));
    newService.setCreatedAt(handleDate(object.getString("created_at"), formatter));
    return newService;
  }

  private Date handleDate(String dateString, SimpleDateFormat formatter) {
    try {
      return formatter.parse(dateString);
    } catch (Exception ex) {
      System.out.println("Error: " + ex.getMessage());
      return new Date();
    }
  }

  public void addServices(HashMap<String, String> servicesStatus) {
    servicesList.stream().forEach(s -> {
      System.out.println("Service " + s.getName() + " (" + s.getUrl() + ")");
      servicesStatus.put(s.getUrl(), "UNKNOWN");
    });
  }

  public Future<Integer> insertService(String name, String url) throws InterruptedException {

    Matcher matcher = urlPattern.matcher(url);
    if(!matcher.matches()) return Future.failedFuture("Invalid URL");

    System.out.println("URL " + url + " is valid!");

    Future<Integer> futureResult = connector.insertService(name, url);

    return futureResult;
  }

  public void getAllServices() {
    String sql = "SELECT * FROM service;";

    connector.query(sql).setHandler(fr -> {
      if (fr.succeeded()) {
        System.out.println("Success Rows: " + fr.result().getRows().size());
        ResultSet result = fr.result();
        for (JsonObject object : result.getRows()) {
          servicesList.add(serviceDataMapper(object));
        }
      } else {
        System.out.println(fr.cause().getMessage());
      }
    });
  }

  public Future<Integer> deleteService(String url) {
    Future<Integer> futureResult = connector.deleteService(url);

    return futureResult;
  }

  public static String removePrefixFromUrl(String url){
    url = url.toLowerCase();
    url = url.replace("http://", "");
    url = url.replace("https://", "");
    return url;
  }
}
