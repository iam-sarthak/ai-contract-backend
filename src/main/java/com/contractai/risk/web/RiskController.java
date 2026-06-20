package com.contractai.risk.web;

import com.contractai.risk.application.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contracts/{id}/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @GetMapping
    public RiskService.RiskAnalysisResponse getRisks(@PathVariable Long id) {
        return riskService.getRisks(id);
    }
}
