package io.vertx.starter.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import static io.vertx.starter.database.DbProps.CONFIG_DB_DRIVER;
import static io.vertx.starter.database.DbProps.CONFIG_DB_URL;
import static io.vertx.starter.database.DbProps.DB_URL;
import static io.vertx.starter.database.DbProps.DB_DRIVER;

import static io.vertx.starter.database.DbProps.PERSIST_INSULT_ADDRESS;

public class DbVerticle extends AbstractVerticle {

  private JDBCClient jdbcClient;

  @Override
  public void start(Future<Void> future) {

    System.out.println(config().getString("test"));

    jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", config().getString(CONFIG_DB_URL, DB_URL))
      .put("driver_class", config().getString(CONFIG_DB_DRIVER, DB_DRIVER))
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
          message.fail( FailureCodes.BAD_ACTION.ordinal(), FailureCodes.BAD_ACTION.failureCodeMessage + message.body());
      }
    });

    future.complete();

  }

  private void persistInsult(Message<JsonObject> message) {

    jdbcClient.updateWithParams("insert into INSULTS(body) VALUES (?);", new JsonArray().add(message.body().getString("insult")), res ->{
      if (res.succeeded()) {
        message.reply("success");
      }else {
        message.fail(FailureCodes.DB_ERROR.ordinal(), FailureCodes.DB_ERROR.failureCodeMessage + res.cause().getMessage());
      }
    });
  }

}
