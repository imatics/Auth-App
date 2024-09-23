package com.auth.app.DAO;

import com.auth.app.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;



@Repository
public interface RoleRepository extends JpaRepository<Role, UUID>{

@Query("SELECT r FROM Role r WHERE r.name = :name")
public Role findByName(@Param("name") String name);


}