package com.nipun.system.document.base;

import com.nipun.system.document.Status;
import com.nipun.system.document.branch.Branch;
import com.nipun.system.document.content.Content;
import com.nipun.system.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DocumentFactory {
    public static Document createNewDocument(User user, String title, Status status) {
        var document = Document.builder()
                .publicId(UUID.randomUUID())
                .owner(user)
                .title(title)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        var content = Content.builder()
                .content("")
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
                .build();

        document.addBranch(branch);

        return document;
    }
}
