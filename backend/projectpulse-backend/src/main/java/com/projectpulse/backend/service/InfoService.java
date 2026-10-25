package com.projectpulse.backend.service;
import org.springframework.stereotype.Service;
import java.util.Map;



@Service 
public class InfoService {
    
    public Map<String , String> getInfo()
    {
        return Map.of(
            "project","projectpulse",
            "status","RUNNING",
            "message", "Information from Service layer"
        
        );
    }
}
