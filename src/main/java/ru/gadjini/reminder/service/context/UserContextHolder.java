package ru.gadjini.reminder.service.context;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> USER_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static void set(UserContext userContext) {
        USER_CONTEXT_THREAD_LOCAL.set(userContext);
    }

    public static UserContext get() {
        return USER_CONTEXT_THREAD_LOCAL.get();
    }

    public static void remove() {
        USER_CONTEXT_THREAD_LOCAL.remove();
    }
}
