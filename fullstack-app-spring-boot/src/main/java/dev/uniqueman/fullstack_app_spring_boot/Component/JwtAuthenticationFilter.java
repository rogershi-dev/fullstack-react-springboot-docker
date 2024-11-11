package dev.uniqueman.fullstack_app_spring_boot.Component;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.uniqueman.fullstack_app_spring_boot.Entity.ApiError;
import dev.uniqueman.fullstack_app_spring_boot.Entity.ErrorCode;
import dev.uniqueman.fullstack_app_spring_boot.Exception.JwtAuthenticationException;
import dev.uniqueman.fullstack_app_spring_boot.Service.CustomUserDetailsService;
import dev.uniqueman.fullstack_app_spring_boot.Service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtilization jwtUtilization;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private List<String> publicUrls;
    
    public void setPublicUrls(List<String> publicUrls) {
        this.publicUrls = publicUrls;
    }

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain chain ) throws ServletException, IOException{

        String requestPath = request.getRequestURI();
        if(isFromPublicUrl(requestPath)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String header = request.getHeader("Authorization");
            String token = null;
            String username = null;

            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);

                if (jwtUtilization.validateToken(token)) {
                    username = jwtUtilization.getUsernameFromToken(token);
                } else {
                    throw new JwtAuthenticationException("Invalid or expired token");
                }
            } else {
                throw new JwtAuthenticationException("Token is missing");
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null ) {
                if (tokenService.isTokenBlacklisted(token)) {
                    throw new JwtAuthenticationException("Token has been blacklisted");
                }

                if (tokenService.isTokenActive(username, token)) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);    
                    request.setAttribute("JWT_TOKEN", token);

                } else { 
                    throw new JwtAuthenticationException("Token is inactive");              
                }
            } 

            chain.doFilter(request, response);
       } catch (JwtAuthenticationException ex) {
            logger.warn("Error Code: {}, Error Message: {}", ErrorCode.JWT_AUTHENTICATION_FAILED, ex.getMessage(), ex);

            ApiError apiError = new ApiError(ErrorCode.JWT_AUTHENTICATION_FAILED, ex.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writeValue(response.getOutputStream(), apiError);

       }
    }  
    
    private boolean isFromPublicUrl(String requestPath) {
        for (String pattern : publicUrls) {
            if (antPathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

}
