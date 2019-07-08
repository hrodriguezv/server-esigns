/**
 * 
 */
package com.consultec.esigns.listener.util;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.consultec.esigns.core.io.IPostHttpClient;

/**
 * @author hrodriguez
 *
 */
public class HttpPostClientImpl implements IPostHttpClient {


  private String uri;

  private HttpEntityEnclosingRequestBase request;

  private CloseableHttpClient httpclient;

  public HttpPostClientImpl(String path) {

    this.uri = path;
    this.request = new HttpPost(this.uri);
    this.httpclient = HttpClients.createDefault();

  }

  @Override
  public void setEntity(HttpEntity entity) {
    this.request.setEntity(entity);
  }

  @Override
  public void setCookie(String cookie) {
    request.addHeader("Cookie", cookie);
  }

  @Override
  public void fillHeader(Map<String, List<String>> header) {
    header.keySet().stream().forEach(k -> request.addHeader(k, header.get(k).get(0)));
    request.removeHeaders("content-length"); 
  }

  @Override
  public CloseableHttpResponse execute() throws Exception {
    return httpclient.execute(request);
  }
}
