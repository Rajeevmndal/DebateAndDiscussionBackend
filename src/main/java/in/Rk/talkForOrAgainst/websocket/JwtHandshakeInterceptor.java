package in.Rk.talkForOrAgainst.websocket;

import in.Rk.talkForOrAgainst.util.JwtUtil;
import in.Rk.talkForOrAgainst.entity.Role;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        System.out.println(">>> JwtHandshakeInterceptor triggered");
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String jwt = null;

            // Check Header
//            String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                jwt = authHeader.substring(7);
//            }

            // Check Cookies
            if (jwt == null) {
                Cookie[] cookies = servletRequest.getServletRequest().getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("jwt".equals(cookie.getName())) {
                            jwt = cookie.getValue();
                            break;
                        }
                    }
                }
            }

            if (jwt == null) {
                System.out.println("WebSocket JWT not found in cookie ");
            }

            // Check Query Parameters
            if (jwt == null) {
                String query = servletRequest.getServletRequest().getQueryString();
                if (query != null && query.contains("token=")) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("token=")) {
                            jwt = param.substring(6); // skip 'token='
                            break;
                        }
                    }
                }
            }


            if (jwt != null && jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.extractEmail(jwt);
                Set<Role> roles = jwtUtil.extractRoles(jwt);

                attributes.put("email", email);
                attributes.put("roles", roles);
                return true;
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
