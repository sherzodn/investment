package com.xm.crypto.investment.service;

import com.xm.crypto.investment.model.CryptoData;
import com.xm.crypto.investment.repository.CryptoDataRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Loads crypto data from CSV files when the application starts up.
 * All data will be stored in repositories
 */
@Component
@Slf4j
public class CsvFileParserService {

  @Value("${crypto.location-pattern}")
  private String cryptoLocationPattern;
  @Autowired
  private CryptoDataRepository cryptoDataRepository;

  /**
   * On the occurrence of the ApplicationReadyEvent, the method loads the crypto data
   * from CSV files and persists it into the database using repositories.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void loadPricesFromCsv() {
    log.info("loadPricesFromCsv:: Started loading CSV files by location pattern: {}", cryptoLocationPattern);
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    try {
      Resource[] resources = resolver.getResources(cryptoLocationPattern);
      for (Resource resource : resources) {
        loadCsvFile(resource.getInputStream());
      }
    } catch (IOException e) {
      log.error("loadPricesFromCsv:: Crypto load process is failed.", e);
      return;
    }
    log.info("loadPricesFromCsv:: CSV files successfully stored in DB");
  }

  private void loadCsvFile(InputStream inputStream) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      var cryptoDataList = reader.lines().skip(1)
        .map(this::mapToCryptoData).collect(Collectors.toList());
      cryptoDataRepository.saveAll(cryptoDataList);
    }
  }

  private CryptoData mapToCryptoData(String line) {
    String[] values = line.split(",");
    var timestamp = Instant.ofEpochMilli(Long.parseLong(values[0]));
    var symbol = values[1];
    var price = Double.parseDouble(values[2]);
    return new CryptoData(null, LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC), symbol, BigDecimal.valueOf(price));
  }
}
