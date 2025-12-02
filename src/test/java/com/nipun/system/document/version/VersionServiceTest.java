package com.nipun.system.document.version;

import com.nipun.system.document.Status;
import com.nipun.system.document.base.Document;
import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.branch.Branch;
import com.nipun.system.document.branch.BranchRepository;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import com.nipun.system.document.content.Content;
import com.nipun.system.document.diff.DiffUtils;
import com.nipun.system.document.permission.PermissionUtils;
import com.nipun.system.document.permission.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.version.dtos.VersionResponse;
import com.nipun.system.document.version.exceptions.VersionNotFoundException;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.Role;
import com.nipun.system.user.User;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.dtos.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Version Service Unit Test")
class VersionServiceTest {
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private VersionMapper versionMapper;

    @Mock
    private DiffUtils diffUtils;

    @Mock
    private VersionFactory versionFactory;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private VersionService versionService;

    private UUID testDocumentId;
    private UUID testBranchId;
    private UUID testVersionId;
    private VersionResponse testVersionResponse;
    private Document testDocument;
    private Branch testBranch;
    private Version testVersion;
    private User testUser;
    private Long testUserId;

    @BeforeEach
    void setup() {
        var testUserResponse = new UserResponse();
        testUserResponse.setId(1L);
        testUserResponse.setUsername("Test User");
        testUserResponse.setEmail("testuser@email.com");
        testUserResponse.setRole(Role.USER);

        Content testContent = new Content();
        testContent.setContent("");

        testDocumentId = UUID.randomUUID();
        testBranchId = UUID.randomUUID();
        testVersionId = UUID.randomUUID();
        testUserId = 1L;

        ContentResponse testContentResponse = new ContentResponse();
        testContentResponse.setContent("Content");

        testVersionResponse = new VersionResponse();
        testVersionResponse.setBranchId(testBranchId);
        testVersionResponse.setDocumentId(testDocumentId);
        testVersionResponse.setCreatedBy(testUserResponse);
        testVersionResponse.setTitle("Test Version");
        testVersionResponse.setStatus(Status.PRIVATE);
        testVersionResponse.setCreatedAt(LocalDateTime.now());

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("Test User");

        testBranch = new Branch();
        testBranch.setId(1L);
        testBranch.setPublicId(testBranchId);
        testBranch.setDocument(testDocument);
        testBranch.setContent(testContent);
        testBranch.setBranchContent("Content");

        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setPublicId(testDocumentId);
        testDocument.setOwner(testUser);
        testDocument.addBranch(testBranch);

        testVersion = new Version();
        testVersion.setId(1L);
        testVersion.setPublicId(testVersionId);
        testVersion.setBranch(testBranch);
        testVersion.setContent(testContent);
        testVersion.setStatus(Status.PRIVATE);
    }

    @Nested
    @DisplayName("createNewVersion Tests")
    class CreateNewVersionTests {

