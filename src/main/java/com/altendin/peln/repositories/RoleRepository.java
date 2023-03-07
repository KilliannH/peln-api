package com.altendin.peln.repositories;

import com.altendin.peln.models.ERole;
import com.altendin.peln.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
