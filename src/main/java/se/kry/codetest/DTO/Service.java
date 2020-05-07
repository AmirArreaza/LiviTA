package se.kry.codetest.DTO;

import io.vertx.core.json.JsonObject;

public class Service {

  private String url;

  public Service(String url){
    this.url = url;
  }

  public Service(JsonObject json) {
    this.url = json.getString("url");
  }


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
