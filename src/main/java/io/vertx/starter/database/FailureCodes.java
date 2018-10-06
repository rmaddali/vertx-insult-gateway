package io.vertx.starter.database;

public enum FailureCodes {

  NO_ACTION_SPECIFIED( "No action specified"),
  BAD_ACTION("Bad action"),
  DB_ERROR("Database error");

  public String failureCodeMessage;

  private FailureCodes(String msg) {
    this.failureCodeMessage = msg;
  }

}
