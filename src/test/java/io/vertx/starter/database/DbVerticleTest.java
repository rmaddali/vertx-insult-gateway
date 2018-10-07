package io.vertx.starter.database;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
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
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;

@RunWith(VertxUnitRunner.class)
public class DbVerticleTest {

  private Vertx vertx;

  protected static final String TEST_DB_URL = "jdbc:hsqldb:mem:testdb;db_close_delay=-1";
  protected static final String TEST_DB_DRIVER = DB_DRIVER;
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

    // Create our table and insert 3 rows
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
  public void testPersistMessage(TestContext tc) {

    Async async = tc.async();
    Async async2 = tc.async();

    JsonObject message = new JsonObject()
      .put("action", "persist")
      .put("insult", "Congrats, spongy strumpet!");

    vertx.eventBus().send(INSULTS_ADDRESS, message, ar -> {
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

  @Test
  public void testRetrieveMessage(TestContext testContext) {
    Async async = testContext.async();

    JsonObject message = new JsonObject()
      .put("action", "retrieve");

    vertx.eventBus().send(INSULTS_ADDRESS, message, ar -> {
      if (ar.failed()) {
        System.out.println(ar.cause());
      }
      assertTrue(ar.succeeded());

      JsonObject reply = (JsonObject) ar.result().body();
      assertNotNull(reply);

      JsonArray insults = reply.getJsonArray("insults");
      assertNotNull(insults);
      assertEquals(3, insults.size());
      assertEquals("infectious devil-monk", insults.getString(0));

      async.complete();
    });

  }

}
