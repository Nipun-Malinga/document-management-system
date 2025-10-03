package com.nipun.system.document.websocket.state;

import com.nipun.system.document.branch.BranchRepository;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StateServiceImpl implements StateService {
    private final BranchRepository branchRepository;
    private final StateCacheService stateCacheService;

    @Override
    public String setDocumentState(UUID documentId, UUID branchId, Long userId, String content) {
        stateCacheService.setDocumentState(documentId, branchId, content);
        return content;
    }

    @Override
    public String getDocumentState(UUID documentId, UUID branchId, Long userId) {
        var state = stateCacheService.getDocumentState(documentId, branchId);

        if (state != null)
            return state;

        var branch = branchRepository
                .findByPublicIdAndDocumentPublicId(branchId, documentId)
                .orElseThrow(BranchNotFoundException::new);
        return branch.getBranchContent();
    }
}
