package io.vertx.starter.database;

public class DbProps {

  protected static final String INSULTS_ADDRESS = "insults-address";
  protected static final String RETRIEVE_ALL_INSULTS_ADDRESS = "retrieve-insults";
  protected static final String CONFIG_DB_URL = "db.url";
  protected static final String DB_URL = "jdbc:hsqldb:mem:testdb;shutdown=true";
  protected static final String CONFIG_DB_DRIVER = "db.driver";
  protected static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
  protected static final String DB_USER = "sa";
  protected static final String CONFIG_DB_USER = "db.user";
  protected static final String DB_PASSWORD = null;
  protected static final String CONFIG_DB_PASSWORD = "db.password";

  protected static final String QUERY_ALL_INSULTS = "SELECT * FROM INSULTS";

}
