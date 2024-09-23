package com.auth.app.service;



import com.auth.app.DAO.PrivilegeRepository;
import com.auth.app.DAO.RoleRepository;
import com.auth.app.DAO.UserRepository;
import com.auth.app.DTO.*;
import com.auth.app.common.AppUtils;
import com.auth.app.configuration.JWTGenerator;
import com.auth.app.configuration.SecurityConstants;
import com.auth.app.domain.Privilege;
import com.auth.app.domain.Role;
import com.auth.app.domain.User;
import com.musala.drone.dispatch.dronedispatch.exception.ClientException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.auth.app.common.AppUtils.generateID;


@Component
@Service
@Transactional
public class UserService implements UserDetailsService, UserDetailsPasswordService, ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private ModelMapper mapper;


    static final Logger LOGGER = Logger.getLogger(UserService.class.getName());


    public boolean createUser(CreateUserDTO dto , boolean isAdmin) {
        String roleString =  isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(roleString));
        User tempUser =
                new User();
                        tempUser.setFirstName(dto.getFirstName());
                        tempUser.setLastName(dto.getLastName());
                        tempUser.setEmail(dto.getEmail());
                        tempUser.setDateModified(ZonedDateTime.now());
                        tempUser.setActive(false);
                        tempUser.setIsSystemAdmin(false);
                        tempUser.setIsDeleted(false);
                        tempUser.setPhone(dto.getPhone());
                        tempUser.setRoles(roles);
        User user = repository.save(tempUser);
        return true;
    }

    public Token getToken(LoginDTO loginModel){
        User user = repository.getUserByEmailOrPhone(loginModel.getUsername()).orElseThrow();
        Authentication authentication = authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken(loginModel.getUsername(), loginModel.getPassword()));

        String token = jwtGenerator.generateToken(authentication,
            String.format("%s,%s", user.getId(), user.getPhone()), new HashMap<>());
        return new Token(token, "", ProfileDTO.fromUserModel(user), SecurityConstants.JWT_EXPIRATION);

    }

    public Token refreshToken(String refreshToken) {
        return null;
    }

    public List<ProfileDTO> getUsers() {
        return repository.findAll().stream().map(ProfileDTO::fromUserModel).toList();
    }

    public User getUserByEmail(String email) {
        return repository.getUserByEmailOrPhone(email).orElseThrow();
    }

    public User replaceUser(User newUser, UUID id) {
          User oldUser = repository.findById(id).orElse(new User());
          if(oldUser.getId() == null){
              oldUser.setId(id);
          }
          oldUser.setEmail(newUser.getEmail());
          oldUser.setFirstName(newUser.getFirstName());
          oldUser.setMiddleName(newUser.getMiddleName());
          oldUser.setLastName(newUser.getLastName());
          oldUser.setPasswordHash(newUser.getPasswordHash());
          oldUser.setIsDeleted(newUser.getIsDeleted());
          oldUser.setIsSystemAdmin(newUser.getIsSystemAdmin());
          oldUser.setProfileImageThumb(newUser.getProfileImageThumb());
          oldUser.setPhone(newUser.getPhone());
          oldUser.setRoles(newUser.getRoles());
          oldUser.setActive(newUser.isActive());
          oldUser.setDateCreated(newUser.getDateCreated());
          oldUser.setDateModified(newUser.getDateModified());
          oldUser.setDateDeleted(newUser.getDateDeleted());
          oldUser.setReasonForDeactivation(newUser.getReasonForDeactivation());
          return repository.save(oldUser);
    }

    public ProfileDTO updateUserDetails(ProfileDTO profileDTO)  {
        var user = repository.getUserByEmail(profileDTO.getEmail()).orElseThrow();
            user.setFirstName(profileDTO.getFirstName());
            user.setLastName(profileDTO.getLastName());
            user.setActive(profileDTO.getIsActive());
            user.setPhone(profileDTO.getPhone());
            repository.save(user);
            return profileDTO;
    }

    public boolean changePassword(ChangePasswordDTO dto) throws RuntimeException {
        User user = getLoggedUser();
                if(user == null){
                    throw new IllegalArgumentException("User not found");
                }
        if (AppUtils.hashMatch(dto.getOldPassword(), user.getPasswordHash())) throw new IllegalArgumentException("Password mismatch");
        user.setPasswordHash(AppUtils.hash(dto.getNewPassword()));
        replaceUser(user, user.getId());
        return true;
    }





    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = repository.getUserByEmailOrPhone(username).orElseThrow();
            return user.toUserDetails();
    }

    @Override
    public UserDetails updatePassword(UserDetails user,String newPassword) {
        User userCredentials = repository.getUserByEmailOrPhone((user.getUsername())).orElseThrow();
        userCredentials.setPasswordHash(newPassword);
        return userCredentials.toUserDetails();
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (alreadySetup) return;
        User adminUser = getUserByEmail("admin@authapp.com");
        String newPassword = generateID(10, false);

        if (adminUser == null){
            Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
            Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");


        Set<Privilege> adminPrivileges = new HashSet<>();
        adminPrivileges.add(readPrivilege);
        adminPrivileges.add(writePrivilege);



        Set<Privilege> userPrivileges = new HashSet<>();
        adminPrivileges.add(readPrivilege);



        createRoleIfNotFound(null,"ROLE_ADMIN", adminPrivileges);
        createRoleIfNotFound(null,"ROLE_USER", userPrivileges);

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        User user = new User();
        user.setFirstName("Admin");
        user.setLastName("Admin");
        user.setPasswordHash(AppUtils.hash(newPassword));
        user.setEmail("admin@authapp.com");
        user.setRoles(roles);
        user.setActive(true);
        adminUser = repository.save(user);
        alreadySetup = true;
        }else{
            adminUser.setPasswordHash(AppUtils.hash(newPassword));
        repository.save(adminUser);
        }
        alreadySetup = true;
        Logger.getAnonymousLogger().log(Level.INFO, "Password : $newPassword");

    }

    @Transactional
    public Privilege createPrivilegeIfNotFound(String name) {
        var privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(null, name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    public void createRoleIfNotFound(UUID tenantId, String name, Set<Privilege> privileges) {
        Role role = roleRepository.findByName(name);

        if (role == null) {
            role = new Role(null, name, privileges, ZonedDateTime.now(), null);
            roleRepository.save(role);
        }
    }

    public Collection<GrantedAuthority> getAuthorities(Collection<Role> roles)  {
        return getGrantedAuthorities(getPrivileges(roles));
    }

    public List<String> getPrivileges(Collection<Role> roles) {
        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        roles.forEach(role -> {
            privileges.add(role.getName());
             collection.addAll(role.getPrivileges());
        });

        for (Privilege item : collection) {
            privileges.add(item.getName());
        };
        return privileges;
    }

    public List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
           authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

    public User getLoggedUser() {
        return repository.getUserByEmailOrPhone(AppUtils.getUsername()).orElseThrow();
    }



}



