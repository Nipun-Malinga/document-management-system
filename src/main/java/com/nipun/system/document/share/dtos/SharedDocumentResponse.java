package com.nipun.system.document.share.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Response object for user permission")
public class SharedDocumentResponse {
    private List<SharedDocumentDto> data;
}
