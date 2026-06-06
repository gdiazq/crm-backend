package com.crm.mcsv_user.util;

public final class NameUtil {

    private NameUtil() {
    }

    public static String fullName(String firstName, String lastName) {
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }
}
