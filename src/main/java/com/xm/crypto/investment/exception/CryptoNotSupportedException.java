package com.xm.crypto.investment.exception;

public class CryptoNotSupportedException extends RuntimeException {
  public CryptoNotSupportedException(String crypto) {
    super(String.format("At the moment, the %s symbol for cryptocurrency is not supported.", crypto));
  }
}
