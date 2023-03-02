package com.xm.crypto.investment.repository.projection;

public interface CryptoSummaryView {

  String getSymbol();

  Double getMinPrice();

  Double getMaxPrice();

  Double getOldestPrice();

  Double getNewestPrice();
}
