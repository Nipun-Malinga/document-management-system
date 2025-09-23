package com.nipun.system.document.diff;

import com.nipun.system.document.dtos.version.DiffRowDto;
import com.nipun.system.document.exceptions.PatchFailedException;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiffServiceImpl implements DiffService {

    private final DiffMatchPatch dmp = new DiffMatchPatch();

    @Override
    public List<DiffRowDto> getVersionDiffs(String baseVersionContent, String compareVersionContent) {
        if(baseVersionContent == null || compareVersionContent == null)
            throw new IllegalArgumentException("Document contents cannot be null");

        var diffs = dmp.diffMain(baseVersionContent, compareVersionContent);

        dmp.diffCleanupSemantic(diffs);

        return diffs.stream()
                .map(diff -> new DiffRowDto(diff.operation.toString(), diff.text))
                .toList();
    }

    @Override
    public String patchDocument(String originalDoc, String updatedDoc) {
        if(originalDoc == null || updatedDoc == null)
            throw new IllegalArgumentException("Document contents cannot be null");

        var patches = dmp.patchMake(originalDoc, updatedDoc);
        var patchResults = dmp.patchApply(patches, originalDoc);

        if (isPatchSuccess(patchResults))
            return (String) patchResults[0];

        throw new PatchFailedException("Not all patches applied successfully");
    }

    private boolean isPatchSuccess(Object[] patchResults) {
        boolean patchSuccesses = true;

        for (var applied : (boolean[]) patchResults[1]) {
            patchSuccesses = applied;
        }

        return patchSuccesses;
    }
}
