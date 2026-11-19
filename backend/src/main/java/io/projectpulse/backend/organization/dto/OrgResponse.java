package io.projectpulse.backend.organization.dto;

import io.projectpulse.backend.organization.OrgRole;
import java.time.Instant;

public class OrgResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long ownerId;
    private String ownerName;
    private OrgRole currentUserRole;
    private long memberCount;
    private Instant createdAt;

    public OrgResponse() {}

    public OrgResponse(Long id, String name, String slug, String description, Long ownerId, String ownerName, OrgRole currentUserRole, long memberCount, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.currentUserRole = currentUserRole;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public OrgRole getCurrentUserRole() {
        return currentUserRole;
    }

    public void setCurrentUserRole(OrgRole currentUserRole) {
        this.currentUserRole = currentUserRole;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(long memberCount) {
        this.memberCount = memberCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
