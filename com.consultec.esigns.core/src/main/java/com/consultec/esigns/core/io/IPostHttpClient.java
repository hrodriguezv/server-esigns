package com.consultec.esigns.core.io;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

public interface IPostHttpClient {

  public void setEntity(HttpEntity entity);
  
  public void setCookie(String cookie);
  
  public void fillHeader(Map<String, List<String>> header);
  
  public CloseableHttpResponse execute() throws Exception;
  
}
