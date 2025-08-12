package in.Rk.talkForOrAgainst.controller;

import in.Rk.talkForOrAgainst.io.ProfileRequest;
import in.Rk.talkForOrAgainst.io.ProfileResponse;
import in.Rk.talkForOrAgainst.service.EmailService;
import in.Rk.talkForOrAgainst.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final EmailService emailService;

    // 🔥 **Allow only authenticated users to register**
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()") // Ensure registration happens in authenticated sessions
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.createProfile(request);
        emailService.sendWelcomeEmail(response.getEmail(), response.getName());
        return response;
    }

    // 🔥 **Restrict profile access based on roles**
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')") // Ensure only valid roles access profiles
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return profileService.getProfile(email);
    }
}