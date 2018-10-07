package io.vertx.starter;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class InsultAPITest {
  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(InsultGatewayVerticle.class.getName(), tc.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  public void testGetAllInsults(TestContext context) {
    final Async async = context.async();

    vertx.createHttpClient().getNow(8080, "localhost", "/api/insults",
      response -> {
        response.handler(body -> {
          context.assertEquals(200, response.statusCode());
          context.assertTrue(body.toString().contains("Congrats "));
          async.complete();
        });
      });
  }
}
