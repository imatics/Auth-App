package com.auth.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Token{
    private String token;
    private String refreshToken;
    private ProfileDTO profileDTO;
    private Long expiry;
}
