package com.contractai.clause.web;

import com.contractai.clause.application.ClauseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contracts/{id}/clauses")
@RequiredArgsConstructor
public class ClauseController {

    private final ClauseService clauseService;

    @GetMapping
    public List<ClauseService.ClauseResponse> getClauses(@PathVariable Long id) {
        return clauseService.getClauses(id);
    }
}
