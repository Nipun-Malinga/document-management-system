package com.nipun.system.document.utils;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import com.nipun.system.document.Document;
import com.nipun.system.document.version.DocumentVersion;
import com.nipun.system.document.version.DocumentVersionContent;
import com.nipun.system.user.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

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

    public static String patchDocument(String originalDoc, String updatedDoc) throws PatchFailedException {
        String baseContent = originalDoc.replace("\r\n", "\n");
        String targetContent = updatedDoc.replace("\r\n", "\n");

        List<String> base = List.of(baseContent.split("\n"));
        List<String> target = List.of(targetContent.split("\n"));

        Patch<String> patch = DiffUtils.diff(base, target);

        List<String> patched = patch.applyTo(base);

        return String.join("\n", patched);
    }
}
