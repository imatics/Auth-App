package com.auth.app.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JwtAuthEntryPoint extends BasicAuthenticationEntryPoint {

    @Autowired
    private List<AntPathRequestMatcher> ignoreRoute = null;


    @Override public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {

        Logger.getAnonymousLogger().log(Level.SEVERE,request.getHeaderNames().toString());
        Logger.getAnonymousLogger().log(Level.SEVERE,request.getMethod());
        Logger.getAnonymousLogger().log(Level.SEVERE,request.getPathInfo());
        Logger.getAnonymousLogger().log(Level.SEVERE,request.getAuthType());
        Logger.getAnonymousLogger().log(Level.SEVERE,request.getPathTranslated());
        Logger.getAnonymousLogger().log(Level.SEVERE,request.getRequestURI());
        Logger.getAnonymousLogger().log(Level.SEVERE,request.getContextPath());
        Logger.getAnonymousLogger().log(Level.SEVERE,request.getServletPath());

        for (AntPathRequestMatcher matcher : ignoreRoute){
            if(matcher.matches(request)){
                super.commence(request, response, authException);
                return;
            }
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        response.addHeader("WWW-Authenticate", "Basic realm=$realmName");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.println("HTTP Status 401 - " + authException.getMessage());
    }


    @Override public void afterPropertiesSet() {
        setRealmName("Blst");
        super.afterPropertiesSet();
    }
}
