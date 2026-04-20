package com.moneymatters.ai.controller;

import com.moneymatters.ai.dto.ExplainRequest;
import com.moneymatters.ai.dto.ExplainResponse;
import com.moneymatters.ai.dto.FollowupRequest;
import com.moneymatters.ai.service.AiPortfolioAnalysisService;
import com.moneymatters.ai.service.AiRateLimiter;
import com.moneymatters.ai.service.NemotronService;
import com.moneymatters.ai.service.PromptBuilder;
import com.moneymatters.common.dto.ApiResponse;
import com.moneymatters.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final NemotronService nemotron;
    private final PromptBuilder prompts;
    private final AiPortfolioAnalysisService portfolioAi;
    private final UserService userService;
    private final AiRateLimiter rateLimiter;

    @PostMapping("/explain-calculator")
    public ResponseEntity<ApiResponse<ExplainResponse>> explainCalculator(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ExplainRequest req) {
        String userId = jwt.getSubject();
        rateLimiter.checkAndConsume(userId);

        String system = prompts.calculatorSystem();
        String user = prompts.buildCalculatorPrompt(req.getType(), req.getInputs(), req.getResult());
        String explanation = nemotron.chat(system, user);

        return ResponseEntity.ok(new ApiResponse<>(true,
            new ExplainResponse(explanation, rateLimiter.remaining(userId)),
            "OK"));
    }

    @PostMapping("/analyse-portfolio")
    public ResponseEntity<ApiResponse<ExplainResponse>> analysePortfolio(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        userService.ensureUserExists(userId, email);
        rateLimiter.checkAndConsume(userId);

        String analysis = portfolioAi.analyse(userId);
        return ResponseEntity.ok(new ApiResponse<>(true,
            new ExplainResponse(analysis, rateLimiter.remaining(userId)),
            "OK"));
    }

    @PostMapping("/followup")
    public ResponseEntity<ApiResponse<ExplainResponse>> followup(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody FollowupRequest req) {
        String userId = jwt.getSubject();
        rateLimiter.checkAndConsume(userId);

        String system = prompts.followupSystem(req.getTopic());
        String user = prompts.buildFollowupPrompt(req.getContext(), req.getQuestion());
        String answer = nemotron.chat(system, user);

        return ResponseEntity.ok(new ApiResponse<>(true,
            new ExplainResponse(answer, rateLimiter.remaining(userId)),
            "OK"));
    }

    @GetMapping("/quota")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> quota(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(new ApiResponse<>(true,
            Map.of("remaining", rateLimiter.remaining(jwt.getSubject())),
            "OK"));
    }
}
