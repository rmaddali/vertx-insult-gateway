package io.vertx.starter;


import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;



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
            router.get("/api/insult").handler(this::getREST);
		
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
		
		
        
		
		
		
		
		
		
        
        
            
            
        
    	
        
        
    	
    	
    	
    }
	
	/*private void setLogLevel(String level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.getLevel(level));
        ctx.updateLoggers();
    }*/

	
	

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
	Future<JsonObject> getNoun() {
        Future<JsonObject> fut = Future.future();
        clientSpringboot.get("/api/noun")
                .timeout(3000)
                .rxSend()
               
                .map(HttpResponse::bodyAsJsonObject)
                .doOnError(fut::fail)
                .subscribe(fut::complete);
        return fut;
    }
	
	
	Future<JsonObject> getAdjective() {
        Future<JsonObject> fut = Future.future();
        clientSwarm.get("/api/adjective")
                .timeout(3000)
                .rxSend()
                
                .map(HttpResponse::bodyAsJsonObject)
                .doOnError(fut::fail)
                .subscribe(fut::complete);
        return fut;
    }
	Future<JsonObject> getAdjective2() {
        Future<JsonObject> fut = Future.future();
        clientVertx.get("/api/adjective")
                .timeout(3000)
                .rxSend()
                
                .map(HttpResponse::bodyAsJsonObject)
                .doOnError(fut::fail)
                .subscribe(fut::complete);
        return fut;
    }
	
	
	
	public void getREST(RoutingContext rc) {
        // Request 2 adjectives and a noun in parallel, then handle the results
		
		
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
	
	
	

	private  final CompositeFuture mapResultToError(CompositeFuture res)
            throws Exception {
if (res.succeeded()) {
return res;
}
throw new Exception(res.cause());
}

		 
	 

}
