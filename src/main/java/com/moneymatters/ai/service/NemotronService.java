package com.moneymatters.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneymatters.ai.config.NvidiaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NemotronService {

    private final NvidiaProperties props;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public String chat(String systemPrompt, String userPrompt) {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            log.warn("NVIDIA API key not configured");
            return "AI is not configured on this server. Please set NVIDIA_API_KEY.";
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", props.getModel());
        body.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userPrompt)
        ));
        body.put("temperature", props.getTemperature());
        body.put("max_tokens", props.getMaxTokens());

        try {
            String payload = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(props.getApiUrl()))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + props.getApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 400) {
                log.error("Nemotron error {}: {}", res.statusCode(), res.body());
                return "AI service returned an error (HTTP " + res.statusCode() + "). Please try again shortly.";
            }
            JsonNode root = mapper.readTree(res.body());
            JsonNode choice = root.path("choices").get(0);
            if (choice == null || choice.isMissingNode()) {
                return "AI returned no response. Please try again.";
            }
            String content = choice.path("message").path("content").asText("");
            return content.isBlank() ? "AI returned an empty response. Please try again." : content;
        } catch (Exception e) {
            log.error("Nemotron call failed", e);
            return "AI analysis temporarily unavailable. Please try again in a moment.";
        }
    }
}
