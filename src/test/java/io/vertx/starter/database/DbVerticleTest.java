package io.vertx.starter.database;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.starter.database.DbProps.*;
import static junit.framework.TestCase.assertEquals;

@RunWith(VertxUnitRunner.class)
public class DbVerticleTest {

  private Vertx vertx;
  protected static final String TEST_DB_URL = "jdbc:hsqldb:mem:testdb;DB_CLOSE_DELAY=-1";
  protected static final String TEST_DB_DRIVER = "org.hsqldb.jdbcDriver";
  protected static final String TEST_DB_USER = DB_USER;
  protected static final String TEST_DB_PASSWORD = DB_PASSWORD;
  private JDBCClient jdbcClient;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();

    // We will use this to verify that our inserts worked
    jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", TEST_DB_URL)
      .put("driver_class", DB_DRIVER)
      .put("max_pool_size", 30));

    Flyway flyway = Flyway.configure().dataSource(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD).load();
    flyway.migrate();

    JsonObject config = new JsonObject()
      .put(CONFIG_DB_URL, TEST_DB_URL)
      .put(CONFIG_DB_DRIVER, TEST_DB_DRIVER);

    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle(DbVerticle.class.getName(), options, tc.asyncAssertSuccess());

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
        jdbcClient.query(QUERY_ALL_INSULTS, res ->{
          assertEquals(4, res.result().getNumRows());
        });
      }else{
        tc.fail(ar.cause());
      }
      async.complete();
      async2.complete();
    });

  }

}
