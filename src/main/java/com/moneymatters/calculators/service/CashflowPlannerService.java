package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.CashflowRequest;
import com.moneymatters.calculators.dto.CashflowResponse;

public interface CashflowPlannerService {
    CashflowResponse projectCashflow(CashflowRequest request);
}

