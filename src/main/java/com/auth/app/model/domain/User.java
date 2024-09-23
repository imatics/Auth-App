package com.auth.app.domain;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(access = AccessLevel.PUBLIC, setterPrefix = "set")
@Table(name = "_user")
public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;
        private String firstName;
        private String middleName;
        private String lastName;
        private String email;
        private String passwordHash;
        private Boolean isDeleted;
        private Boolean isSystemAdmin;
        private String profileImageThumb;
        private String phone;
        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "users_roles",
                joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
                inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
       private Set<Role> roles;
       private boolean isActive;
       private String reasonForDeactivation;
        private ZonedDateTime dateCreated;
        private ZonedDateTime dateDeleted;
        private ZonedDateTime dateModified;


        public UserDetails toUserDetails() {
                return new UserDetails() {

                        @Override
                        public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                                return User.this.roles.stream().map(it -> new SimpleGrantedAuthority(it.getName()) ).toList();

                        }

                        @Override public String getPassword() {
                                return User.this.passwordHash;
                        }

                        @Override public String getUsername() {
                                return User.this.email;
                        }

                        @Override public boolean isAccountNonExpired() {
                                return true;
                        }

                        @Override public boolean isAccountNonLocked() {
                                return true;
                        }

                        @Override public boolean isCredentialsNonExpired() {
                                return true;
                        }

                        @Override public boolean isEnabled() {
                                return true;
                        }
        };


}

}














