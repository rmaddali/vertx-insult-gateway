package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

public class DbVerticle extends AbstractVerticle {

  protected static final String PERSIST_INSULT_ADDRESS = "ping-address";

  protected static final String DB_URL = "jdbc:hsqldb:mem:testdb;shutdown=true";

  protected static final String DB_DRIVER = "org.hsqldb.jdbcDriver";

  private JDBCClient jdbcClient;

  @Override
  public void start(Future<Void> future) {

    jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", DB_URL)
      .put("driver_class", DB_DRIVER)
      .put("max_pool_size", 30));


    EventBus eventBus = vertx.eventBus();

    MessageConsumer<JsonObject> consumer = eventBus.consumer(PERSIST_INSULT_ADDRESS);
    consumer.handler(message -> {

      System.out.println("I have received a message: " + message.toString());

      JsonObject js = message.body();
      String action = js.getString("action");

      switch(action) {
        case "persist":
          persistInsult(message);
          break;
        default:
          message.fail( FailureCode.BAD_ACTION.ordinal(), FailureCode.BAD_ACTION.failureCodeMessage + message.body());
      }
    });

    future.complete();

  }

  private void persistInsult(Message<JsonObject> message) {

    jdbcClient.updateWithParams("insert into INSULTS(body) VALUES (?);", new JsonArray().add(message.body().getString("insult")), res ->{
      if (res.succeeded()) {
        message.reply("success");
      }else {
        message.fail(FailureCode.DB_ERROR.ordinal(),FailureCode.DB_ERROR.failureCodeMessage + res.cause().getMessage());
      }
    });
  }

}
