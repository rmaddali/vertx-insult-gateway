package io.vertx.starter.database;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.starter.database.DbProps.PERSIST_INSULT_ADDRESS;
import static io.vertx.starter.database.DbProps.QUERY_ALL_INSULTS;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(VertxUnitRunner.class)
public class DbVerticleTest {

  private Vertx vertx;
  protected static final String TEST_DB_URL = "jdbc:hsqldb:mem:testdb;shutdown=true";
  protected static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
  private JDBCClient jdbcClient;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();

    JsonObject config = new JsonObject().put("test", "config success!");
    DeploymentOptions options = new DeploymentOptions().setConfig(config);
    jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", TEST_DB_URL)
      .put("driver_class", DB_DRIVER)
      .put("max_pool_size", 30));

    vertx.deployVerticle(DbVerticle.class.getName(), options, tc.asyncAssertSuccess());

    Async async = tc.async();

    // Create our in-memory database
    jdbcClient.getConnection(ar -> {

      assertTrue(ar.succeeded());

      SQLConnection connection = ar.result();

      connection.execute("create table if not exists INSULTS (ID INT IDENTITY PRIMARY KEY, BODY VARCHAR(255) NOT NULL)", res -> {
        connection.close();
        assertTrue(res.succeeded());
        async.complete();
      });
    });
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void testMessageRoundTrip(TestContext tc) {

    Async async = tc.async();
    Async async2 = tc.async();

    JsonObject message = new JsonObject()
      .put("action", "persist")
      .put("insult", "Congrats, spongy strumpet!");

    vertx.eventBus().send(PERSIST_INSULT_ADDRESS, message, ar -> {
      if (ar.succeeded()) {
        tc.assertEquals("success", ar.result().body());
        async.complete();

        jdbcClient.query(QUERY_ALL_INSULTS, res ->{
          assertEquals(1, res.result().getNumRows());
          async2.complete();
        });
      }else{
        tc.fail(ar.cause());
      }
    });

  }

}
