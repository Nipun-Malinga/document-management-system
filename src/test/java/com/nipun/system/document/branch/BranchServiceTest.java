package com.nipun.system.document.branch;

import com.nipun.system.document.Status;
import com.nipun.system.document.base.Document;
import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.dtos.ContentResponse;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.branch.dtos.BranchResponse;
import com.nipun.system.document.branch.exceptions.BranchNotFoundException;
import com.nipun.system.document.branch.exceptions.BranchTitleAlreadyExistsException;
import com.nipun.system.document.content.Content;
import com.nipun.system.document.diff.DiffUtils;
import com.nipun.system.document.diff.dtos.DiffResponse;
import com.nipun.system.document.permission.PermissionUtils;
import com.nipun.system.document.permission.exceptions.UnauthorizedDocumentException;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.shared.utils.UserIdUtils;
import com.nipun.system.user.User;
import com.nipun.system.user.UserRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Branch Service Unit Test")
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private BranchMapper branchMapper;

    @Mock
    private DiffUtils diffUtils;

    @InjectMocks
    private BranchService branchService;

    private User testUser;
    private Long testUserId;
    private Document testDocument;
    private UUID testDocumentId;
    private UUID testBranchId;
    private UUID testMergeBranchId;
    private Branch testBranch;
    private Branch testMergeBranch;
    private BranchResponse testBranchResponse;

    @BeforeEach
    void setup() {
        this.testUserId = 1L;
        this.testDocumentId = UUID.randomUUID();
        this.testBranchId = UUID.randomUUID();
        this.testMergeBranchId = UUID.randomUUID();

        this.testDocument = new Document();
        this.testDocument.setId(1L);
        this.testDocument.setPublicId(testDocumentId);
        this.testDocument.setTitle("New Document");
        this.testDocument.setStatus(Status.PRIVATE);
        this.testDocument.setTrashed(false);
        this.testDocument.setFavorite(false);
        this.testDocument.setCreatedAt(LocalDateTime.now());
        this.testDocument.setUpdatedAt(LocalDateTime.now());

        this.testUser = new User();
        this.testUser.setId(testUserId);
        this.testUser.setUsername("Test User");
        this.testUser.setEmail("testuser@email.com");
        this.testUser.setPassword("test12345");

        Content testContent = new Content();
        testContent.setContent("Test Content");

        this.testBranch = new Branch();
        this.testBranch.setId(1L);
        this.testBranch.setPublicId(testBranchId);
        this.testBranch.setBranchName("Test Branch");
        this.testBranch.setContent(testContent);
        this.testBranch.setStatus(Status.PRIVATE);

        this.testMergeBranch = new Branch();
        this.testMergeBranch.setId(2L);
        this.testMergeBranch.setPublicId(testMergeBranchId);
        this.testMergeBranch.setBranchName("Merge Branch");
        this.testMergeBranch.setContent(testContent);
        this.testMergeBranch.setBranchContent("Merge Content");
        this.testMergeBranch.setStatus(Status.PRIVATE);

        testBranchResponse = new BranchResponse();
        testBranchResponse.setId(UUID.randomUUID());
        testBranchResponse.setBranchName("Test Branch");
    }

    @Nested
    @DisplayName("Create Branch Tests")
    class CreateBranchTests {

        @Test
        @DisplayName("Should create branch successfully with valid request")
        void shouldCreateBranchSuccessfully() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<BranchFactory> mockedBranchFactory = mockStatic(BranchFactory.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(userRepository.findById(testUserId))
                        .thenReturn(Optional.of(testUser));

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.checkUserCanWrite(testUserId, testDocument)
                ).thenAnswer(_ -> null);

                when(branchRepository.existsByBranchNameAndDocumentId(
                        "Test Branch",
                        testDocument.getId()
                )).thenReturn(false);

                when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                        .thenReturn(Optional.of(testBranch));

                mockedBranchFactory.when(() ->
                        BranchFactory.createNewBranch(
                                testDocument,
                                "Test Branch",
                                testBranch.getBranchContent(),
                                testUser
                        )).thenReturn(testBranch);

                when(branchRepository.save(testBranch))
                        .thenReturn(testBranch);

                when(branchMapper.toDto(testBranch))
                        .thenReturn(testBranchResponse);

                BranchResponse response = branchService.createBranch(
                        testDocumentId,
                        testBranchId,
                        "Test Branch"
                );

                assertThat(response).isNotNull();
                assertThat(response).isEqualTo(testBranchResponse);

                verify(documentRepository).findByPublicId(testDocumentId);
                verify(userRepository).findById(testUserId);
                verify(branchRepository).existsByBranchNameAndDocumentId(
                        "Test Branch",
                        testDocument.getId()
                );
                verify(branchRepository).save(testBranch);
                verify(branchMapper).toDto(testBranch);
            }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        void shouldThrowDocumentNotFoundExceptionWhenDocumentNotFound() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class)) {
                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(userRepository.findById(testUserId))
                        .thenReturn(Optional.of(testUser));

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> branchService.createBranch(
                        testDocumentId,
                        testBranchId,
                        "Test Branch"
                )).isInstanceOf(DocumentNotFoundException.class);

                verify(documentRepository).findByPublicId(testDocumentId);
                verifyNoInteractions(branchRepository);
            }
        }

        @Test
        @DisplayName("Should throw BranchTitleAlreadyExistsException when branch name exists")
        void shouldThrowBranchTitleAlreadyExistsException() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(userRepository.findById(testUserId))
                        .thenReturn(Optional.of(testUser));

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.checkUserCanWrite(testUserId, testDocument)
                ).thenAnswer(_ -> null);

                when(branchRepository.existsByBranchNameAndDocumentId(
                        "Test Branch",
                        testDocument.getId()
                )).thenReturn(true);

                assertThatThrownBy(() -> branchService.createBranch(
                        testDocumentId,
                        testBranchId,
                        "Test Branch"
                )).isInstanceOf(BranchTitleAlreadyExistsException.class);

                verify(branchRepository).existsByBranchNameAndDocumentId(
                        "Test Branch",
                        testDocument.getId()
                );
                verify(branchRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should throw BranchNotFoundException when base branch not found")
        void shouldThrowBranchNotFoundExceptionWhenBaseBranchNotFound() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(userRepository.findById(testUserId))
                        .thenReturn(Optional.of(testUser));

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.checkUserCanWrite(testUserId, testDocument)
                ).thenAnswer(_ -> null);

                when(branchRepository.existsByBranchNameAndDocumentId(
                        "Test Branch",
                        testDocument.getId()
                )).thenReturn(false);

                when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> branchService.createBranch(
                        testDocumentId,
                        testBranchId,
                        "Test Branch"
                )).isInstanceOf(BranchNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("Get All Branches Tests")
    class GetAllBranchesTests {

        @Test
        @DisplayName("Should return paginated branches successfully")
        void shouldReturnPaginatedBranchesSuccessfully() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.isUnauthorizedUser(testUserId, testDocument)
                ).thenReturn(false);

                PageRequest pageRequest = PageRequest.of(0, 10);
                Page<Branch> branchPage = new PageImpl<>(
                        List.of(testBranch),
                        pageRequest,
                        1
                );

                when(branchRepository.findAllByDocumentId(testDocument.getId(), pageRequest))
                        .thenReturn(branchPage);

                when(branchMapper.toDto(testBranch))
                        .thenReturn(testBranchResponse);

                PaginatedData result = branchService.getAllBranches(testDocumentId, 0, 10);

                assertThat(result).isNotNull();
                assertThat(result.getData()).isNotNull();
                assertThat(result.getPageNumber()).isEqualTo(0);
                assertThat(result.getPageSize()).isEqualTo(10);

                verify(documentRepository).findByPublicId(testDocumentId);
                verify(branchRepository).findAllByDocumentId(testDocument.getId(), pageRequest);
            }
        }

        @Test
        @DisplayName("Should throw UnauthorizedDocumentException when user unauthorized")
        void shouldThrowUnauthorizedDocumentException() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.isUnauthorizedUser(testUserId, testDocument)
                ).thenReturn(true);

                assertThatThrownBy(() -> branchService.getAllBranches(testDocumentId, 0, 10))
                        .isInstanceOf(UnauthorizedDocumentException.class);

                verify(branchRepository, never()).findAllByDocumentId(any(), any());
            }
        }
    }

    @Nested
    @DisplayName("Get All Branch Count Tests")
    class GetAllBranchCountTests {

        @Test
        @DisplayName("Should return branch count successfully")
        void shouldReturnBranchCountSuccessfully() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class)) {
                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.of(testDocument));

                when(branchRepository.countAllByDocumentId(testDocument.getId()))
                        .thenReturn(5);

                CountResponse response = branchService.getAllBranchCount(testDocumentId);

                assertThat(response).isNotNull();
                assertThat(response.getCount()).isEqualTo(5L);

                verify(documentRepository).findByPublicIdAndOwnerId(testDocumentId, testUserId);
                verify(branchRepository).countAllByDocumentId(testDocument.getId());
            }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        void shouldThrowDocumentNotFoundException() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class)) {
                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> branchService.getAllBranchCount(testDocumentId))
                        .isInstanceOf(DocumentNotFoundException.class);

                verify(branchRepository, never()).countAllByDocumentId(any());
            }
        }
    }

    @Nested
    @DisplayName("Get Branch Content Tests")
    class GetBranchContentTests {

        @Test
        @DisplayName("Should return branch content successfully")
        void shouldReturnBranchContentSuccessfully() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                        .thenReturn(Optional.of(testBranch));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.isUnauthorizedUser(testUserId, testDocument)
                ).thenReturn(false);

                ContentResponse response = branchService.getBranchContent(testDocumentId, testBranchId);

                assertThat(response).isNotNull();
                assertThat(response.getContent()).isEqualTo("Merge Content");

                verify(documentRepository).findByPublicId(testDocumentId);
                verify(branchRepository).findByPublicIdAndDocumentId(testBranchId, testDocument.getId());
            }
        }

        @Test
        @DisplayName("Should throw BranchNotFoundException when branch not found")
        void shouldThrowBranchNotFoundException() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class)) {
                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> branchService.getBranchContent(testDocumentId, testBranchId))
                        .isInstanceOf(BranchNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("Update Branch Content Tests")
    class UpdateBranchContentTests {

        @Test
        @DisplayName("Should update branch content successfully")
        void shouldUpdateBranchContentSuccessfully() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                String newContent = "Updated Content";

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.checkUserCanWrite(testUserId, testDocument)
                ).thenAnswer(_ -> null);

                when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                        .thenReturn(Optional.of(testBranch));

                when(branchRepository.save(testBranch))
                        .thenReturn(testBranch);

                ContentResponse response = branchService.updateBranchContent(
                        testDocumentId,
                        testBranchId,
                        newContent
                );

                assertThat(response).isNotNull();
                assertThat(testBranch.getBranchContent()).isEqualTo(newContent);

                verify(branchRepository).save(testBranch);
            }
        }
    }

    @Nested
    @DisplayName("Get Branch Diffs Tests")
    class GetBranchDiffsTests {

        @Test
        @DisplayName("Should return diffs for public branches without authorization")
        void shouldReturnDiffsForPublicBranches() {
            testBranch.setStatus(Status.PUBLIC);
            testMergeBranch.setStatus(Status.PUBLIC);

            when(documentRepository.findByPublicId(testDocumentId))
                    .thenReturn(Optional.of(testDocument));

            when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                    .thenReturn(Optional.of(testBranch));

            when(branchRepository.findByPublicIdAndDocumentId(testMergeBranchId, testDocument.getId()))
                    .thenReturn(Optional.of(testMergeBranch));

            DiffResponse expectedDiff = new DiffResponse();
            when(diffUtils.buildDiffResponse(
                    testBranch.getBranchContent(),
                    testMergeBranch.getBranchContent()
            )).thenReturn(expectedDiff);

            DiffResponse response = branchService.getBranchDiffs(
                    testDocumentId,
                    testBranchId,
                    testMergeBranchId
            );

            assertThat(response).isNotNull();
            assertThat(response).isEqualTo(expectedDiff);

            verify(diffUtils).buildDiffResponse(
                    testBranch.getBranchContent(),
                    testMergeBranch.getBranchContent()
            );
        }

        @Test
        @DisplayName("Should return diffs for private branches with authorization")
        void shouldReturnDiffsForPrivateBranches() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                testBranch.setStatus(Status.PRIVATE);
                testMergeBranch.setStatus(Status.PRIVATE);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                        .thenReturn(Optional.of(testBranch));

                when(branchRepository.findByPublicIdAndDocumentId(testMergeBranchId, testDocument.getId()))
                        .thenReturn(Optional.of(testMergeBranch));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.isUnauthorizedUser(testUserId, testDocument)
                ).thenReturn(false);

                DiffResponse expectedDiff = new DiffResponse();
                when(diffUtils.buildDiffResponse(
                        testBranch.getBranchContent(),
                        testMergeBranch.getBranchContent()
                )).thenReturn(expectedDiff);

                DiffResponse response = branchService.getBranchDiffs(
                        testDocumentId,
                        testBranchId,
                        testMergeBranchId
                );

                assertThat(response).isNotNull();
                verify(diffUtils).buildDiffResponse(
                        testBranch.getBranchContent(),
                        testMergeBranch.getBranchContent()
                );
            }
        }
    }

    @Nested
    @DisplayName("Merge Branches Tests")
    class MergeBranchesTests {

        @Test
        @DisplayName("Should merge branches successfully")
        void shouldMergeBranchesSuccessfully() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.checkUserCanWrite(testUserId, testDocument)
                ).thenAnswer(_ -> null);

                when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                        .thenReturn(Optional.of(testBranch));

                when(branchRepository.findByPublicIdAndDocumentId(testMergeBranchId, testDocument.getId()))
                        .thenReturn(Optional.of(testMergeBranch));

                when(branchRepository.save(testBranch))
                        .thenReturn(testBranch);

                branchService.mergeBranches(testDocumentId, testBranchId, testMergeBranchId);

                assertThat(testBranch.getBranchContent()).isEqualTo(testMergeBranch.getBranchContent());

                verify(branchRepository).save(testBranch);
                verify(documentRepository).findByPublicId(testDocumentId);
            }
        }

        @Test
        @DisplayName("Should throw BranchNotFoundException when base branch not found")
        void shouldThrowBranchNotFoundExceptionWhenBaseBranchNotFound() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.checkUserCanWrite(testUserId, testDocument)
                ).thenAnswer(_ -> null);

                when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> branchService.mergeBranches(
                        testDocumentId,
                        testBranchId,
                        testMergeBranchId
                )).isInstanceOf(BranchNotFoundException.class);

                verify(branchRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should throw BranchNotFoundException when merge branch not found")
        void shouldThrowBranchNotFoundExceptionWhenMergeBranchNotFound() {
            try (MockedStatic<UserIdUtils> mockedUserIdUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<PermissionUtils> mockedPermissionUtils = mockStatic(PermissionUtils.class)) {

                mockedUserIdUtils.when(UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));

                mockedPermissionUtils.when(() ->
                        PermissionUtils.checkUserCanWrite(testUserId, testDocument)
                ).thenAnswer(_ -> null);

                when(branchRepository.findByPublicIdAndDocumentId(testBranchId, testDocument.getId()))
                        .thenReturn(Optional.of(testBranch));

                when(branchRepository.findByPublicIdAndDocumentId(testMergeBranchId, testDocument.getId()))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> branchService.mergeBranches(
                        testDocumentId,
                        testBranchId,
                        testMergeBranchId
                )).isInstanceOf(BranchNotFoundException.class);

                verify(branchRepository, never()).save(any());
            }
        }
    }
}