package com.nipun.system.document.branch.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object for create branch")
public class CreateBranchRequest {
    @Schema(description = "Content of the document", example = "Hello everyone")
    private String branchName;
}
