package com.nipun.system.document.branch;

import com.nipun.system.document.Status;
import com.nipun.system.document.base.Document;
import com.nipun.system.document.content.Content;
import com.nipun.system.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class BranchFactory {

    public static Branch createNewBranch(Document document, String branchName, String content, User owner) {

        var branchContent = Content.builder().content(content).build();

        return Branch.builder()
                .publicId(UUID.randomUUID())
                .branchName(branchName)
                .document(document)
                .content(branchContent)
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .status(Status.PUBLIC)
                .trashed(false)
                .build();
    }
}
