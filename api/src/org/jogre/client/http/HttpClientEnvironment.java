package org.jogre.client.http;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Environment for http-based communication
 * 
 * @author Jens Scheffler
 *
 */
public class HttpClientEnvironment implements HttpClientMessageBus.Environment {
  
  private final HttpClient client = new HttpClient();
  private final String url;
  
  public HttpClientEnvironment(String url) {
    this.url = url;
  }

  @Override
  public long currentTimeMillis() {
    return System.currentTimeMillis();
  }

  @Override
  public void execute(final HttpClientMessageBus bus) {
    new Thread(new Runnable(){
      @Override
      public void run() {
        try {
          bus.run();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }}).start();
  }

  @Override
  public void sleep(long millis)
      throws InterruptedException {
    if (millis > 0) {
      Thread.sleep(millis);
    }
  }

  @Override
  public String fetch(String data) {
    PostMethod method = new PostMethod(url);
    method.setRequestBody(data);
    try {
      int returnCode = client.executeMethod(method);
      if(returnCode == HttpStatus.SC_OK) {
        return method.getResponseBodyAsString();
      }
    } catch (HttpException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      method.releaseConnection();
    }
    return null;
  }

}
