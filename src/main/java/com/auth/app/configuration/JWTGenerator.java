package com.auth.app.configuration;

import com.google.common.collect.Iterables;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

//import java.security.KeyPair;
@Component
public class JWTGenerator {
    private Key key;
    JWTGenerator(){
        key  = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    @Autowired
    JwtDecoder jwtDecoder;

    @Autowired
    JwtEncoder jwtEncoder;

    public String generateToken(Authentication authentication, String subject, Map<String, Object> params) {
        String scope = authentication.getAuthorities().stream().map((GrantedAuthority::getAuthority)).collect(Collectors.joining(" "));
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + SecurityConstants.JWT_EXPIRATION);
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
            .subject(subject)
            .issuedAt(new Date().toInstant())
            .claim("roles", scope)
            .issuer("www.rava.com")
            .expiresAt(expireDate.toInstant());
        params.forEach(claims::claim);
        return jwtEncoder.encode(JwtEncoderParameters.from(claims.build())).getTokenValue();
    }

    public String getUsernameFromJWT(String token) {
        return Iterables.getLast(Arrays.stream(jwtDecoder.decode(token).getSubject().split(",")).toList());
    }
    public String[] getRoles(String token) {
        return jwtDecoder.decode(token).getClaimAsString("scope").split(" ");
    }

    public boolean validateToken(String token) {
         try {
            return Objects.requireNonNull(jwtDecoder.decode(token).getExpiresAt()).isAfter(Instant.now());
        } catch (Exception exception) {
           // TODO log exceptions
            return false;
        }
    }

}
