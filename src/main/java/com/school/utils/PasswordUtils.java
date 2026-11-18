package com.school.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    
    // Scrambles a plain password into a secure hash
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Checks if the typed password matches the stored hash
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}