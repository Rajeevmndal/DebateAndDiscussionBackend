package in.Rk.talkForOrAgainst.controller;

import in.Rk.talkForOrAgainst.entity.Role;
import in.Rk.talkForOrAgainst.io.AuthRequest;
import in.Rk.talkForOrAgainst.io.AuthResponse;
import in.Rk.talkForOrAgainst.io.ResetPasswordRequest;
import in.Rk.talkForOrAgainst.service.AppUserDetailsService;
import in.Rk.talkForOrAgainst.service.ProfileService;
import in.Rk.talkForOrAgainst.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticate(request.getEmail(), request.getPassword());
            final UserDetails userDetails = appUserDetailsService.loadUserByUsername(request.getEmail());

            // 🔥 Extract roles from UserDetails
            Set<Role> roles = userDetails.getAuthorities().stream()
                    .map(authority -> Role.valueOf(authority.getAuthority().replace("ROLE_", "")))
                    .collect(Collectors.toSet());

            // 🔥 Generate JWT with roles included
            final String jwtToken = jwtUtil.generateToken(userDetails, roles);

            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jwtToken, roles));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", true, "message", "Email or Password incorrect"));
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", true, "message", "User Account disabled"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", true, "message", "Authentication Failed"));
        }
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return ResponseEntity.ok(email != null);
    }

    @PostMapping("/send-reset-opt")
    public void sendResetOtp(@RequestParam String email) {
        profileService.sendResetOtp(email);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        profileService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
    }

    @PostMapping("/send-otp")
    public void sendVerifyOtp(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        profileService.sendOtp(email);
    }

    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody Map<String, Object> request, @CurrentSecurityContext(expression = "authentication?.name") String email) {
        String otp = (String) request.get("otp");
        if (otp == null || otp.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing details");
        }
        profileService.verifyOtp(email, otp);
    }

    // 🔥 **ROLE-BASED ACCESS CONTROL EXAMPLES**
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/manage-users")
    public ResponseEntity<String> manageUsers() {
        return ResponseEntity.ok("Admin can manage users.");
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @PostMapping("/moderator/moderate-debate")
    public ResponseEntity<String> moderateDebate() {
        return ResponseEntity.ok("Moderators can moderate discussions.");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/user/join-debate")
    public ResponseEntity<String> joinDebate() {
        return ResponseEntity.ok("User has joined the debate.");
    }
}