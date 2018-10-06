package io.vertx.starter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import static io.vertx.starter.DbVerticle.PERSIST_INSULT_ADDRESS;

@RunWith(VertxUnitRunner.class)
public class DbVerticleTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(DbVerticle.class.getName(), tc.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void testThatTheEventBusIsStarted(TestContext tc) {

    Async async = tc.async();

    vertx.eventBus().send(PERSIST_INSULT_ADDRESS, "ping!", ar -> {
      if (ar.succeeded()) {
        System.out.println("Received: " + ar.result().body());
        tc.assertEquals("pong!", ar.result().body());
        async.complete();
      }else{
        System.out.println("Sending failed: " + ar.result().body());
        tc.fail(ar.cause());
      }
    });

  }

}
