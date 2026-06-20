package com.contractai.contract.infrastructure;

import com.contractai.contract.domain.ContractComparison;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractComparisonRepository extends JpaRepository<ContractComparison, Long> {
}
