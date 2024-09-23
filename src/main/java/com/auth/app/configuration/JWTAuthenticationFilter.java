package com.auth.app.configuration;

import com.example.demo.identity.domain.User;
import com.example.demo.identity.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;


public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTGenerator tokenGenerator;

    @Autowired
    private UserService customUserDetailsService;

    @Autowired
    private List<AntPathRequestMatcher> ignoreRoute  = new ArrayList<>();


    @Override public void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        AntPathRequestMatcher selectedMatcher = null;
         for(AntPathRequestMatcher matcher : ignoreRoute){
            if(matcher.matches(request)){
                selectedMatcher = matcher;
                HttpServletRequestWrapper newRequest = new HttpServletRequestWrapper(request){
                    private Set<String> headerNameSet =  new HashSet<>();
                    @Override
                    public Enumeration<String> getHeaderNames() {
                        if (headerNameSet.isEmpty()) {
                            headerNameSet = new HashSet<>();
                            Enumeration<String> wrappedHeaderNames = super.getHeaderNames();
                            while (wrappedHeaderNames.hasMoreElements()) {
                                String headerName = wrappedHeaderNames.nextElement();
                                if (!"Authorization".contentEquals(headerName) && !"www-authenticate".contentEquals(headerName)) {
                                    headerNameSet.add(headerName);
                                }
                            }
                        }
                        return super.getHeaderNames();
                    }

                    @Override
                    public Enumeration<String> getHeaders(String name) {
                         if ("Authorization".contentEquals(name)) {
                           return  Collections.emptyEnumeration();
                        } else return super.getHeaders(name);
                    }

                    @Override
                    public String getHeader(String name) {
                         if ("Authorization".contentEquals(name)) {
                            return null;
                        } else return super.getHeader(name);
                }
                };
                filterChain.doFilter(newRequest, response);
                break;
            }
        }

        if(selectedMatcher == null){
            String token = getJWTFromRequest(request);
            if (StringUtils.hasText(token) && tokenGenerator.validateToken(token)) {
                String username = tokenGenerator.getUsernameFromJWT(token);
                User user = customUserDetailsService.getUserByEmail(username);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                assert(user != null);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null,
                        customUserDetailsService.getAuthorities(user.getRoles())
            );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request, response);
        }

    }

    private String getJWTFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else return null;
    }
}