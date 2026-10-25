package com.projectpulse.backend.repository;

import com.projectpulse.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project , Long>{
    
}