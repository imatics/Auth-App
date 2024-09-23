package com.auth.app.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateUserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
}



