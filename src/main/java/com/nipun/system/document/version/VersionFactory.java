package com.nipun.system.document.version;

import com.nipun.system.document.Status;
import com.nipun.system.document.branch.Branch;
import com.nipun.system.document.content.Content;
import com.nipun.system.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class VersionFactory {

    public Version createNewVersion(
            Branch branch,
            User createdBy,
            String content,
            String title,
            Status status

    ) {
        var versionContent = Content.builder().content(content).build();

        return Version.builder()
                .publicId(UUID.randomUUID())
                .branch(branch)
                .createdBy(createdBy)
                .content(versionContent)
                .versionTitle(title)
                .createdAt(LocalDateTime.now())
                .status(status)
                .build();
    }
}
