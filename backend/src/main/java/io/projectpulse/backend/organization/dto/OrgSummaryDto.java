package io.projectpulse.backend.organization.dto;

import io.projectpulse.backend.organization.OrgRole;

public class OrgSummaryDto {
    private Long id;
    private String name;
    private String slug;
    private OrgRole userRole;

    public OrgSummaryDto() {}

    public OrgSummaryDto(Long id, String name, String slug, OrgRole userRole) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.userRole = userRole;
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

    public OrgRole getUserRole() {
        return userRole;
    }

    public void setUserRole(OrgRole userRole) {
        this.userRole = userRole;
    }
}
