package io.vertx.starter;

import io.vertx.reactivex.core.AbstractVerticle;

public class ConfigTestVerticle extends AbstractVerticle{

  @Override
  public void start() {

    System.out.println(config().getString(ApplicationProperties.CONFIG_DB_URL, "nope"));
    vertx.createHttpServer()
      .requestHandler(req -> req.response().end(config().getString(ApplicationProperties.CONFIG_DB_URL, "None")))
      .listen(8080);
  }
}
