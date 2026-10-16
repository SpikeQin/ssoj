package com.ss.ssoj.auth;

/**
 * 保存当前请求的登录用户信息，由登录拦截器写入、请求结束后清理。
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public record CurrentUser(Long userId, String username, Integer role) {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static Long userId() {
        CurrentUser user = HOLDER.get();
        return user == null ? null : user.userId();
    }

    public static boolean isAdmin() {
        CurrentUser user = HOLDER.get();
        return user != null && Integer.valueOf(1).equals(user.role());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
