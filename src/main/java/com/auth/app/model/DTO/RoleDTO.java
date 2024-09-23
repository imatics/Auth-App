
package com.auth.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {
    private UUID id;
    private UUID tenantId;
    private String name;
    private Set<PrivilegeDTO> privileges;

}