package com.xm.crypto.investment.service;

import com.xm.crypto.investment.domain.dto.CryptoNormalizedDto;
import com.xm.crypto.investment.domain.dto.CryptoNormalizedListDto;
import com.xm.crypto.investment.domain.dto.CryptoStatisticDto;
import com.xm.crypto.investment.exception.CryptoNotSupportedException;
import com.xm.crypto.investment.repository.CryptoDataRepository;
import com.xm.crypto.investment.repository.projection.CryptoSummaryView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoDataService {

  private final CryptoDataRepository cryptoDataRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * Calculates a list of cryptocurrency prices normalized to a specified date range from the database.
   * The list will be sorted in descending order based on the normalized prices.
   *
   * @param dateFrom The start date for the query
   * @param dateTo   The end date for the query
   *                 return CryptoNormalizedListDto
   */

  public CryptoNormalizedListDto getCryptosNormalizedRange(LocalDate dateFrom, LocalDate dateTo) {
    var key = getKeyForNormalizedRange(dateFrom, dateTo);
    var cachedResult = redisTemplate.opsForValue().get(key);
    CryptoNormalizedListDto cryptoNormalizedListDto;
    if (cachedResult != null ) {
      cryptoNormalizedListDto = (CryptoNormalizedListDto) cachedResult;
      return cryptoNormalizedListDto;
    }
    var dateTimeFrom =
      dateFrom != null ? LocalDateTime.of(dateFrom, LocalTime.MIN) : LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    var dateTimeTo = dateTo != null ? LocalDateTime.of(dateTo, LocalTime.MIN) : LocalDateTime.now();
    log.info("getCryptosNormalizedRange:: Trying to load cryptos summary from DB for dateFrom {} and dateTo {}", dateFrom, dateTo);
    var cryptoSummaryViewList = cryptoDataRepository.calculateCryptosSummary(dateTimeFrom, dateTimeTo);
    var cryptoNormalizedList = calculateSortedNormalizedRanges(cryptoSummaryViewList);
    var result = new CryptoNormalizedListDto().cryptos(cryptoNormalizedList);
    redisTemplate.opsForValue().set(key, result);
    return result;
  }

  /**
   * Returns Crypto with highest normalized range for the specified day.
   *
   * @param date specified day
   * @return CryptoNormalizedDto
   */
  public CryptoNormalizedDto getNormalizedRangeHighest(LocalDate date) {
    var toDate = date.plusDays(1);
    var key = getKeyForNormalizedRange(date, toDate);
    var cachedResult = redisTemplate.opsForValue().get(key);
    CryptoNormalizedListDto cryptoNormalizedListDto;
    if (cachedResult != null) {
      cryptoNormalizedListDto = (CryptoNormalizedListDto) cachedResult;
      return cryptoNormalizedListDto.getCryptos().get(0);
    }

    var dateTimeFrom = LocalDateTime.of(date, LocalTime.MIN);
    var dateTimeTo = LocalDateTime.of(toDate, LocalTime.MIN);
    log.info("getNormalizedRangeHighest:: Trying to load cryptos summary from DB for date {}", date);
    var cryptoSummaryViewList = cryptoDataRepository.calculateCryptosSummary(dateTimeFrom, dateTimeTo);
    var cryptoNormalizedList = calculateSortedNormalizedRanges(cryptoSummaryViewList);
    redisTemplate.opsForValue().set(key, new CryptoNormalizedListDto().cryptos(cryptoNormalizedList));
    return cryptoNormalizedList.get(0);
  }

  /**
   * Calculates oldest/newest/min/max for each crypto in any time frame.
   *
   * @param crypto   crypto symbol (e.g BTC)
   * @param dateFrom The start date for the query
   * @param dateTo   The end date for the query
   * @return CryptoStatisticDto
   */
  public CryptoStatisticDto getStatisticsByCrypto(String crypto, LocalDate dateFrom, LocalDate dateTo) {
    crypto = crypto.toUpperCase();
    var key = getKeyForStatistic(crypto, dateFrom, dateTo);
    var cachedResult = redisTemplate.opsForValue().get(key);
    CryptoStatisticDto cryptoStatisticDto;
    if (cachedResult != null) {
      cryptoStatisticDto = (CryptoStatisticDto) cachedResult;
      return cryptoStatisticDto;
    }
    var dateTimeFrom =
      dateFrom != null ? LocalDateTime.of(dateFrom, LocalTime.MIN) : LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    var dateTimeTo = dateTo != null ? LocalDateTime.of(dateTo, LocalTime.MIN) : LocalDateTime.now();
    log.info("getStatisticsByCrypto:: Hitting DB for dateFrom {} and dateTo {}", dateFrom, dateTo);
    var cryptoSummaryViewList = cryptoDataRepository.calculateCryptosSummary(dateTimeFrom, dateTimeTo);
    cryptoStatisticDto = calculateCryptoStatisticsByCrypto(crypto, cryptoSummaryViewList);
    redisTemplate.opsForValue().set(key, cryptoStatisticDto);
    return cryptoStatisticDto;
  }

  private String getKeyForNormalizedRange(LocalDate fromDate, LocalDate toDate) {
    if (fromDate == null && toDate == null) {
      return "normalized_all_interval";
    }
    return String.format("normalized_%s_%s", fromDate, toDate);
  }

  private String getKeyForStatistic(String crypto, LocalDate fromDate, LocalDate toDate) {
    if (fromDate == null && toDate == null) {
      return crypto + "_all_interval";
    }
    return String.format("statistic_%s_%s_%s", crypto, fromDate, toDate);
  }

  private List<CryptoNormalizedDto> calculateSortedNormalizedRanges(List<CryptoSummaryView> cryptoSummaryViewList) {

    return cryptoSummaryViewList.stream().map(cryptoSummaryView -> {
      var normalizedRange = new CryptoNormalizedDto();
      normalizedRange.setSymbol(cryptoSummaryView.getSymbol());
      var minPrice = BigDecimal.valueOf(cryptoSummaryView.getMinPrice());
      var maxPrice = BigDecimal.valueOf(cryptoSummaryView.getMaxPrice());
      if (!BigDecimal.ZERO.equals(minPrice)) {
        var normalizedPrice = (maxPrice.subtract(minPrice))
          .divide(minPrice, RoundingMode.HALF_EVEN);
        normalizedRange.setNormalizedPrice(normalizedPrice);
        return normalizedRange;
      }
      normalizedRange.setNormalizedPrice(BigDecimal.ZERO);
      return normalizedRange;
    }).sorted(Comparator.comparing(CryptoNormalizedDto::getNormalizedPrice)
      .reversed()).collect(Collectors.toList());
  }

  private CryptoStatisticDto calculateCryptoStatisticsByCrypto(String crypto, List<CryptoSummaryView> cryptoSummaryViewList) {
    var cryptoSummary = cryptoSummaryViewList.stream()
      .filter(cryptoSummaryView -> cryptoSummaryView.getSymbol().equals(crypto))
      .findFirst();
    if (cryptoSummary.isEmpty()) {
      log.error("At the moment, the {} symbol for cryptocurrency is not supported.", crypto);
      throw new CryptoNotSupportedException(crypto);
    }
    var cryptoStatisticDto = new CryptoStatisticDto();
    cryptoStatisticDto.setSymbol(cryptoSummary.get().getSymbol());
    cryptoStatisticDto.setMax(BigDecimal.valueOf(cryptoSummary.get().getMaxPrice()));
    cryptoStatisticDto.setMin(BigDecimal.valueOf(cryptoSummary.get().getMinPrice()));
    cryptoStatisticDto.setNewest(BigDecimal.valueOf(cryptoSummary.get().getNewestPrice()));
    cryptoStatisticDto.setOldest(BigDecimal.valueOf(cryptoSummary.get().getOldestPrice()));
    return cryptoStatisticDto;
  }
}
