package com.contractai.contract.web;

import com.contractai.common.PageResponse;
import com.contractai.contract.application.ContractService;
import com.contractai.contract.domain.ContractStatus;
import com.contractai.contract.web.dto.CompareContractsRequest;
import com.contractai.contract.web.dto.CompareContractsResponse;
import com.contractai.contract.web.dto.ContractResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ContractResponse upload(@RequestParam("file") MultipartFile file) {
        return contractService.upload(file);
    }

    @GetMapping
    public PageResponse<ContractResponse> list(
            @RequestParam(required = false) ContractStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return contractService.list(status, pageable);
    }

    @GetMapping("/{id}")
    public ContractResponse getById(@PathVariable Long id) {
        return contractService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contractService.delete(id);
    }

    @PostMapping("/compare")
    public CompareContractsResponse compare(@Valid @RequestBody CompareContractsRequest request) {
        return contractService.compare(request);
    }
}
