package com.nipun.system.document.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class Documents {
    private List<DocumentDto> data;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private Long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;
}
