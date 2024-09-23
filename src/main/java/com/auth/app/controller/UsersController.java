package com.auth.app.controller;

import com.auth.app.DTO.*;
import com.auth.app.service.UserService;

import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import javax.ws.rs.QueryParam

@Slf4j
@RestController
@RequestMapping("api/users")
@AllArgsConstructor
class UsersController {

    private UserService userService;

    @PostMapping("/createUser")
    public ResponseEntity<Boolean> createNewUser(@RequestBody CreateUserDTO createUserDTO){
        userService.createUser(createUserDTO, false);
        return ResponseEntity.ok().body(true);

    }

    @GetMapping("/verifyEmail/{email}/{otp}")
    public ResponseEntity<Boolean> verifyEmail(@PathVariable("email") String email, @PathVariable("otp") String otp) {
            return ResponseEntity.ok().body(userService.verifyUserEmail(email,otp)))
    }



    @PostMapping("/changePassword")
    public ResponseEntity<Boolean> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
            return ResponseEntity.ok().body(userService.changePassword(changePasswordDTO));
    }


//    @PostMapping("/resetPassword")
//    fun resetPassword(@RequestBody resetPasswordDTO: ResetPasswordDTO): ResponseEntity<ServiceResponse<Boolean>> {
//            return buildSuccessResponse(userService.resetPassword(resetPasswordDTO))
//    }



    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody LoginDTO loginModel) {
          return ResponseEntity.ok().body(userService.getToken(loginModel));
    }


    @GetMapping("/refreshToken")
    public ResponseEntity<Token>  refreshToken(@QueryParam("refreshToken") refreshToken: String){
            return buildSuccessResponse(userService.refreshToken(refreshToken));
    }


    @GetMapping("/all")
    fun all(): ResponseEntity<ServiceResponse<List<ProfileDTO?>>> {
            return buildSuccessResponse(userService.getUsers())
    }

    @GetMapping("/user")
    fun getUser(email:String): ResponseEntity<ServiceResponse<ProfileDTO>> {
            return buildSuccessResponse(userService.getUserByEmail(email).toProfileDTO())
    }




    @PutMapping("/profile")
    public ResponseEntity<ProfileDTO> updateUser(@RequestBody ProfileDTO profileDTO) {
            return ResponseEntity.ok().body(userService.updateUserDetails(profileDTO));
    }


}





