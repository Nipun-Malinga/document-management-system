package com.nipun.system.document.diff;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.nipun.system.document.dtos.version.DiffRowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DiffServiceImpl implements DiffService{

    @Override
    public List<DiffRowDto> getVersionDiffs(String baseVersionContent, String compareVersionContent) {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .oldTag(_ -> "~")
                .newTag(_ -> "**")
                .build();


        List<DiffRow> rows = generator.generateDiffRows(
                List.of(compareVersionContent.split("\n")),
                List.of(baseVersionContent.split("\n")));

        return rows
                .stream()
                .map(row ->
                        new DiffRowDto(
                                row.getTag().toString(),
                                row.getOldLine(),
                                row.getNewLine())
                )
                .toList();
    }

    @Override
    public String patchDocument(String originalDoc, String updatedDoc) throws PatchFailedException {
        String baseContent = originalDoc.replace("\r\n", "\n");
        String targetContent = updatedDoc.replace("\r\n", "\n");

        List<String> base = List.of(baseContent.split("\n"));
        List<String> target = List.of(targetContent.split("\n"));

        Patch<String> patch = DiffUtils.diff(base, target);

        List<String> patched = patch.applyTo(base);

        return String.join("\n", patched);
    }
}
