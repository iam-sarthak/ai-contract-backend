package com.contractai.contract.infrastructure;

import com.contractai.contract.domain.Contract;
import com.contractai.contract.domain.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);

    Optional<Contract> findByIdAndUploadedById(Long id, Long userId);
}
