package com.projectpulse.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.projectpulse.backend.entity.User;
public interface UserRepository  extends  JpaRepository<User , Long>{
    boolean existsByEmail(String Email);
}
