package com.nipun.system.document.diff;

import com.nipun.system.document.diff.dtos.DiffResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class DiffUtils {
    private final DiffService diffService;

    public DiffResponse buildDiffResponse(String baseContent, String compareContent) {
        return new DiffResponse(Map.of(
                "diffs", diffService.getDiffs(baseContent, compareContent)
        ));
    }
}
