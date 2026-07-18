package com.vinay.moneymanager.common.exception;

public class FeatureNotImplementedException extends RuntimeException {

  public FeatureNotImplementedException(String message) {
    super(message);
  }

  public FeatureNotImplementedException(String message, Throwable cause) {
    super(message, cause);
  }
}
