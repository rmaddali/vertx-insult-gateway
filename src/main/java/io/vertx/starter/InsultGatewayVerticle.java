package io.vertx.starter;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;



public class InsultGatewayVerticle extends AbstractVerticle {
	private ConfigRetriever conf;
    private String message;
    private String logLevel;
    private WebClient clientSpringboot;
    private WebClient clientSwarm;
    private WebClient clientVertx;
    
    private JsonObject config;
	
    private ServiceDiscovery discovery;
	
	@Override
    public void start() {

		conf = ConfigRetriever.create(vertx);
		Router router = Router.router(vertx);
		

        clientSpringboot = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("springboot-noun-service.vertx-adjective.svc")
                .setDefaultPort(8080));

            clientSwarm = WebClient.create(vertx, new WebClientOptions()
                    .setDefaultHost("wildflyswarm-adj.vertx-adjective.svc")
                    .setDefaultPort(8080));
            router.get("/api/insult").handler(this::invoke);
		
            ServiceDiscovery.create(vertx, discovery ->
  	      // Retrieve a web client
  	      HttpEndpoint.getWebClient(discovery, svc -> svc.getName().equals("vertx-adjective-service"), ar -> {
  	        if (ar.failed()) {
  	          System.out.println("D'oh the service is not available");
  	        } else {
  	        	clientVertx = ar.result();
  	        	vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  	        	
  	        }
  	      }));
		
		
        
		
		
		
		
		
		
        
        
            
            
        
    	
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

	private void invoke(RoutingContext rc) {
		clientVertx.get("/api/adjective").as(BodyCodec.string()).send(ar -> {
	      if (ar.failed()) {
	        rc.response().end("Unable to call the greeting service: " + ar.cause().getMessage());
	      } else {
	        if (ar.result().statusCode() != 200) {
	          rc.response().end("Unable to call the greeting service - received status:" + ar.result().statusMessage());
	        } else {
	          rc.response().end("Greeting service invoked: '" + ar.result().body() + "'");
	        }
	      }
	    });
	  }
	 private void retriveInsult(RoutingContext rc) {
		 
		 
		 if (message == null) {
	            rc.response().setStatusCode(500)
	                .putHeader(CONTENT_TYPE.toString(), "application/json; charset=utf-8")
	                .end(new JsonObject().put("content", "no config map").encode());
	            return;
	        }
		 
		 
		 
	 }
		 private Single<Record> publishmsg(HttpServer server) {
			    // 1 - Create a service record using `io.vertx.reactivex.servicediscovery.types.HttpEndpoint.createRecord`.
			    // This record defines the service name ("greetings"), the host ("localhost"), the server port and the root ("/")
			    Record record = HttpEndpoint.createRecord("greetings", "localhost", server.actualPort(), "/");

			    // 2 - Call the rxPublish method with the created record and return the resulting single
			    return discovery.rxPublish(record);
			}
		
		 
	 

}
