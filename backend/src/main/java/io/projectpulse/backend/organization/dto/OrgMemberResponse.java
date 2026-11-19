package io.projectpulse.backend.organization.dto;

import io.projectpulse.backend.organization.MemberStatus;
import io.projectpulse.backend.organization.OrgRole;
import java.time.Instant;

public class OrgMemberResponse {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private OrgRole role;
    private MemberStatus status;
    private Instant joinedAt;

    public OrgMemberResponse() {}

    public OrgMemberResponse(Long id, Long userId, String email, String fullName, OrgRole role, MemberStatus status, Instant joinedAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public OrgRole getRole() {
        return role;
    }

    public void setRole(OrgRole role) {
        this.role = role;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
