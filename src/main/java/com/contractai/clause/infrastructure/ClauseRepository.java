package com.contractai.clause.infrastructure;

import com.contractai.clause.domain.Clause;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClauseRepository extends JpaRepository<Clause, Long> {

    List<Clause> findByContractId(Long contractId);

    void deleteByContractId(Long contractId);
}
