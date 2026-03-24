package com.ShreeNagariCRM.Repository;

import com.ShreeNagariCRM.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    @Query("""
        SELECT u FROM User u 
        WHERE u.email = :identifier OR u.phone = :identifier
    """)
    Optional<User> findByEmailOrPhone(String identifier);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
