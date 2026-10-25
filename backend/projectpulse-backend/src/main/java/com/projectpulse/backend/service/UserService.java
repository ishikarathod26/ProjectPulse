package com.projectpulse.backend.service;

import com.projectpulse.backend.entity.User;
import com.projectpulse.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service 
public class UserService {
   
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(
        UserRepository userRepository, PasswordEncoder passwordEncoder){
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }

    public User createUser(String name, String email , String rawPassword){

        
        
     if(userRepository.existsByEmail(email)){
        throw new IllegalArgumentException("Email is already registered");
     }        
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

}