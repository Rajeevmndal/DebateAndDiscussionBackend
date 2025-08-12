package in.Rk.talkForOrAgainst.io;

import in.Rk.talkForOrAgainst.entity.Role;
import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String email;
    private String token;
    private Set<Role> roles; // 🔥 Include roles in the response
}