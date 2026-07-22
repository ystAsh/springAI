/*
 * =============================================================================
 * 클래스명 : AppUser
 * =============================================================================
 * 목적
 *  - 사용자 정보를 저장하는 Entity
 */
package com.example.springai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String organizationId;

    @Column(nullable = false)
    private String departmentId;

    @Column(nullable = false)
    private Integer securityLevel;

    public AppUser() {
    }

    public AppUser(
            String username,
            String password,
            String role,
            String organizationId,
            String departmentId,
            Integer securityLevel) {

        this.username = username;
        this.password = password;
        this.role = role;
        this.organizationId = organizationId;
        this.departmentId = departmentId;
        this.securityLevel = securityLevel;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public Integer getSecurityLevel() {
        return securityLevel;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public void setSecurityLevel(Integer securityLevel) {
        this.securityLevel = securityLevel;
    }
}