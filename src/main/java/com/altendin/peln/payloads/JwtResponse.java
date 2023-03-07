package com.altendin.peln.payloads;

import com.altendin.peln.models.User;

import java.io.Serializable;
import java.util.List;

public class JwtResponse implements Serializable {

    public String token;
    public Long id;
    public String username;
    public String email;
    public List<String> roles;


    public JwtResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
