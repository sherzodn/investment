package com.xm.crypto.investment.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.xm.crypto.investment.domain.dto.CryptoStatisticDto;
import com.xm.crypto.investment.service.CryptoDataService;
import java.math.BigDecimal;
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

}
