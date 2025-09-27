package com.nipun.system.document.diff.dtos;

import com.nipun.system.document.diff.DiffRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DiffResponse {
    private Map<String, List<DiffRow>> diffs;
}
