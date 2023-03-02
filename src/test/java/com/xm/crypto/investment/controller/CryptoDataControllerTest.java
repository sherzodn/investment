package com.xm.crypto.investment.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xm.crypto.investment.domain.dto.CryptoNormalizedDto;
import com.xm.crypto.investment.domain.dto.CryptoNormalizedListDto;
import com.xm.crypto.investment.domain.dto.CryptoStatisticDto;
import com.xm.crypto.investment.service.CryptoDataService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CryptoDataController.class)
class CryptoDataControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private CryptoDataService cryptoDataService;

  @Test
  void testGetNormalizedRange() throws Exception {
    var cryptoNormalizedRangeListDto = new CryptoNormalizedListDto().cryptos(
      List.of(
        new CryptoNormalizedDto().normalizedPrice(BigDecimal.valueOf(46813.21)).symbol("BTC"),
        new CryptoNormalizedDto().normalizedPrice(BigDecimal.valueOf(3715.32)).symbol("ETH")
      ));

    when(cryptoDataService.getCryptosNormalizedRange(
      LocalDate.of(2022, 1, 1),
      LocalDate.of(2022, 1, 2)))
      .thenReturn(cryptoNormalizedRangeListDto);

    mockMvc.perform(get("/api/v1/cryptos/range?dateFrom=2022-01-01&dateTo=2022-01-02"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.cryptos[0].symbol").value("BTC"))
      .andExpect(jsonPath("$.cryptos[0].normalizedPrice").value(46813.21))
      .andExpect(jsonPath("$.cryptos[1].symbol").value("ETH"))
      .andExpect(jsonPath("$.cryptos[1].normalizedPrice").value(3715.32));
  }

  @Test
  void testGetNormalizedRangeHighest() throws Exception {

    when(cryptoDataService.getNormalizedRangeHighest(LocalDate.of(2022, 1, 1)))
      .thenReturn(new CryptoNormalizedDto().normalizedPrice(BigDecimal.valueOf(46813.21)).symbol("BTC"));

    mockMvc.perform(get("/api/v1/cryptos/range/highest/{date}", "2022-01-01"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.symbol").value("BTC"))
      .andExpect(jsonPath("$.normalizedPrice").value(46813.21));
  }

  @Test
  void getStatisticsByCrypto() throws Exception {
    var cryptoStatsDto = new CryptoStatisticDto()
      .max(BigDecimal.valueOf(44000.52))
      .min(BigDecimal.valueOf(11000.52))
      .oldest(BigDecimal.valueOf(40000.52))
      .newest(BigDecimal.valueOf(20000.52))
      .symbol("BTC");

    when(cryptoDataService.getStatisticsByCrypto(
      "BTC",
      LocalDate.of(2022, 1, 1),
      LocalDate.of(2022, 1, 2)))
      .thenReturn(cryptoStatsDto);

    mockMvc.perform(
        get("/api/v1/cryptos/statistics/{crypto}?dateFrom=2022-01-01&dateTo=2022-01-02",
          "BTC"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.symbol").value("BTC"))
      .andExpect(jsonPath("$.min").value(11000.52))
      .andExpect(jsonPath("$.max").value(44000.52))
      .andExpect(jsonPath("$.oldest").value(40000.52))
      .andExpect(jsonPath("$.newest").value(20000.52));
  }

}