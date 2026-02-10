package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.SWPRequest;
import com.moneymatters.calculators.dto.SWPResponse;

public interface SWPCalculatorService {
    SWPResponse calculateSWP(SWPRequest request);
}
