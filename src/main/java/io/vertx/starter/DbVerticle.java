package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

public class DbVerticle extends AbstractVerticle {

  protected static final String PERSIST_INSULT_ADDRESS = "ping-address";

  @Override
  public void start(Future<Void> future) {

    EventBus eventBus = vertx.eventBus();

    MessageConsumer<String> consumer = eventBus.consumer(PERSIST_INSULT_ADDRESS);
    consumer.handler(message -> {
      System.out.println("I have received a message: " + message.body());
    });

    future.complete();

  }

}
