package tn.smartfight.session;

import tn.smartfight.models.User;

public class SessionManager {

    private static User currentUser;
    private static boolean adminMode;

    private SessionManager() {}

    public static void loginAsAdmin(User user) {
        currentUser = user;
        adminMode = true;
    }

    public static void loginAsFan(User user) {
        currentUser = user;
        adminMode = false;
    }

    public static void logout() {
        currentUser = null;
        adminMode = false;
    }

    public static boolean isAdmin() {
        return adminMode;
    }

    public static boolean isFan() {
        return currentUser != null && !adminMode;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public static String getCurrentUserName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }
}
