package com.moneymatters.ai.service;

import com.moneymatters.portfolio.dto.PortfolioAnalyticsResponse;
import com.moneymatters.portfolio.entity.Holding;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PromptBuilder {

    private static final int MAX_CONTEXT_CHARS = 3000;

    // ───────── Calculator explainer ─────────

    public String calculatorSystem() {
        return """
            You are MoneyMatters AI, a friendly Indian personal finance advisor.
            Explain financial calculations in simple, conversational English suited to Indian investors.
            Use Indian context: rupees (INR), Indian inflation (~6%), comparable instruments (FD ~7%, Nifty50 ~14%).
            Be encouraging, practical, and highlight key insights the user should act on.
            Always end with one actionable tip. Keep response under 220 words. Avoid emojis.
            """;
    }

    public String buildCalculatorPrompt(String type, Map<String, Object> inputs, Map<String, Object> result) {
        if (type == null) type = "GENERIC";
        String t = type.toUpperCase().replace('-', '_');
        String header = """
            Please explain this %s calculation for the user.

            User Inputs:
            %s

            Calculated Result:
            %s

            """.formatted(t, formatMap(inputs), formatMap(result));

        String guidance = switch (t) {
            case "SIP_STEPUP", "SIP" -> """
                Cover:
                1. The compounding effect visible here and how the annual step-up amplifies it.
                2. How much of the maturity is "free" returns vs invested capital.
                3. What this corpus could mean in real life (down payment, child's education, retirement chunk).
                4. The cost of starting 2 years late at the same rate.
                5. One concrete tip to improve this plan.
                """;
            case "RETIREMENT" -> """
                Cover:
                1. Why the corpus number looks large (inflation effect over the years to retirement).
                2. Whether the required monthly SIP is realistic for an average earner.
                3. The penalty of delaying the start by 5 years.
                4. Roughly how long the corpus would last post-retirement at the assumed withdrawal rate.
                5. One actionable tip on equity/debt mix or SIP discipline.
                """;
            case "LOAN_ANALYZE", "LOAN" -> """
                Cover:
                1. Ratio of total interest to principal — is this loan "expensive"?
                2. Whether the EMI is affordable (rule of thumb: EMI under 40% of monthly income).
                3. Approximate savings if they prepay an extra 10,000 per month.
                4. Whether the interest rate is competitive in the current Indian market.
                5. One actionable tip (prepayment, tenure choice, or refinancing).
                """;
            case "LOAN_COMPARE" -> """
                Cover:
                1. Which loan is cheaper overall and by how much.
                2. Which has a lower EMI burden vs which saves more interest.
                3. Hidden cost considerations (processing fees, prepayment penalties — call them out as things to verify).
                4. Which scenario suits a salaried borrower vs self-employed.
                5. One actionable tip on which to pick and why.
                """;
            case "ASSET_ALLOCATION" -> """
                Cover:
                1. Whether the current allocation matches the target risk profile.
                2. Which asset class is most over- or under-weight and the rebalancing action needed.
                3. Tax implications of rebalancing in India (LTCG on equity above 1L, debt taxation).
                4. Whether the target allocation suits the investor's likely age/horizon.
                5. One actionable rebalancing tip.
                """;
            case "CASHFLOW" -> """
                Cover:
                1. Whether the projected cashflow is sustainable or trending negative.
                2. Savings rate as a percent of income — is it healthy (>20%)?
                3. The biggest leak or opportunity visible in the breakdown.
                4. What an emergency fund of 6 months of expenses would look like here.
                5. One actionable tip to improve monthly surplus.
                """;
            case "SWP" -> """
                Cover:
                1. How long the corpus is expected to last at this withdrawal rate.
                2. Whether the withdrawal rate is safe (4% rule context for Indian inflation).
                3. Tax efficiency of SWP vs dividend income for the user.
                4. What happens if returns underperform by 2%.
                5. One actionable tip on withdrawal sizing or fund choice.
                """;
            case "FD" -> """
                Cover:
                1. The real (inflation-adjusted) return after assuming ~6% inflation.
                2. Post-tax return for someone in the 30% slab.
                3. How this compares to a debt mutual fund or short-duration fund over the same period.
                4. Whether locking in this rate is a good move given current rate cycle.
                5. One actionable tip (ladder strategy, tax-saver FD, or alternative).
                """;
            case "RD" -> """
                Cover:
                1. Effective return after considering monthly contribution timing.
                2. Post-tax return for the 30% slab.
                3. How an RD compares to a SIP in a debt fund or balanced fund for the same goal.
                4. Whether RD discipline is better than SIP for this user type.
                5. One actionable tip on goal alignment or alternative.
                """;
            case "PPF" -> """
                Cover:
                1. The power of EEE tax-free compounding visible here.
                2. How this maturity compares to an equity SIP over 15 years at 12%.
                3. Why PPF still belongs in most portfolios despite lower returns.
                4. The 80C limit context and how to maximise the 1.5L benefit.
                5. One actionable tip (timing of deposit, partial withdrawal rules, extension).
                """;
            default -> """
                Explain the result in simple terms, highlight 2-3 insights specific to the numbers,
                and end with one actionable tip.
                """;
        };

        String custom = inputs != null && inputs.get("customQuestion") != null
            ? "\nThe user has a specific follow-up question: " + inputs.get("customQuestion")
              + "\nAnswer it directly and concisely (max 150 words). Do NOT repeat the original explanation."
            : "";

        return header + guidance + custom;
    }

    // ───────── Portfolio analyser ─────────

    public String portfolioSystem() {
        return """
            You are MoneyMatters AI, an expert Indian stock market analyst and portfolio advisor.
            You have deep knowledge of NSE/BSE stocks, mutual funds, and Indian financial markets.
            Provide honest, data-driven analysis. Be direct about risks and concentration.
            Use Indian financial context (Nifty50 ~14% long-term, FD ~7%, inflation ~6%).
            Structure your response with clear numbered sections matching the user's request.
            Keep total response under 600 words. Avoid emojis. Do not give regulated investment advice
            in absolute terms — frame as observations and considerations.
            """;
    }

    public String buildPortfolioPrompt(
            List<Holding> holdings,
            List<Map<String, Object>> enriched,
            PortfolioAnalyticsResponse analytics) {

        StringBuilder sb = new StringBuilder();
        sb.append("Analyse this Indian stock portfolio:\n\n");

        sb.append("PORTFOLIO SUMMARY:\n");
        if (analytics == null) {
            sb.append("- (Analytics unavailable)\n");
            sb.append("- Holdings Count: ").append(holdings.size()).append("\n\n");
        } else {
        sb.append("- Total Invested: ₹").append(fmt(analytics.getTotalInvested())).append("\n");
        sb.append("- Current Value: ₹").append(fmt(analytics.getCurrentValue())).append("\n");
        sb.append("- Overall Gain/Loss: ₹").append(fmt(analytics.getTotalGain()))
          .append(" (").append(fmt(analytics.getTotalGainPercent())).append("%)\n");
        sb.append("- XIRR: ").append(fmt(analytics.getXirr())).append("%\n");
        sb.append("- CAGR: ").append(fmt(analytics.getCagr())).append("%\n");
        sb.append("- Realized Gain: ₹").append(fmt(analytics.getRealizedGain())).append("\n");
        sb.append("- Unrealized Gain: ₹").append(fmt(analytics.getUnrealizedGain())).append("\n");
        sb.append("- Total Dividends: ₹").append(fmt(analytics.getTotalDividendReceived())).append("\n");
        sb.append("- Holdings Count: ").append(holdings.size()).append("\n");
        if (analytics.getInvestmentDurationYears() != null) {
            sb.append("- Investment Duration: ")
              .append(String.format("%.2f", analytics.getInvestmentDurationYears()))
              .append(" years\n");
        }
        sb.append("\n");
        }

        if (analytics != null && analytics.getAssetWiseAnalytics() != null && !analytics.getAssetWiseAnalytics().isEmpty()) {
            sb.append("ASSET-CLASS ALLOCATION:\n");
            for (var a : analytics.getAssetWiseAnalytics()) {
                sb.append("- ").append(a.getAssetType())
                  .append(": ").append(fmt(a.getAllocation())).append("% ")
                  .append("(₹").append(fmt(a.getCurrentValue())).append(", ")
                  .append(a.getCount()).append(" holdings, ")
                  .append("gain ").append(fmt(a.getGainPercent())).append("%)\n");
            }
            sb.append("\n");
        }

        sb.append("INDIVIDUAL HOLDINGS (with live fundamentals):\n");
        for (int i = 0; i < holdings.size(); i++) {
            Holding h = holdings.get(i);
            Map<String, Object> f = i < enriched.size() ? enriched.get(i) : Map.of();
            sb.append("• ").append(h.getAssetSymbol());
            if (f.get("companyName") != null) sb.append(" (").append(f.get("companyName")).append(")");
            sb.append(" | ").append(h.getAssetType());
            if (f.get("sector") != null) sb.append(" | Sector: ").append(f.get("sector"));
            sb.append("\n");
            sb.append("    Qty: ").append(h.getQuantity())
              .append(" | Avg Buy: ₹").append(h.getAvgBuyPrice())
              .append(" | Current: ₹").append(h.getCurrentPrice())
              .append(" | Value: ₹").append(h.getCurrentValue()).append("\n");
            sb.append("    Gain: ₹").append(h.getUnrealizedGain())
              .append(" (").append(h.getUnrealizedGainPercent()).append("%)\n");
            if (!f.isEmpty()) {
                sb.append("    52W H/L: ₹").append(or(f.get("fiftyTwoWeekHigh"))).append(" / ₹").append(or(f.get("fiftyTwoWeekLow")));
                sb.append(" | 50DMA: ₹").append(or(f.get("fiftyDayAverage")));
                sb.append(" | 200DMA: ₹").append(or(f.get("twoHundredDayAverage"))).append("\n");
                sb.append("    PE: ").append(or(f.get("trailingPE")));
                sb.append(" | P/B: ").append(or(f.get("priceToBook")));
                sb.append(" | ROE: ").append(pct(f.get("roe")));
                sb.append(" | D/E: ").append(or(f.get("debtToEquity"))).append("\n");
                sb.append("    Rev Growth: ").append(pct(f.get("revenueGrowth")));
                sb.append(" | Earnings Growth: ").append(pct(f.get("earningsGrowth")));
                sb.append(" | Beta: ").append(or(f.get("beta"))).append("\n");
                sb.append("    Analyst Rating: ").append(or(f.get("analystRating")));
                sb.append(" | Target: ₹").append(or(f.get("analystTargetPrice")));
                sb.append(" | Div Yield: ").append(pct(f.get("dividendYield"))).append("\n");
            }
        }

        sb.append("""

            Please provide a structured analysis with these sections:
            1. PORTFOLIO HEALTH SCORE (0-100) — one-line reasoning.
            2. TOP 2 PERFORMERS — name them and explain why they are doing well.
            3. TOP 2 RISKS — name them and explain what to watch or consider exiting.
            4. SECTOR / CONCENTRATION ANALYSIS — is the portfolio diversified? Any single-name or single-sector overweight?
            5. RETURN CONTEXT — is the XIRR beating Nifty50 (~14%) and FD (~7%)?
            6. THREE ACTIONABLE RECOMMENDATIONS — specific buy more / hold / trim / exit calls with reasoning.
            7. ONE-LINE VERDICT.
            """);

        return sb.toString();
    }

    // ───────── Follow-up ─────────

    public String followupSystem(String topic) {
        if ("PORTFOLIO".equalsIgnoreCase(topic)) {
            return """
                You are MoneyMatters AI continuing a portfolio discussion.
                The user has already received a full portfolio analysis (provided as context).
                Answer their follow-up question directly and concisely. Reference specific stocks
                or numbers from the prior analysis when relevant. Indian market context applies.
                Maximum 180 words. Avoid emojis. Do not repeat the prior analysis.
                """;
        }
        return """
            You are MoneyMatters AI continuing a calculator explanation.
            The user has already received an explanation (provided as context).
            Answer their follow-up directly. Stay in Indian financial context.
            Maximum 150 words. Avoid emojis. Do not repeat the prior explanation.
            """;
    }

    public String buildFollowupPrompt(String context, String question) {
        String trimmedContext = context == null ? "" : context;
        if (trimmedContext.length() > MAX_CONTEXT_CHARS) {
            trimmedContext = trimmedContext.substring(0, MAX_CONTEXT_CHARS) + "\n[... context truncated ...]";
        }
        return "Prior analysis context:\n---\n" + trimmedContext + "\n---\n\nUser's follow-up question: "
            + (question == null ? "" : question.trim());
    }

    // ───────── helpers ─────────

    private String formatMap(Map<String, Object> m) {
        if (m == null || m.isEmpty()) return "(none)";
        StringBuilder sb = new StringBuilder();
        m.forEach((k, v) -> {
            if (!"customQuestion".equals(k)) {
                sb.append("  - ").append(k).append(": ").append(stringify(v)).append("\n");
            }
        });
        return sb.toString();
    }

    private String stringify(Object v) {
        if (v == null) return "null";
        if (v instanceof List<?> list) return "[" + list.size() + " items]";
        if (v instanceof Map<?, ?> map) return "{" + map.size() + " fields}";
        return v.toString();
    }

    private String fmt(Object v) {
        if (v == null) return "—";
        try { return String.format("%,.2f", Double.parseDouble(v.toString())); }
        catch (Exception e) { return v.toString(); }
    }

    private String or(Object v) { return v == null ? "—" : v.toString(); }

    private String pct(Object v) {
        if (v == null) return "—";
        try {
            double d = Double.parseDouble(v.toString());
            // Yahoo returns ratios like 0.18 for 18%; convert if value looks like a ratio
            if (Math.abs(d) < 5) d = d * 100;
            return String.format("%.2f%%", d);
        } catch (Exception e) { return v.toString(); }
    }
}
