package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.UpdateResult;

public class DBConnector {

  private final String DB_PATH = "poller.db";
  private final SQLClient client;
  private Vertx vertx;

  public DBConnector(Vertx vertx) {
    JsonObject config = new JsonObject()
            .put("url", "jdbc:sqlite:" + DB_PATH)
            .put("driver_class", "org.sqlite.JDBC")
            .put("max_pool_size", 30);

    client = JDBCClient.createShared(vertx, config);
    this.vertx = vertx;
  }

  public DBConnector(Vertx vertx, String database) {
    JsonObject config = new JsonObject()
            .put("url", "jdbc:sqlite:" + database)
            .put("driver_class", "org.sqlite.JDBC")
            .put("max_pool_size", 30);

    client = JDBCClient.createShared(vertx, config);
  }

  public Future<ResultSet> query(String query) {
    return query(query, new JsonArray());
  }

  public Future<ResultSet> query(String query, JsonArray params) {
    if (query == null || query.isEmpty()) {
      return Future.failedFuture("Query is null or empty");
    }
    if (!query.endsWith(";")) {
      query = query + ";";
    }

    Future<ResultSet> queryResultFuture = Future.future();

    client.queryWithParams(query, params, result -> {
      if (result.failed()) {
        queryResultFuture.fail(result.cause());
      } else {
        queryResultFuture.complete(result.result());
      }
    });

    return queryResultFuture;
  }

  public Future<Integer> insertService(String name, String url) {
    String sql = "INSERT INTO service (name, url) VALUES (?,?)";
    Future<Integer> future = Future.future();

    client.updateWithParams(sql, new JsonArray().add(name).add(url), result -> {
      if (result.failed()) {
        System.out.println("failed");
        future.fail(result.cause());
      }
      UpdateResult updateResult = result.result();
      System.out.println("Updated no. of rows: " + updateResult.getUpdated());
      future.complete(updateResult.getUpdated());
    });

    return future;
  }

  public Future<Integer> deleteService(String name) {
    String sql = "DELETE FROM service WHERE name=?;";

    Future<Integer> future = Future.future();

    client.updateWithParams(sql, new JsonArray().add(name), result -> {
      if (result.failed()) {
        System.out.println("failed");
        future.fail(result.cause());
      }
      UpdateResult updateResult = result.result();
      System.out.println("Updated no. of rows: " + updateResult.getUpdated());
      future.complete(updateResult.getUpdated());
    });
    return future;
  }


}
