package io.vertx.starter;

import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.circuitbreaker.CircuitBreakerState;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.CompositeFuture;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.starter.ApplicationProperties.*;


public class InsultGatewayVerticle extends AbstractVerticle{
  private static final Logger LOG = LoggerFactory.getLogger(InsultGatewayVerticle.class);

  private WebClient clientSpringboot;
  private WebClient clientSwarm;
  private WebClient clientVertx;
  private ConfigRetriever conf;

  CircuitBreaker clientSpringbootBreaker;
  CircuitBreaker clientSwarmBreaker;
  CircuitBreaker clientVertxBreaker;



  @Override
  public void start(Future<Void> startFuture) {

    conf = ConfigRetriever.create(vertx);
    Router router = Router.router(vertx);



    CircuitBreakerOptions breakerOpts = new CircuitBreakerOptions()
      .setFallbackOnFailure(true)
      .setMaxFailures(2)
      .setMaxRetries(2)
      .setResetTimeout(config().getInteger(GATEWAY_CIRCUIT_TIMEOUT, 1000))
      .setTimeout(config().getInteger(GATEWAY_RESET_TIMEOUT, 1000));

    clientSpringbootBreaker = CircuitBreaker
      .create("nounSpringBoot", vertx, breakerOpts)
      .openHandler(t -> circuitBreakerHandler("adj", "[open]"));


    clientSwarmBreaker = CircuitBreaker
      .create("swarmAdj", vertx, breakerOpts)
      .openHandler(t -> circuitBreakerHandler("swarmAdj", "[open]"));


    clientVertxBreaker = CircuitBreaker
      .create("vertxAdj", vertx, breakerOpts)
      .openHandler(t -> circuitBreakerHandler("vertxAdj", "[open]"));


    clientSpringboot = WebClient.create(vertx, new WebClientOptions()
      .setDefaultHost(config().getString(GATEWAY_HOST_SPRINGBOOT_NOUN, "springboot-noun-service-devenv-vmaddali.apps.ocpmwdemo.com"))
      .setDefaultPort(config().getInteger(GATEWAY_HOST_SPRINGBOOT_NOUN_PORT, 80)));

    clientSwarm = WebClient.create(vertx, new WebClientOptions()
      .setDefaultHost(config().getString(GATEWAY_HOST_WILDFLYSWARM_ADJ, "wildflyswarm-adj-devenv-vmaddali.apps.ocpmwdemo.com"))
      .setDefaultPort(config().getInteger(GATEWAY_HOST_WILDFLYSWARM_ADJ_PORT, 80)));



    clientVertx = WebClient.create(vertx, new WebClientOptions()
      .setDefaultHost(config().getString(GATEWAY_HOST_VERTX_ADJ,"vertx-adjective-service-devenv-vmaddali.apps.ocpmwdemo.com"))
      .setDefaultPort(config().getInteger(GATEWAY_HOST_VERTX_ADJ_PORT,80)));



    System.out.println("GATEWAY_HOST_WILDFLYSWARM_ADJ="+config().getString(GATEWAY_HOST_VERTX_ADJ,"springboot-noun-service.vertx-adjective.svc"));
    System.out.println("GATEWAY_HOST_SPRINGBOOT_NOUN="+config().getString(GATEWAY_HOST_SPRINGBOOT_NOUN,"wildflyswarm-adj.vertx-adjective.svc"));
    System.out.println("GATEWAY_HOST_VERTX_ADJ="+config().getString(GATEWAY_HOST_VERTX_ADJ,"wildflyswarm-adj.vertx-adjective.svc"));



    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    router.get("/api/insult").handler(this::insultHandler);
    router.get("/api/cb-state").handler(this::checkHealth);
    router.get("/*").handler(StaticHandler.create());
    System.out.println("Gdone");



    startFuture.complete();


  }


