package com.projectpulse.backend.controller;



import com.projectpulse.backend.service.InfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController 
public class InfoController {
    
private final InfoService infoService;

public InfoController(InfoService infoService){
    this.infoService = infoService;
}
    @GetMapping("/api/info")
    public Map<String , String> getInfo(){
        return infoService.getInfo();
    }
}
