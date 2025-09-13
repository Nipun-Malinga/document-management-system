package com.nipun.system.document.dtos.version;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DiffRowDto {
    private String tag;
    private String oldLine;
    private String newLine;
}
