package io.vertx.starter.database;

public class DbProps {

  protected static final String PERSIST_INSULT_ADDRESS = "ping-address";
  protected static final String CONFIG_DB_URL = "db.url";
  protected static final String DB_URL = "jdbc:hsqldb:mem:testdb;shutdown=true";
  protected static final String CONFIG_DB_DRIVER = "db.driver";
  protected static final String DB_DRIVER = "org.hsqldb.jdbcDriver";

  protected static final String QUERY_ALL_INSULTS = "SELECT * FROM INSULTS";

}
