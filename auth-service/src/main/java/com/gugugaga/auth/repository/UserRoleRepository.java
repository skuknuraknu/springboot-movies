package com.gugugaga.auth.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import com.gugugaga.auth.entity.UserRole;
import com.gugugaga.auth.entity.UserRoleId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    
    List<UserRole> findByUserId(Long userId);
    
    List<UserRole> findByRoleId(Long roleId);
    
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId")
    void deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}