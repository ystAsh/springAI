/*
 * =============================================================================
 * 클래스명 : AppUserRepository
 * =============================================================================
 * 목적
 *  - 사용자 정보를 조회하는 Repository
 */
package com.example.springai.repository;

import com.example.springai.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

}