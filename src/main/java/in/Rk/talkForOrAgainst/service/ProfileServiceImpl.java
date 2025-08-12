package in.Rk.talkForOrAgainst.service;

import in.Rk.talkForOrAgainst.entity.Role;
import in.Rk.talkForOrAgainst.entity.UserEntity;
import in.Rk.talkForOrAgainst.io.ProfileRequest;
import in.Rk.talkForOrAgainst.io.ProfileResponse;
import in.Rk.talkForOrAgainst.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);

        if (!userRepository.existsByEmail(newProfile.getEmail())) {
            userRepository.save(newProfile);
            return convertToProfileResponse(newProfile);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity userExisting = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return convertToProfileResponse(userExisting);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity userExisting = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000); // 15 mins expiry

        userExisting.setResetOtp(otp);
        userExisting.setResetOtpExpireAt(expiryTime);
        userRepository.save(userExisting);

        emailService.sendResetOtpEmail(userExisting.getEmail(), otp);
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity userExisting = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (!otp.equals(userExisting.getResetOtp()) || userExisting.getResetOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        userExisting.setPassword(passwordEncoder.encode(newPassword));
        userExisting.setResetOtp(null);
        userExisting.setResetOtpExpireAt(null);
        userRepository.save(userExisting);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + email));

        if (Boolean.TRUE.equals(existingUser.getIsAccountVerified())) {
            return;
        }

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 hours expiry

        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);
        userRepository.save(existingUser);

        emailService.sendOtpEmail(existingUser.getEmail(), otp);
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + email));

        if (!otp.equals(existingUser.getVerifyOtp()) || existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpireAt(null);
        userRepository.save(existingUser);
    }

    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .roles(newProfile.getRoles()) // 🔥 Include roles in response
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtp(null)
                .verifyOtpExpireAt(null)
                .verifyOtp(null)
                .resetOtpExpireAt(null)
                .build();

        // 🔥 Assign roles during registration
        if (request.getRole() == null) {
            user.setRoles(Set.of(Role.USER)); // Default role
        } else {
            user.setRoles(Set.of(request.getRole())); // Assign provided role
        }

        return user;
    }
}