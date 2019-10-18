package ru.gadjini.reminder.security;

public class SecurityContextHolder {

    private static final ThreadLocal<SecurityContext> SECURITY_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    public static SecurityContext getContext() {
        return SECURITY_CONTEXT_THREAD_LOCAL.get();
    }

    public static void setContext(SecurityContext securityContext) {
        SECURITY_CONTEXT_THREAD_LOCAL.set(securityContext);
    }

    public static void clearContext() {
        SECURITY_CONTEXT_THREAD_LOCAL.remove();
    }
}
