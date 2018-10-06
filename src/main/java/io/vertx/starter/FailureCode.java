package io.vertx.starter;

public enum FailureCode {

  NO_ACTION_SPECIFIED( "No action specified"),
  BAD_ACTION("Bad action"),
  DB_ERROR("Database error");

  public String failureCodeMessage;

  private FailureCode(String msg) {
    this.failureCodeMessage = msg;
  }

}
