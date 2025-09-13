package com.nipun.system.document.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class Utils {

    public static Long getUserIdFromContext() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }

    public static Long getUserIdFromPrincipal(Principal principal) {
        if (principal instanceof org.springframework.security.core.Authentication auth) {
            Object userObj = auth.getPrincipal();
            if (userObj instanceof Long l) {
                return l;
            }
        }
        return null;
    }
}
