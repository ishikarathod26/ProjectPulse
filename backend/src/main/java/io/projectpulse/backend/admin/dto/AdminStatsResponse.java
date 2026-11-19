package io.projectpulse.backend.admin.dto;

public class AdminStatsResponse {
    private long totalUsers;
    private long totalOrganizations;
    private long totalProjects;
    private long totalActivities;
    private long totalAnchoredRecords;

    public AdminStatsResponse() {}

    public AdminStatsResponse(long totalUsers, long totalOrganizations, long totalProjects, long totalActivities, long totalAnchoredRecords) {
        this.totalUsers = totalUsers;
        this.totalOrganizations = totalOrganizations;
        this.totalProjects = totalProjects;
        this.totalActivities = totalActivities;
        this.totalAnchoredRecords = totalAnchoredRecords;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalOrganizations() {
        return totalOrganizations;
    }

    public void setTotalOrganizations(long totalOrganizations) {
        this.totalOrganizations = totalOrganizations;
    }

    public long getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(long totalProjects) {
        this.totalProjects = totalProjects;
    }

    public long getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(long totalActivities) {
        this.totalActivities = totalActivities;
    }

    public long getTotalAnchoredRecords() {
        return totalAnchoredRecords;
    }

    public void setTotalAnchoredRecords(long totalAnchoredRecords) {
        this.totalAnchoredRecords = totalAnchoredRecords;
    }
}
