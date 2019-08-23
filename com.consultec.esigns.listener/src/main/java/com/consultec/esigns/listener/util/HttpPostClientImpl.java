/**
 * 
 */
package com.consultec.esigns.listener.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import com.consultec.esigns.core.io.IPostHttpClient;

/**
 * @author hrodriguez
 *
 */
public class HttpPostClientImpl implements IPostHttpClient {

  private String uri;

  private HttpEntityEnclosingRequestBase request;

  private CloseableHttpClient httpclient;

  public HttpPostClientImpl(String path) throws Exception {

    this.uri = path;
    this.request = new HttpPost(this.uri);

    buildHttpClient();

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

  private void buildHttpClient() throws KeyManagementException, NoSuchAlgorithmException,
      KeyStoreException, CertificateException, IOException {

    KeyStore trustStore = KeyStore.getInstance("Windows-ROOT");
    trustStore.load(null, null);

    /*
     * Use the TrustSelfSignedStrategy to allow Self Signed Certificates.
     */
    SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(trustStore, null).build();

    /*
     * Create an SSL Socket Factory to use the SSLContext with the trust self signed certificate
     * strategy and allow all hosts verifier.
     */
    SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext);

    /*
     * Finally create the HttpClient using HttpClient factory methods and assign the ssl socket
     * factory.
     */
    this.httpclient = HttpClients.custom().setSSLSocketFactory(connectionFactory).build();

  }

}
