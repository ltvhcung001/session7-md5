package com.quickbite.gateway.filter;

import com.quickbite.common.security.JwtUtil;
import com.quickbite.common.security.SecurityConstants;
import com.quickbite.gateway.config.RouterValidator;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouterValidator routerValidator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouterValidator routerValidator, JwtUtil jwtUtil) {
        super(Config.class);
        this.routerValidator = routerValidator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (routerValidator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                    return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(SecurityConstants.TOKEN_PREFIX.length());

                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, "Unauthorized Access / Expired Token", HttpStatus.UNAUTHORIZED);
                }

                Long userId = jwtUtil.extractUserId(token);
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // Populate headers for downstream microservices
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header(SecurityConstants.USER_ID_HEADER, userId != null ? userId.toString() : "")
                        .header(SecurityConstants.USER_EMAIL_HEADER, email != null ? email : "")
                        .header(SecurityConstants.USER_ROLE_HEADER, role != null ? role : "")
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
        // Configuration properties if needed
    }
}
