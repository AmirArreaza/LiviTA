package se.kry.codetest.DTO;

import java.util.Date;

public class Service {

  private String name;

  private String url;

  private Date createdAt;

  public Service(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public Service() {
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }
}
