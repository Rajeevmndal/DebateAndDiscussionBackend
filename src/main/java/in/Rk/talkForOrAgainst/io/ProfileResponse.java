package in.Rk.talkForOrAgainst.io;

import lombok.*;
import in.Rk.talkForOrAgainst.entity.Role; // Import Role Enum
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponse {
    private String userId;
    private String name;
    private String email;
    private Boolean isAccountVerified;
    private Set<Role> roles; // ✅ Changed to Set<Role> to support multiple roles
}