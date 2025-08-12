package in.Rk.talkForOrAgainst.io;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "New Password required")
    private String newPassword;
    @NotBlank(message = "Otp required")
    private String otp;
    @NotBlank(message = "email required")
    private String email;
}
