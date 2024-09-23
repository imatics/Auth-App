package com.auth.app.DAO;

import com.auth.app.domain.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PrivilegeRepository extends JpaRepository<Privilege, UUID>{

@Query("SELECT p FROM Privilege p WHERE p.name = :name")
public Privilege findByName(@Param("name")String name);
        }