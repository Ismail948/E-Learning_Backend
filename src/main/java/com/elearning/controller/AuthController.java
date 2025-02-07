package com.elearning.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.elearning.models.User;
import com.elearning.repositories.UserRepository;
import com.elearning.response_request.ApiResponse;
import com.elearning.response_request.LoginRequest;
import com.elearning.response_request.LoginResponse;
import com.elearning.response_request.OtpVerificationRequest;
import com.elearning.security.JwtService;
import com.elearning.services.CustomUserDetailsService;
import com.elearning.services.RegistrationService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(RegistrationService registrationService, UserRepository userRepository, JwtService jwtService,
                          AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService) {
        this.registrationService = registrationService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
    	return ResponseEntity.status(HttpStatus.OK).body("hello");
    }

//    @PostMapping("/register")
//    public ResponseEntity<ApiResponse> registerUser(@RequestBody User user) {
//        try {
//            registrationService.registerUser(user);
//            return ResponseEntity.ok(new ApiResponse(true, "User registered. Check your email for OTP."));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ApiResponse(false, "Registration failed: " + e.getMessage()));
//        }
//    }
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
        	if(!registrationService.validUser(user)) {
        		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: Email or Username Already Exist");
        	}
            registrationService.registerUser(user);
            return ResponseEntity.ok("User registered. Check your email for OTP.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody OtpVerificationRequest request) {
        boolean verified = registrationService.verifyOtp(request.getEmail(), request.getOtp());
        if (verified) {
            return ResponseEntity.ok(new ApiResponse(true, "Email verified!"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid or expired OTP."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        try {
            // Load the user from the database based on the provided email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            // Check if the provided password matches the stored password
            if (password.equals(user.getPassword())) {
                // If the passwords match, generate the JWT token
                String token = jwtService.generateToken(user.getEmail());
                String username=user.getUsername();
                // Return the success response with the token
                LoginResponse response = new LoginResponse(true, token,username);
                return ResponseEntity.ok(response);
            }

            // If authentication fails
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid email or password."));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Login failed: " + e.getMessage()));
        }
    }

}
