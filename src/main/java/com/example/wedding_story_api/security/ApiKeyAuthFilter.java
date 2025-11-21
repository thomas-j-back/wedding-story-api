package com.example.wedding_story_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;

public class ApiKeyAuthFilter extends BasicAuthenticationFilter {

    private final String expectedApiKey;

    public ApiKeyAuthFilter(String expectedApiKey) {
        super(authentication -> authentication); // no-op auth manager
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        System.out.println(">>> ApiKeyAuthFilter HIT: " + request.getMethod() + " " + request.getRequestURI());

        String apiKey = request.getHeader("X-Api-Key");

        if (apiKey != null && apiKey.equals(expectedApiKey)) {
            // Mark user as authenticated with a simple principal
            var auth = new UsernamePasswordAuthenticationToken(
                    "api-key-user", null, List.of()  // no roles yet
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            Authentication a = SecurityContextHolder.getContext().getAuthentication();

            System.out.println("AUTH AFTER FILTER = " + a);
        }

        chain.doFilter(request, response);
    }
}