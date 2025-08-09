package com.nipun.system.document.dtos.version;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class DiffResponse {
    private Map<String, Object> diffs;
}
