package com.nipun.system.document.permission;

import com.nipun.system.document.Status;
import com.nipun.system.document.base.Document;
import com.nipun.system.document.branch.Branch;
import com.nipun.system.document.branch.BranchRepository;
import com.nipun.system.document.content.Content;
import com.nipun.system.document.share.Permission;
import com.nipun.system.shared.utils.UserIdUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("Permission Service Unit Tests")
class PermissionServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private PermissionService permissionService;

    private UUID testDocumentId;
    private UUID testBranchId;
    private Branch testBranch;
    private Document testDocument;

    @BeforeEach
    void setup() {
        testDocumentId = UUID.randomUUID();
        testBranchId = UUID.randomUUID();

        this.testDocument = new Document();
        this.testDocument.setId(1L);
        this.testDocument.setPublicId(testDocumentId);
        this.testDocument.setTitle("New Document");
        this.testDocument.setStatus(Status.PRIVATE);
        this.testDocument.setTrashed(false);
        this.testDocument.setFavorite(false);
        this.testDocument.setCreatedAt(LocalDateTime.now());
        this.testDocument.setUpdatedAt(LocalDateTime.now());

        this.testBranch = new Branch();
        this.testBranch.setId(1L);
        this.testBranch.setPublicId(testBranchId);
        this.testBranch.setBranchName("Test Branch");
        this.testBranch.setContent(new Content());
        this.testBranch.setStatus(Status.PRIVATE);
    }

    @Nested
    @DisplayName("Validate User Permissions Tests")
    class ValidateUserPermissionTests {

        @Test
        @DisplayName("Should Validate Read Only User")
        void shouldValidateReadOnlyUser() {
            try (
                    MockedStatic<UserIdUtils> testUserIdUtils = mockStatic(UserIdUtils.class);
                    MockedStatic<PermissionUtils> testPermissionUtils = mockStatic(PermissionUtils.class)
            ) {
                testUserIdUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(1L);
                when(branchRepository.findByPublicIdAndDocumentPublicId(testBranchId, testDocumentId)).thenReturn(Optional.of(testBranch));
                testPermissionUtils.when(() -> PermissionUtils.isUnauthorizedUser(1L, testDocument)).thenReturn(false);
                testPermissionUtils.when(() ->
                        PermissionUtils.isReadOnlyUser(1L, testBranch.getDocument())
                ).thenReturn(true);

                var permissionResponse = permissionService.validateUserPermissions(testDocumentId, testBranchId);

                assertThat(permissionResponse).isNotNull();
                assertThat(permissionResponse.getPermission()).isEqualTo(Permission.READ_ONLY);
            }
        }

        @Test
        @DisplayName("Should Validate Read Write User")
        void shouldValidateReadWriteUser() {
            try (
                    MockedStatic<UserIdUtils> testUserIdUtils = mockStatic(UserIdUtils.class);
                    MockedStatic<PermissionUtils> testPermissionUtils = mockStatic(PermissionUtils.class)
            ) {
                testUserIdUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(1L);
                when(branchRepository.findByPublicIdAndDocumentPublicId(testBranchId, testDocumentId)).thenReturn(Optional.of(testBranch));
                testPermissionUtils.when(() -> PermissionUtils.isUnauthorizedUser(1L, testDocument)).thenReturn(false);
                testPermissionUtils.when(() ->
                        PermissionUtils.isReadOnlyUser(1L, testBranch.getDocument())
                ).thenReturn(false);

                var permissionResponse = permissionService.validateUserPermissions(testDocumentId, testBranchId);

                assertThat(permissionResponse).isNotNull();
                assertThat(permissionResponse.getPermission()).isEqualTo(Permission.READ_WRITE);
            }
        }

        @Test
        @DisplayName("Should validate unauthorized user")
        void shouldValidateUnauthorizedUser() {

            try (
                    MockedStatic<UserIdUtils> testUserIdUtils = mockStatic(UserIdUtils.class);
                    MockedStatic<PermissionUtils> testPermissionUtils = mockStatic(PermissionUtils.class)
            ) {
                testUserIdUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(1L);

                when(branchRepository.findByPublicIdAndDocumentPublicId(testBranchId, testDocumentId))
                        .thenReturn(Optional.of(testBranch));

                testPermissionUtils.when(() ->
                        PermissionUtils.isUnauthorizedUser(1L, testDocument)
                ).thenReturn(true);

                var permissionResponse =
                        permissionService.validateUserPermissions(testDocumentId, testBranchId);

                assertThat(permissionResponse).isNotNull();
                assertThat(permissionResponse.getPermission())
                        .isEqualTo(Permission.UNAUTHORIZED);
            }
        }

    }
}