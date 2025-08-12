package in.Rk.talkForOrAgainst.io;

import jakarta.validation.constraints.*;
import lombok.*;
import in.Rk.talkForOrAgainst.entity.Role; // Import Role Enum

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileRequest {
   @NotBlank(message = "Name should not be empty")
   private String name;

   @Email(message = "Enter a valid email")
   @NotNull(message = "Email should not be empty")
   private String email;

   @Size(min = 6, message = "Password must be at least six characters")
   private String password;

   @NotNull(message = "Role must be selected")
   private Role role; // Include role selection during registration
}