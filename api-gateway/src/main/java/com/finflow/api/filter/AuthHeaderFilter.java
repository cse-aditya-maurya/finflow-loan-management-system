package com.finflow.api.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Log the Authorization header for debugging
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            System.out.println("🔑 Authorization header present: " + authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
        } else {
            System.out.println("⚠️ Authorization header missing in request to: " + request.getPath());
        }
        
        // Continue with the request
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}
