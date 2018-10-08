package io.vertx.starter.database;

public class DbProps {

  public static final String INSULTS_ADDRESS = "insults-address";
  protected static final String DB_URL = "jdbc:hsqldb:mem:testdb;shutdown=true";
  protected static final String DB_DRIVER = "org.hsqldb.jdbcDriver";
  protected static final String DB_USER = "sa";
  protected static final String DB_PASSWORD = "sa";

  protected static final String QUERY_ALL_INSULTS = "SELECT * FROM INSULTS";

}
