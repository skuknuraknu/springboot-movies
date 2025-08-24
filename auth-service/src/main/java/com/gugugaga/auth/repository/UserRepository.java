package com.gugugaga.auth.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gugugaga.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    <S extends User> S save(S entity);
    
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase( String username );
    Optional<User> findByEmailIgnoreCase( String email );

    // find only user that is active
    Optional<User> findByIdAndIsActiveTrue(Long id);
    Optional<User> findByUsernameIgnoreCaseAndIsActiveTrue(String username);
    // Add this method with JOIN FETCH to load roles eagerly
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.username = :username AND u.isActive = true")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
}