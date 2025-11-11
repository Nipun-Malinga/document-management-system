package com.nipun.system.document.trash;

import com.nipun.system.document.base.Document;
import com.nipun.system.document.branch.Branch;

import java.time.LocalDateTime;

public class TrashFactory {
    public static Trash createDocumentTrash(Document document) {
        return Trash.builder()
                .document(document)
                .added_date(LocalDateTime.now())
                .build();
    }

    public static Trash createBranchTrash(Branch branch) {
        return Trash.builder()
                .branch(branch)
                .added_date(LocalDateTime.now())
                .build();
    }
}
