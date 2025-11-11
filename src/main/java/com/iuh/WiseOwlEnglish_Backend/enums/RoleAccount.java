package com.iuh.WiseOwlEnglish_Backend.enums;

public enum RoleAccount {
    ADMIN,
    LEARNER;

    public static RoleAccount fromAuthority(String authority) {
        if (authority == null || authority.isBlank()) {
            return LEARNER;
        }
        String normalized = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
        try {
            return RoleAccount.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return LEARNER; // hoặc throw custom exception
        }
    }

}
