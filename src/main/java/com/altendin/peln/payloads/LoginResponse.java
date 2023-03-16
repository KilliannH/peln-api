package com.altendin.peln.payloads;

import java.io.Serializable;
import java.util.List;

public class LoginResponse implements Serializable {

    public String token;
    public Long id;
    public String username;
    public String email;
    public List<String> roles;


    public LoginResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
