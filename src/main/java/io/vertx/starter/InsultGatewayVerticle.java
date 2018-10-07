package io.vertx.starter;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class InsultGatewayVerticle extends AbstractVerticle {
	private ConfigRetriever conf;
    private String message;
    private String logLevel;


    private JsonObject config;

	@Override
    public void start(Future<Void> startFuture) {

		conf = ConfigRetriever.create(vertx);

		Router router = Router.router(vertx);
    router.get("/").handler(this::indexHandler);
    router.get("/api/insult").handler(this::retriveInsult);

        retrieveMessageTemplateFromConfiguration()
        .setHandler(ar -> {
            // Once retrieved, store it and start the HTTP server.
            message = ar.result();

            vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                    // Retrieve the port from the configuration,
                    // default to 8080.
                    config().getInteger("http.port", 8080));

        });

        retrieveLogLevelFromConfiguration()
        .setHandler(ar ->{

        	logLevel = ar.result();
        	//setLogLevel(logLevel);
        });




    }

  private void indexHandler(RoutingContext context) {
    context.response().setStatusCode(200)
      .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
      .end(new JsonObject().put("content", "hi").encode());
  }

	/*private void setLogLevel(String level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.getLevel(level));
        ctx.updateLoggers();
    }*/

	private Future<String> retrieveMessageTemplateFromConfiguration() {
        Future<String> future = Future.future();
        conf.getConfig(ar ->
            future.handle(ar
                .map(json -> json.getString("message"))
                .otherwise(t -> null)));
        return future;
    }
	private Future<String> retrieveLogLevelFromConfiguration() {
        Future<String> future = Future.future();
        conf.getConfig(ar ->
            future.handle(ar
                .map(json -> json.getString("level","INFO"))
                .otherwise(t -> null)));
        return future;
    }


	 private void retriveInsult(RoutingContext rc) {


		 if (message == null) {
	            rc.response().setStatusCode(500)
	                .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
	                .end(new JsonObject().put("content", "no config map").encode());
	            return;
	        }



	 }

}
