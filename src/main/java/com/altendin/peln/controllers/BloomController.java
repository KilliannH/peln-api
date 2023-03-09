package com.altendin.peln.controllers;

import com.altendin.peln.repositories.UserRepository;
import com.altendin.peln.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class BloomController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
        // @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    List<User> all() {
        return userRepository.findAll();
    }
}
