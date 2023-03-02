package com.xm.crypto.investment.repository;

import com.xm.crypto.investment.model.CryptoData;
import com.xm.crypto.investment.repository.projection.CryptoSummaryView;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CryptoDataRepository extends JpaRepository<CryptoData, Long> {

  @Query(value = "SELECT DISTINCT symbol, " +
    "MIN(price) OVER (PARTITION BY symbol) AS minPrice, " +
    "MAX(price) OVER (PARTITION BY symbol) AS maxPrice, " +
    "FIRST_VALUE(price) OVER (PARTITION BY symbol ORDER BY date_time ASC) AS oldestPrice, " +
    "FIRST_VALUE(price) OVER (PARTITION BY symbol ORDER BY date_time DESC) AS newestPrice " +
    "FROM crypto_data " +
    "WHERE date_time BETWEEN ?1 AND ?2 " +
    "GROUP BY symbol, price, date_time", nativeQuery = true)
  List<CryptoSummaryView> calculateCryptosSummary(LocalDateTime dateFrom,
                                                  LocalDateTime dateTo);

}
