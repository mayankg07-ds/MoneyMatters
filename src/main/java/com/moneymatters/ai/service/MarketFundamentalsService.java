package com.moneymatters.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fetches richer Yahoo Finance fundamentals (PE, ROE, DMA, analyst rating) for AI prompt enrichment.
 * Separate from StockPriceService which only does price/chart data.
 */
@Service
@Slf4j
public class MarketFundamentalsService {

    private static final String SUMMARY_URL =
        "https://query1.finance.yahoo.com/v10/finance/quoteSummary/%s"
        + "?modules=price,defaultKeyStatistics,financialData,summaryDetail,assetProfile";
    private static final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10)).build();

    @Cacheable(value = "stockFundamentals", key = "#yahooSymbol")
    public Map<String, Object> getFundamentals(String yahooSymbol) {
        Map<String, Object> out = new LinkedHashMap<>();
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(String.format(SUMMARY_URL, yahooSymbol)))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", USER_AGENT)
                .GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                log.warn("Yahoo fundamentals returned {} for {}", res.statusCode(), yahooSymbol);
                return out;
            }
            JsonNode root = mapper.readTree(res.body());
            JsonNode result = root.path("quoteSummary").path("result");
            if (!result.isArray() || result.size() == 0) return out;
            JsonNode data = result.get(0);

            JsonNode price = data.path("price");
            put(out, "companyName", text(price, "longName"));
            put(out, "currentPrice", raw(price, "regularMarketPrice"));
            put(out, "dayChangePercent", raw(price, "regularMarketChangePercent"));
            put(out, "marketCap", raw(price, "marketCap"));

            JsonNode profile = data.path("assetProfile");
            put(out, "sector", text(profile, "sector"));
            put(out, "industry", text(profile, "industry"));

            JsonNode stats = data.path("defaultKeyStatistics");
            put(out, "trailingPE", raw(stats, "trailingPE"));
            put(out, "priceToBook", raw(stats, "priceToBook"));
            put(out, "beta", raw(stats, "beta"));
            put(out, "earningsGrowth", raw(stats, "earningsQuarterlyGrowth"));

            JsonNode fin = data.path("financialData");
            put(out, "roe", raw(fin, "returnOnEquity"));
            put(out, "roa", raw(fin, "returnOnAssets"));
            put(out, "debtToEquity", raw(fin, "debtToEquity"));
            put(out, "revenueGrowth", raw(fin, "revenueGrowth"));
            put(out, "currentRatio", raw(fin, "currentRatio"));
            put(out, "analystTargetPrice", raw(fin, "targetMeanPrice"));
            put(out, "analystRating", text(fin, "recommendationKey"));

            JsonNode sd = data.path("summaryDetail");
            put(out, "dividendYield", raw(sd, "dividendYield"));
            put(out, "fiftyDayAverage", raw(sd, "fiftyDayAverage"));
            put(out, "twoHundredDayAverage", raw(sd, "twoHundredDayAverage"));
            put(out, "fiftyTwoWeekHigh", raw(sd, "fiftyTwoWeekHigh"));
            put(out, "fiftyTwoWeekLow", raw(sd, "fiftyTwoWeekLow"));
        } catch (Exception e) {
            log.warn("Failed to fetch fundamentals for {}: {}", yahooSymbol, e.getMessage());
        }
        return out;
    }

    private void put(Map<String, Object> m, String k, Object v) {
        if (v != null) m.put(k, v);
    }

    private Object raw(JsonNode parent, String field) {
        JsonNode n = parent.path(field);
        if (n.isMissingNode() || n.isNull()) return null;
        if (n.has("raw")) {
            JsonNode r = n.path("raw");
            return r.isNumber() ? r.numberValue() : r.asText();
        }
        if (n.isNumber()) return n.numberValue();
        if (n.isTextual()) return n.asText();
        return null;
    }

    private String text(JsonNode parent, String field) {
        JsonNode n = parent.path(field);
        return (n.isMissingNode() || n.isNull()) ? null : n.asText(null);
    }
}
