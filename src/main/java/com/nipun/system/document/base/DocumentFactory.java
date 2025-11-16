package com.nipun.system.document.base;

import com.nipun.system.document.Status;
import com.nipun.system.document.branch.Branch;
import com.nipun.system.document.content.Content;
import com.nipun.system.document.template.Template;
import com.nipun.system.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DocumentFactory {
    public static Document createNewDocument(User user, String title, Status status, Template template) {
        var document = Document.builder()
                .publicId(UUID.randomUUID())
                .owner(user)
                .title(title)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .trashed(false)
                .favorite(false)
                .build();

        var content = Content.builder()
                .content(template.getTemplate())
                .build();

        var branch = Branch.builder()
                .publicId(UUID.randomUUID())
                .branchName("main")
                .document(document)
                .content(content)
                .owner(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(Status.PUBLIC)
                .trashed(false)
                .build();

        document.addBranch(branch);

        return document;
    }
}
