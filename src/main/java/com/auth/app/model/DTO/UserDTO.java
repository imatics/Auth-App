package com.auth.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;



@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserDTO{
        private UUID id;
        private String firstName;
        private String middleName;
        private String lastName;
        private String email;
        private Boolean isSystemAdmin;
        private ZonedDateTime dateCreated;
        private ZonedDateTime dateModified;
        private String profileImage;
        private String profileImageThumb;
        private String phone;
        private Set<RoleDTO> roles;
        private boolean isActive;
        private String reasonForDeactivation;
}