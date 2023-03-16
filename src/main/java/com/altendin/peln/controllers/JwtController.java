package com.altendin.peln.controllers;

import com.altendin.peln.payloads.errors.GenericError;
import com.altendin.peln.utils.JwtUtil;
import com.altendin.peln.jwt.UserDetailsImpl;
import com.altendin.peln.models.ERole;
import com.altendin.peln.models.Role;
import com.altendin.peln.models.User;
import com.altendin.peln.payloads.LoginRequest;
import com.altendin.peln.payloads.MessageResponse;
import com.altendin.peln.payloads.SignupRequest;
import com.altendin.peln.payloads.LoginResponse;
import com.altendin.peln.repositories.RoleRepository;
import com.altendin.peln.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
public class JwtController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtil jwtUtil;

    @Operation(summary = "Login", description = "Login to the API", tags = "Post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token to be used for secured endpoints",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized request",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationCredentialsNotFoundException.class))})
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Optional<User> optUser = userRepository.findByEmail(loginRequest.getEmail());
        User connUser;
        if(optUser.isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Bad credentials");
        }
        connUser = optUser.get();

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(connUser.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwtToken = jwtUtil.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .body(new LoginResponse(jwtToken, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
    }

    @Operation(summary = "Signup", description = "Signup a new user", tags = "Post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = GenericError.class))})
    })
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new GenericError("/signup", "Bad Request", "Username is already in use", 400));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new GenericError("/signup", "Bad Request", "Email is already in use", 400));
        }

        // Create new user
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Optional<Role> userRoleOpt = roleRepository.findByName(ERole.ROLE_USER);
            if(userRoleOpt.isEmpty()) {
                return ResponseEntity.status(500).body(new GenericError("/signup", "Server error", "Role not found", 500));
            }
            Role userRole = userRoleOpt.get();
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin" -> {
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(adminRole);
                    }
                    case "mod" -> {
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(modRole);
                    }
                    default -> {
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(userRole);
                    }
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.status(201).body(new MessageResponse("User registered successfully"));
    }
}