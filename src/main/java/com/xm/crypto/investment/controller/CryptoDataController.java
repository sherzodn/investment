package com.xm.crypto.investment.controller;

import com.xm.crypto.investment.domain.dto.CryptoNormalizedDto;
import com.xm.crypto.investment.domain.dto.CryptoNormalizedListDto;
import com.xm.crypto.investment.domain.dto.CryptoStatisticDto;
import com.xm.crypto.investment.rest.resource.CryptosApi;
import com.xm.crypto.investment.service.CryptoDataService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CryptoDataController implements CryptosApi {
  private final CryptoDataService cryptoDataService;

  @Override
  public ResponseEntity<CryptoNormalizedListDto> getNormalizedRange(LocalDate dateFrom, LocalDate dateTo) {
    return ResponseEntity.ok(cryptoDataService.getCryptosNormalizedRange(dateFrom, dateTo));
  }

  @Override
  public ResponseEntity<CryptoNormalizedDto> getNormalizedRangeHighest(String date) {
    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
    return ResponseEntity.ok(cryptoDataService.getNormalizedRangeHighest(localDate));
  }

  @Override
  public ResponseEntity<CryptoStatisticDto> getStatisticsByCrypto(String crypto, LocalDate dateFrom, LocalDate dateTo) {
    return ResponseEntity.ok(cryptoDataService.getStatisticsByCrypto(crypto, dateFrom, dateTo));
  }
}
