package com.nipun.system.document.diff;

import com.nipun.system.document.dtos.version.DiffRowDto;

import java.util.List;

public interface DiffService {
    List<DiffRowDto> getVersionDiffs(String baseVersionContent, String compareVersionContent);

    String patchDocument(String originalDoc, String updatedDoc);
}
