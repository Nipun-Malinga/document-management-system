package com.nipun.system.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PaginatedData {
    private Object data;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private Long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;
}
