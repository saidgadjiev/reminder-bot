package ru.gadjini.reminder.security;

import org.telegram.telegrambots.meta.api.objects.User;

public class SecurityContextHolder {

    private static final ThreadLocal<User> AUTHENTICATED_USERS = new ThreadLocal<>();

    public static User getAuthenticatedUser() {
        return AUTHENTICATED_USERS.get();
    }

    public static void setAuthenticatedUser(User user) {
        AUTHENTICATED_USERS.set(user);
    }

    public static void removeAuthenticatedUser() {
        AUTHENTICATED_USERS.remove();
    }
}
