package com.contractai.risk.infrastructure;

import com.contractai.risk.domain.Risk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRepository extends JpaRepository<Risk, Long> {

    List<Risk> findByContractId(Long contractId);

    void deleteByContractId(Long contractId);
}
