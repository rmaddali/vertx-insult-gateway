package io.vertx.starter;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private  Vertx vertx;

  protected static final String TEST_DB_URL = "jdbc:hsqldb:mem:testdb;db_close_delay=-1";
  protected static final String TEST_DB_USER = "sa";
  protected static final String TEST_DB_PASSWORD = "sa";
  int port;

  @Before
  public  void setUp(TestContext tc) throws Exception {
    vertx = Vertx.vertx();


    vertx.deployVerticle(InsultGatewayVerticle.class.getName(), tc.asyncAssertSuccess());



  }


  @Test
  public void testThatTheServerIsStartedAndReturnsAnAdjective(TestContext tc) {
    Async async = tc.async();
    HttpClient httpClient = vertx.createHttpClient();
    httpClient.getNow(8080, "localhost", "/api/insult", response -> {
      tc.assertEquals(response.statusCode(), 200);
      response.bodyHandler(body -> {
        tc.assertTrue(body.length() > 0);
        httpClient.close();
async.complete();

      });

    });


    async.awaitSuccess();
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());



  }



}
