package com.nipun.system.document.diff;

import java.util.List;

public interface DiffService {
    List<DiffRow> getVersionDiffs(String baseVersionContent, String compareVersionContent);

    String patchDocument(String originalDoc, String updatedDoc);
}
