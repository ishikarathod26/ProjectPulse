package com.projectpulse.backend.controller;


import com.projectpulse.backend.entity.Project;
import com.projectpulse.backend.service.ProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import  java.util.List;


@RestController 
@RequestMapping ("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    public  ProjectController(ProjectService projectService){
        this.projectService = projectService;
    }

    @GetMapping()
    public List<Project> getAllProjects(){
        return projectService.getAllProjects();
    }
    
    
}
