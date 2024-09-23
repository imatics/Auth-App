package com.auth.app.DAO;

import com.auth.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    public Optional<User> getUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.phone = :email")
    public Optional<User> getUserByEmailOrPhone(@Param("email") String email);

}






