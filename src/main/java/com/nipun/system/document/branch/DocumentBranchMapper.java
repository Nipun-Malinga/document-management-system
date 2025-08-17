package com.nipun.system.document.branch;

import com.nipun.system.document.dtos.branch.DocumentBranchDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "Spring")
public interface DocumentBranchMapper {

    @Mapping(target = "branchId", source = "publicId")
    @Mapping(target = "versionNumber", source = "version.versionNumber")
    @Mapping(target = "branchName", source = "branchName")
    @Mapping(target = "content", source = "content.content")
    DocumentBranchDto toDto(DocumentBranch branch);
}