        @Test
        @DisplayName("Should create new version successfully")
        void shouldCreateNewVersionSuccessfully() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class);
                 MockedStatic<com.nipun.system.document.permission.PermissionUtils> permissionUtils =
                         mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);
                permissionUtils.when(() -> com.nipun.system.document.permission.PermissionUtils
                                .checkUserCanWrite(testUserId, testDocument))
                        .thenAnswer(_ -> null);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));
                when(userRepository.findById(testUserId))
                        .thenReturn(Optional.of(testUser));
                when(testDocument.getBranch(testBranchId))
                        .thenReturn(testBranch);
                when(versionFactory.createNewVersion(eq(testBranch), eq(testUser),
                        eq("Content"), eq("Test Version"), eq(Status.PRIVATE)))
                        .thenReturn(testVersion);
                when(versionRepository.save(testVersion))
                        .thenReturn(testVersion);
                when(versionMapper.toDto(testVersion))
                        .thenReturn(testVersionResponse);

                VersionResponse result = versionService.createNewVersion(
                        testDocumentId, testBranchId, "Test Version", Status.PRIVATE);

                assertThat(result).isNotNull();
                assertThat(result.getTitle()).isEqualTo("Test Version");
                assertThat(result.getStatus()).isEqualTo(Status.PRIVATE);
                verify(versionRepository).save(testVersion);
            }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        void shouldThrowDocumentNotFoundException() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);
                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> versionService.createNewVersion(
                        testDocumentId, testBranchId, "Test Version", Status.PRIVATE))
                        .isInstanceOf(DocumentNotFoundException.class);
            }
        }

        @Test
        @DisplayName("Should throw BranchNotFoundException when branch not found")
        void shouldThrowBranchNotFoundException() {

            Document testDocument = mock(Document.class);

            try (MockedStatic<UserIdUtils> userIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> permissionUtils = mockStatic(PermissionUtils.class)) {

                userIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                permissionUtils.when(() -> PermissionUtils.checkUserCanWrite(testUserId, testDocument))
                        .thenAnswer(_ -> null);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                when(userRepository.findById(testUserId))
                        .thenReturn(Optional.of(testUser));

                // Key part
                when(testDocument.getBranch(testBranchId)).thenReturn(null);

                assertThatThrownBy(() -> versionService.createNewVersion(
                        testDocumentId, testBranchId, "Test Version", Status.PRIVATE))
                        .isInstanceOf(BranchNotFoundException.class);
            }
        }


        @Nested
        @DisplayName("getAllDocumentVersions Tests")
        class GetAllDocumentVersionsTests {

            @Test
            @DisplayName("Should return paginated versions for authorized user")
            void shouldReturnPaginatedVersionsForAuthorizedUser() {
                try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                             mockStatic(com.nipun.system.shared.utils.UserIdUtils.class);
                     MockedStatic<com.nipun.system.document.permission.PermissionUtils> permissionUtils =
                             mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                    userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                            .thenReturn(testUserId);
                    permissionUtils.when(() -> com.nipun.system.document.permission.PermissionUtils
                                    .isUnauthorizedUser(testUserId, testDocument))
                            .thenReturn(false);

                    List<Version> versions = Collections.singletonList(testVersion);
                    Page<Version> versionPage = new PageImpl<>(versions, PageRequest.of(0, 10), 1);

                    when(documentRepository.findByPublicId(testDocumentId))
                            .thenReturn(Optional.of(testDocument));
                    when(versionRepository.findAllByBranchDocumentId(eq(1L), any(PageRequest.class)))
                            .thenReturn(versionPage);
                    when(versionMapper.toDto(testVersion))
                            .thenReturn(testVersionResponse);

                    PaginatedData result = versionService.getAllDocumentVersions(testDocumentId, 0, 10);

                    assertThat(result).isNotNull();
                    assertThat(result.getPageNumber()).isEqualTo(0);
                    assertThat(result.getPageSize()).isEqualTo(10);
                    assertThat(result.getTotalElements()).isEqualTo(1);
                    verify(versionRepository).findAllByBranchDocumentId(eq(1L), any(PageRequest.class));
                }
            }

            @Test
            @DisplayName("Should return only public versions for unauthorized user")
            void shouldReturnOnlyPublicVersionsForUnauthorizedUser() {
                try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                             mockStatic(com.nipun.system.shared.utils.UserIdUtils.class);
                     MockedStatic<com.nipun.system.document.permission.PermissionUtils> permissionUtils =
                             mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                    userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                            .thenReturn(2L);
                    permissionUtils.when(() -> com.nipun.system.document.permission.PermissionUtils
                                    .isUnauthorizedUser(2L, testDocument))
                            .thenReturn(true);

                    List<Version> versions = Collections.singletonList(testVersion);
                    Page<Version> versionPage = new PageImpl<>(versions, PageRequest.of(0, 10), 1);

                    when(documentRepository.findByPublicId(testDocumentId))
                            .thenReturn(Optional.of(testDocument));
                    when(versionRepository.findAllByBranchDocumentIdAndStatusPublic(eq(1L), any(PageRequest.class)))
                            .thenReturn(versionPage);
                    when(versionMapper.toDto(testVersion))
                            .thenReturn(testVersionResponse);

                    PaginatedData result = versionService.getAllDocumentVersions(testDocumentId, 0, 10);

                    assertThat(result).isNotNull();
                    verify(versionRepository).findAllByBranchDocumentIdAndStatusPublic(eq(1L), any(PageRequest.class));
                }
            }
        }

        @Nested
        @DisplayName("getVersionContent Tests")
        class GetVersionContentTests {

            @Test
            @DisplayName("Should return version content for public version without auth check")
            void shouldReturnPublicVersionContent() {
                testVersion.setStatus(Status.PUBLIC);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));
                when(versionRepository.findDocumentBranchVersion(testVersionId, 1L))
                        .thenReturn(Optional.of(testVersion));

                ContentResponse result = versionService.getVersionContent(testDocumentId, testVersionId);

                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEqualTo("Content");
            }

            @Test
            @DisplayName("Should return private version content for authorized user")
            void shouldReturnPrivateVersionContentForAuthorizedUser() {
                try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                             mockStatic(com.nipun.system.shared.utils.UserIdUtils.class);
                     MockedStatic<com.nipun.system.document.permission.PermissionUtils> permissionUtils =
                             mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                    userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                            .thenReturn(testUserId);
                    permissionUtils.when(() -> com.nipun.system.document.permission.PermissionUtils
                                    .isUnauthorizedUser(testUserId, testDocument))
                            .thenReturn(false);

                    testVersion.setStatus(Status.PRIVATE);

                    when(documentRepository.findByPublicId(testDocumentId))
                            .thenReturn(Optional.of(testDocument));
                    when(versionRepository.findDocumentBranchVersion(testVersionId, 1L))
                            .thenReturn(Optional.of(testVersion));

                    ContentResponse result = versionService.getVersionContent(testDocumentId, testVersionId);

                    assertThat(result).isNotNull();
                    assertThat(result.getContent()).isEqualTo("Content");
                }
            }

            @Test
            @DisplayName("Should throw UnauthorizedDocumentException for private version and unauthorized user")
            void shouldThrowUnauthorizedExceptionForPrivateVersion() {
                try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                             mockStatic(com.nipun.system.shared.utils.UserIdUtils.class);
                     MockedStatic<com.nipun.system.document.permission.PermissionUtils> permissionUtils =
                             mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                    userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                            .thenReturn(2L);
                    permissionUtils.when(() -> com.nipun.system.document.permission.PermissionUtils
                                    .isUnauthorizedUser(2L, testDocument))
                            .thenReturn(true);

                    testVersion.setStatus(Status.PRIVATE);

                    when(documentRepository.findByPublicId(testDocumentId))
                            .thenReturn(Optional.of(testDocument));
                    when(versionRepository.findDocumentBranchVersion(testVersionId, 1L))
                            .thenReturn(Optional.of(testVersion));

                    assertThatThrownBy(() -> versionService.getVersionContent(testDocumentId, testVersionId))
                            .isInstanceOf(UnauthorizedDocumentException.class);
                }
            }

            @Test
            @DisplayName("Should throw VersionNotFoundException when version not found")
            void shouldThrowVersionNotFoundException() {
                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));
                when(versionRepository.findDocumentBranchVersion(testVersionId, 1L))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> versionService.getVersionContent(testDocumentId, testVersionId))
                        .isInstanceOf(VersionNotFoundException.class);
            }
        }

        @Nested
        @DisplayName("mergeVersionToBranch Tests")
        class MergeVersionToBranchTests {

            @Test
            @DisplayName("Should merge version to branch successfully")
            void shouldMergeVersionToBranchSuccessfully() {
                try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                             mockStatic(com.nipun.system.shared.utils.UserIdUtils.class);
                     MockedStatic<com.nipun.system.document.permission.PermissionUtils> permissionUtils =
                             mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                    userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                            .thenReturn(testUserId);
                    permissionUtils.when(() -> com.nipun.system.document.permission.PermissionUtils
                                    .checkUserCanWrite(testUserId, testDocument))
                            .thenAnswer(_ -> null);

                    when(documentRepository.findByPublicId(testDocumentId))
                            .thenReturn(Optional.of(testDocument));
                    when(branchRepository.findByPublicIdAndDocumentId(testBranchId, 1L))
                            .thenReturn(Optional.of(testBranch));
                    when(versionRepository.findByPublicIdAndBranchDocumentId(testVersionId, 1L))
                            .thenReturn(Optional.of(testVersion));

                    versionService.mergeVersionToBranch(testDocumentId, testBranchId, testVersionId);

                    assertThat(testBranch.getBranchContent()).isEqualTo("Content");
                    verify(branchRepository).save(testBranch);
                }
            }

            @Test
            @DisplayName("Should throw BranchNotFoundException when branch not found")
            void shouldThrowBranchNotFoundExceptionOnMerge() {
                try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                             mockStatic(com.nipun.system.shared.utils.UserIdUtils.class);
                     MockedStatic<com.nipun.system.document.permission.PermissionUtils> permissionUtils =
                             mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                    userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                            .thenReturn(testUserId);
                    permissionUtils.when(() -> com.nipun.system.document.permission.PermissionUtils
                                    .checkUserCanWrite(testUserId, testDocument))
                            .thenAnswer(_ -> null);

                    when(documentRepository.findByPublicId(testDocumentId))
                            .thenReturn(Optional.of(testDocument));
                    when(branchRepository.findByPublicIdAndDocumentId(testBranchId, 1L))
                            .thenReturn(Optional.empty());

                    assertThatThrownBy(() -> versionService.mergeVersionToBranch(
                            testDocumentId, testBranchId, testVersionId))
                            .isInstanceOf(BranchNotFoundException.class);
                }
            }
        }
    }
}