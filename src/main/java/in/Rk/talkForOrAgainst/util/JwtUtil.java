package in.Rk.talkForOrAgainst.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import in.Rk.talkForOrAgainst.entity.Role;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final byte[] SECRET_KEY = "thisisthesecretkeyievercreatedinmydevlopmentcareer".getBytes(StandardCharsets.UTF_8);

    public String generateToken(UserDetails userDetails, Set<Role> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles.stream().map(Role::name).toList()); // Store roles
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String email) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours expiry
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Set<Role> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return Collections.emptySet();

        List<String> roleNames = claims.get("roles", List.class);
        return roleNames != null ? roleNames.stream().map(Role::valueOf).collect(Collectors.toSet()) : Collections.emptySet();
    }

    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration == null || expiration.before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ✅ Used by normal HTTP filters
    public Boolean validateToken(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        Set<Role> roles = extractRoles(token);

        return email != null && email.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && !roles.isEmpty();
    }

    // ✅ New method used in WebSocket HandshakeInterceptor
    public Boolean validateToken(String token) {
        String email = extractEmail(token);
        Set<Role> roles = extractRoles(token);

        return email != null && !isTokenExpired(token) && !roles.isEmpty();
    }
}
