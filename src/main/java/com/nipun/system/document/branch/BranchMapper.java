package com.nipun.system.document.branch;

import com.nipun.system.document.branch.dtos.BranchResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "Spring")
public interface BranchMapper {

    @Mapping(target = "id", source = "publicId")
    @Mapping(target = "documentId", source = "document.publicId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "branchName", source = "branchName")
    BranchResponse toDto(Branch branch);
}
