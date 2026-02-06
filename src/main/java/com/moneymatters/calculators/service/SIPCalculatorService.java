package com.moneymatters.calculators.service;

import com.moneymatters.calculators.dto.SIPStepupRequest;
import com.moneymatters.calculators.dto.SIPStepupResponse;

public interface SIPCalculatorService {

    SIPStepupResponse calculateStepupSIP(SIPStepupRequest request);
}
