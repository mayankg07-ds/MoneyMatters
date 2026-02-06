package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.RetirementPlanRequest;
import com.moneymatters.calculators.dto.RetirementPlanResponse;

public interface RetirementPlannerService {
    RetirementPlanResponse calculateRetirementPlan(RetirementPlanRequest request);
}

