package com.moneymatters.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StockPriceService {

    private static final String YAHOO_CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart/%s?range=1d&interval=1d";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get current price for a single stock using Yahoo v8 chart API
     */
    @Cacheable(value = "stockPrices", key = "#symbol")
    public BigDecimal getCurrentPrice(String symbol) {
        try {
            log.info("Fetching current price for: {}", symbol);
            JsonNode root = fetchYahooData(symbol);

            if (root == null) return null;

            JsonNode meta = root.path("chart").path("result").get(0).path("meta");
            BigDecimal price = new BigDecimal(meta.path("regularMarketPrice").asText())
                    .setScale(2, RoundingMode.HALF_UP);

            log.info("Price for {}: {}", symbol, price);
            return price;
        } catch (Exception e) {
            log.error("Error fetching price for {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    /**
     * Get current prices for multiple stocks
     */
    public Map<String, BigDecimal> getCurrentPrices(List<String> symbols) {
        Map<String, BigDecimal> prices = new HashMap<>();

        for (String symbol : symbols) {
            try {
                BigDecimal price = getCurrentPrice(symbol);
                if (price != null) {
                    prices.put(symbol, price);
                }
            } catch (Exception e) {
                log.error("Error fetching price for {}: {}", symbol, e.getMessage());
            }
        }

        log.info("Successfully fetched {} / {} prices", prices.size(), symbols.size());
        return prices;
    }

    /**
     * Get detailed stock information
     */
    public StockDetails getStockDetails(String symbol) {
        try {
            JsonNode root = fetchYahooData(symbol);
            if (root == null) return null;

            JsonNode result = root.path("chart").path("result").get(0);
            JsonNode meta = result.path("meta");

            BigDecimal regularMarketPrice = toBigDecimal(meta, "regularMarketPrice");
            BigDecimal previousClose = toBigDecimal(meta, "chartPreviousClose");
            BigDecimal dayHigh = toBigDecimal(meta, "regularMarketDayHigh");
            BigDecimal dayLow = toBigDecimal(meta, "regularMarketDayLow");
            BigDecimal open = toBigDecimal(meta, "regularMarketOpen"); // not always available
            Long volume = meta.has("regularMarketVolume") ? meta.path("regularMarketVolume").asLong() : 0L;
            BigDecimal fiftyTwoWeekHigh = toBigDecimal(meta, "fiftyTwoWeekHigh");
            BigDecimal fiftyTwoWeekLow = toBigDecimal(meta, "fiftyTwoWeekLow");

            BigDecimal change = BigDecimal.ZERO;
            BigDecimal changePercent = BigDecimal.ZERO;
            if (regularMarketPrice != null && previousClose != null && previousClose.compareTo(BigDecimal.ZERO) > 0) {
                change = regularMarketPrice.subtract(previousClose).setScale(2, RoundingMode.HALF_UP);
                changePercent = change.multiply(new BigDecimal(100))
                        .divide(previousClose, 2, RoundingMode.HALF_UP);
            }

            String name = meta.has("longName") ? meta.path("longName").asText()
                    : meta.has("shortName") ? meta.path("shortName").asText() : symbol;

            return new StockDetails(
                    symbol, name, regularMarketPrice, previousClose, open,
                    dayHigh, dayLow, change, changePercent, volume,
                    fiftyTwoWeekHigh, fiftyTwoWeekLow
            );
        } catch (Exception e) {
            log.error("Error fetching stock details for {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    /**
     * Convert Indian stock symbol to Yahoo Finance format
     */
    public String toYahooSymbol(String symbol, String exchange) {
        if (symbol == null || symbol.isEmpty()) {
            return symbol;
        }
        if (symbol.contains(".NS") || symbol.contains(".BO")) {
            return symbol;
        }
        if ("BSE".equalsIgnoreCase(exchange)) {
            return symbol + ".BO";
        }
        return symbol + ".NS"; // Default to NSE
    }

    // ---- Internal helpers ----

    private JsonNode fetchYahooData(String symbol) throws Exception {
        String url = String.format(YAHOO_CHART_URL, symbol);
        log.info("Sending request: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Yahoo API returned status {} for {}", response.statusCode(), symbol);
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());

        if (root.path("chart").path("result").isEmpty() || root.path("chart").path("result").get(0) == null) {
            log.warn("No data in Yahoo response for {}", symbol);
            return null;
        }

        return root;
    }

    private BigDecimal toBigDecimal(JsonNode node, String field) {
        if (node.has(field) && !node.path(field).isNull()) {
            return new BigDecimal(node.path(field).asText()).setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    /**
     * Stock details DTO
     */
    public record StockDetails(
        String symbol,
        String name,
        BigDecimal currentPrice,
        BigDecimal previousClose,
        BigDecimal open,
        BigDecimal dayHigh,
        BigDecimal dayLow,
        BigDecimal change,
        BigDecimal changePercent,
        Long volume,
        BigDecimal yearHigh,
        BigDecimal yearLow
    ) {}
}
