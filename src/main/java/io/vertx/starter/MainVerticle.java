package io.vertx.starter;

import io.reactivex.Maybe;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.starter.database.DbVerticle;

import static io.vertx.starter.ApplicationProperties.CONFIG_DB_URL;

public class MainVerticle extends AbstractVerticle{

  @Override
  public void start(Future<Void> startFuture) {

    initConfigRetriever()
      .doOnError(startFuture::fail)
      .subscribe(ar -> {
        vertx.deployVerticle(InsultGatewayVerticle.class.getName(), new DeploymentOptions().setConfig(ar));
        vertx.deployVerticle(DbVerticle.class.getName(), new DeploymentOptions().setConfig(ar));
//        vertx.deployVerticle(ConfigTestVerticle.class.getName(), new DeploymentOptions().setConfig(ar));
        startFuture.complete();
      });


/*
    Future<String> dbVerticleDeployment = Future.future();
    vertx.deployVerticle(new DbVerticle(), dbVerticleDeployment.completer());

    dbVerticleDeployment.compose(id -> {

      Future<String> httpVerticleDeployment = Future.future();
      vertx.deployVerticle(InsultGatewayVerticle.class.getName(), httpVerticleDeployment.completer());
      return httpVerticleDeployment;

    }).setHandler(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      }else {
        startFuture.fail(ar.cause());
      }
    });
*/
  }

  Maybe<JsonObject> initConfigRetriever() {

    // Load the default configuration from the classpath
    //LOG.info("Configuration store loading.");
    ConfigStoreOptions localConfig = new ConfigStoreOptions()
      .setType("file")
      .setFormat("json")
      .setConfig(new JsonObject().put("path", "insult-config.json"));

    // When running inside of Kubernetes, configure the application to also load
    // from a ConfigMap. This config is ONLY loaded when running inside of
    // Kubernetes or OpenShift
/*
    ConfigStoreOptions confOpts = new ConfigStoreOptions()
      .setType("configmap")
      .setFormat("yaml")
      .setConfig(new JsonObject()
        .put("name", "app-config")
        .put("optional", true)
      );
*/

    // Add the default and container config options into the ConfigRetriever
    ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions()
      .addStore(localConfig);
//      .addStore(confOpts);

    // Create the ConfigRetriever and return the Maybe when complete
    return ConfigRetriever.create(vertx, retrieverOptions).rxGetConfig().toMaybe();
  }
}