  public JsonObject circuitBreakerHandler(String key, String value) {
    System.out.println("Error= " + key + "," + "value=" + value);

    return new JsonObject().put(key, value);
  }
  io.vertx.reactivex.core.Future<JsonObject> getNoun() {


    return clientSpringbootBreaker.executeWithFallback(fut ->
      clientSpringboot.get("/api/noun")
        .timeout(3000)
        .rxSend()
        .doOnError(e -> LOG.error("REST Request failed", e))
        .map(HttpResponse::bodyAsJsonObject)
        .subscribe(
          j -> fut.complete(j),
          e -> fut.fail(e)
        ), t -> circuitBreakerHandler("noun", "[SpringBoot noun failure]"));


    //eturn fut;
  }

  public void checkHealth(RoutingContext rc) {
    // Request 2 adjectives and a noun in parallel, then handle the results


    boolean allBreakersClosed = (
      (clientSpringbootBreaker.state().equals(CircuitBreakerState.CLOSED)) &&
        (clientSwarmBreaker.state().equals(CircuitBreakerState.CLOSED)) && (clientVertxBreaker.state().equals(CircuitBreakerState.CLOSED)));


    JsonObject health = new JsonObject()
      .put("noun", new JsonObject()
        .put("failures", clientSpringbootBreaker.failureCount())
        .put("state", clientSpringbootBreaker.state().toString()))
      .put("Swarmadjective", new JsonObject()
        .put("failures", clientSwarmBreaker.failureCount())
        .put("state", clientSwarmBreaker.state().toString()))
      .put("Vertxadjective", new JsonObject()
        .put("failures", clientVertxBreaker.failureCount())
        .put("state", clientVertxBreaker.state().toString()))
      .put("status", allBreakersClosed ? "OK" : "UNHEALTHY");


    rc.response().putHeader("content-type", "application/json").end(health.encodePrettily());


  }
  io.vertx.reactivex.core.Future<JsonObject> getAdjective() {


    return clientSwarmBreaker.executeWithFallback(fut ->
      clientSwarm.get("/api/adjective")
        .timeout(3000)
        .rxSend()
        .doOnError(e -> LOG.error("REST Request failed", e))
        .map(HttpResponse::bodyAsJsonObject)
        .subscribe(
          j -> fut.complete(j),
          e -> fut.fail(e)
        ), t -> circuitBreakerHandler("adjective", "[Swarm adjective failure]"));
  }

  io.vertx.reactivex.core.Future<JsonObject> getAdjective2() {
    return clientVertxBreaker.executeWithFallback(fut ->
      clientVertx.get("/api/adjective")
        .timeout(3000)
        .rxSend()
        .doOnError(e -> LOG.error("REST Request failed", e))
        .map(HttpResponse::bodyAsJsonObject)
        .subscribe(
          j -> fut.complete(j),
          e -> fut.fail(e)
        ), t -> circuitBreakerHandler("adjective", "[Vertx adj failure]"));
  }

  private AsyncResult<JsonObject> buildInsult(CompositeFuture cf) {
    JsonObject insult = new JsonObject();
    JsonArray adjectives = new JsonArray();

    // Because there is no garanteed order of the returned futures, we need to parse the results

    for (int i=0; i<=cf.size()-1; i++) {
      JsonObject item = cf.resultAt(i);
      if (item.containsKey("adjective")) {
        adjectives.add(item.getString("adjective"));
      } else {
        insult.put("noun", item.getString("noun"));
      }

    }
    insult.put("adjectives", adjectives);


    return Future.succeededFuture(insult);
  }
  private void insultHandler(RoutingContext rc) {

    CompositeFuture.all(getNoun(), getAdjective(), getAdjective2())
      .setHandler(ar -> {

        if (ar.succeeded()) {
          AsyncResult<JsonObject> result=buildInsult(ar.result());
          rc.response().putHeader("content-type", "application/json").end(result.result().encodePrettily());
        }
        else
        {
          System.out.println("error");

          rc.response().putHeader("content-type", "application/json").end(new JsonObject("Error").encodePrettily());
        }



      });
  }

}
