package com.nipun.system.document.utils;

import com.nipun.system.document.Document;
import com.nipun.system.document.version.DocumentVersion;
import com.nipun.system.document.version.DocumentVersionContent;
import com.nipun.system.user.User;
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


    public static DocumentVersion createVersion(Document document, User user) {
        var versionContent = new DocumentVersionContent();

        if(document.getContent().getContent() != null)
            versionContent.setContent(document.getContent().getContent());

        var version = new DocumentVersion();
        version.addData(document, user, versionContent);

        return version;
    }
}
