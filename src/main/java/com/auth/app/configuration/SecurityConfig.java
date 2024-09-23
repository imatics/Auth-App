package com.auth.app.configuration;



import com.auth.app.DAO.UserRepository;
import com.auth.app.domain.User;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;


@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class SecurityConfig {

    @Value("jwt.public.key")
    private RSAPublicKey rsaPublicKey = null;

    @Value("jwt.private.key")
    private RSAPrivateKey rsaPrivateKey = null;

    @Value("springdoc.api-docs.path")
    private String restApiDocPath = null;

    @Value("springdoc.swagger-ui.path")
    private String swaggerPath = null;

    @Autowired
    private UserRepository userRepository;

    private final AntPathRequestMatcher[] ignoreRoute =  new AntPathRequestMatcher[]{
            new AntPathRequestMatcher("/api/users/login", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/users/requestOtp/**", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/users/verifyEmail/**", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/users/resetPassword/**", HttpMethod.POST.name()),
            new AntPathRequestMatcher("/api/users/createUser/**", HttpMethod.POST.name()),
    };

    @Bean
    public UserDetailsService userDetailsService() {
       return new UserDetailsService(){
           @Override
           public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
               User user =  userRepository.getUserByEmailOrPhone(username).orElseThrow();
               return user.toUserDetails();
           }
       };
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var newHttp = http.cors(AbstractHttpConfigurer::disable).csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .exceptionHandling ( exceptions ->
                exceptions
                    .authenticationEntryPoint(new JwtAuthEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            );
        newHttp.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
            authorizationManagerRequestMatcherRegistry.requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/client/**")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher(String.format("%s/**", restApiDocPath))).permitAll()
                    .requestMatchers(new AntPathRequestMatcher(String.format("%s/**", swaggerPath))).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/swagger/**", HttpMethod.GET.name())).permitAll();
            for (AntPathRequestMatcher route : ignoreRoute) {
                authorizationManagerRequestMatcherRegistry.requestMatchers(route).permitAll();
            }
        });

        newHttp.httpBasic(Customizer.withDefaults())
            .oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> httpSecurityOAuth2ResourceServerConfigurer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return newHttp.build();
    }

    // Used by JwtAuthenticationProvider to generate JWT tokens
    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaPublicKey).privateKey(rsaPrivateKey).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }



    // Used by JwtAuthenticationProvider to decode and validate JWT tokens
    @Bean
    public JwtDecoder jwtDecoder(){
        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
    }



    // Extract authorities from the roles claim
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsFilter corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }


    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider,authProvider);
    }


    @Bean
    public AntPathRequestMatcher[] getIgnorePaths(){
        return ignoreRoute;
    }


}