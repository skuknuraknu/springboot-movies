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

}