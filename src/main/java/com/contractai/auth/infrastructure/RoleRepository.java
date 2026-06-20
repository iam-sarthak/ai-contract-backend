package com.contractai.auth.infrastructure;

import com.contractai.auth.domain.Role;
import com.contractai.auth.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
