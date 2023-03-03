package com.xm.crypto.investment.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.xm.crypto.investment.domain.dto.CryptoNormalizedDto;
import com.xm.crypto.investment.domain.dto.CryptoNormalizedListDto;
import com.xm.crypto.investment.domain.dto.CryptoStatisticDto;
import com.xm.crypto.investment.service.CryptoDataService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class CryptoRecommendationIntegrationTest extends AbstractIntegrationTest {
  @Autowired
  private CryptoDataService cryptoDataService;
  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  @Test
  public void testGetStatisticsByCrypto_BTC_allInterval() {
    var resultFromDb = cryptoDataService.getStatisticsByCrypto("BTC", null, null);
    assertEquals(BigDecimal.valueOf(46813.21), resultFromDb.getOldest());

    var cachedValue = (CryptoStatisticDto) redisTemplate.opsForValue().get("BTC_all_interval");
    assertEquals(BigDecimal.valueOf(46813.21), cachedValue.getOldest());

    assertEquals(cachedValue, resultFromDb);
  }

  @Test
  public void testGetCryptosNormalizedRange_allInterval() {
    var result = cryptoDataService.getCryptosNormalizedRange(null, null);
    assertEquals(2, result.getCryptos().size());
    assertEquals("DOGE", result.getCryptos().get(0).getSymbol());
    assertEquals(BigDecimal.valueOf(0.46), result.getCryptos().get(0).getNormalizedPrice());
    assertEquals("BTC", result.getCryptos().get(1).getSymbol());
    assertEquals(BigDecimal.valueOf(0.43), result.getCryptos().get(1).getNormalizedPrice());
  }

  @Test
  public void testGetNormalizedRangeHighest_forSpecificDay() {
    var date = LocalDate.of(2022, 1, 1);
    var result = cryptoDataService.getNormalizedRangeHighest(date);
    assertEquals("BTC", result.getSymbol());
    assertEquals(BigDecimal.valueOf(0.02), result.getNormalizedPrice());
    var key = String.format("normalized_%s_%s", date, date.plusDays(1));
    var cachedValue = (CryptoNormalizedListDto) redisTemplate.opsForValue().get(key);
    assertNotNull(cachedValue);
    assertEquals(cachedValue.getCryptos().get(0), result);
  }

  //some other cases need to be covered, my time is over for this task, so that I need to push my changes here

}
