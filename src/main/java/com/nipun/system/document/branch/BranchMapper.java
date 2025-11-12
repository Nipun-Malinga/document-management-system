package com.nipun.system.document.branch;

import com.nipun.system.document.branch.dtos.BranchResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "Spring")
public interface BranchMapper {

    @Mapping(target = "id", source = "publicId")
    @Mapping(target = "documentId", source = "document.publicId")
    @Mapping(target = "branchName", source = "branchName")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "trashed", source = "trashed")
    BranchResponse toDto(Branch branch);
}
