package com.auth.app.common;


import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class AppUtils {


    public static String generateID(int length, boolean onlyNumber) {
        List<String> result = new ArrayList<>();
        char[] characters =  (onlyNumber) ?
            "01234567890123456789012345678901234567890123456789".toCharArray() :
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

        int charactersLength = characters.length;
        for(int i = 0; i < length; i++){
            int index = (int) Math.round(Math.floor(Math.random() * charactersLength));
            result.add(Character.toString(characters[index]));
        }
        return StringUtils.join(result);
    }


    public static String generateID() {
        return generateID(6,false);
    }

    public static String generateID(boolean onlyNumber) {
        return generateID(6,onlyNumber);
    }

    public static String generateID(int length) {
        return generateID(length,false);
    }

    public static String  getUsername() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String[] list = jwt.getSubject().split(",");
        return list[list.length -1];
    }


    public static String hash(String text){
        int strength = 10;
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(strength, new SecureRandom());
        return bCryptPasswordEncoder.encode(text);
    }

    public static boolean hashMatch(String text, String hash){
        int strength = 10;
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(strength, new SecureRandom());
        return bCryptPasswordEncoder.matches(text ,text);
    }


}