package com.anml.codesanbox.Judge;

import java.security.SecureRandom;

public class AccessKeySecretKeyGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ACCESS_KEY_LENGTH = 20;
    private static final int SECRET_KEY_LENGTH = 40;

    public static void main(String[] args) {
        String accessKey = generateAccessKey();
        String secretKey = generateSecretKey();
        System.out.println("Access Key: " + accessKey);
        System.out.println("Secret Key: " + secretKey);
    }

    public static String generateAccessKey() {
        return generateRandomString(ACCESS_KEY_LENGTH);
    }

    public static String generateSecretKey() {
        return generateRandomString(SECRET_KEY_LENGTH);
    }

    private static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHABET.length());
            result.append(ALPHABET.charAt(index));
        }

        return result.toString();
    }
}