package in.Rk.talkForOrAgainst.io;

import in.Rk.talkForOrAgainst.entity.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {
     private String email;
     private String password;
     private Role role; // Optional field if login requires role selection
}