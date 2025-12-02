package com.nipun.system.document.base;

import com.nipun.system.document.Status;
import com.nipun.system.document.base.dtos.CreateDocumentRequest;
import com.nipun.system.document.base.dtos.DocumentResponse;
import com.nipun.system.document.base.dtos.UpdateDocumentRequest;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.permission.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.template.Template;
import com.nipun.system.document.template.TemplateRepository;
import com.nipun.system.document.template.exceptions.TemplateNotFoundException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Document Service Unit Test")
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private DocumentService documentService;

    private Document testDocument;
    private CreateDocumentRequest testCreateDocumentRequest;
    private DocumentResponse testDocumentResponse;
    private UpdateDocumentRequest testUpdateDocumentRequest;
    private Template testTemplate;
    private User testUser;
    private Long testUserId;
    private UUID testDocumentId;

    @BeforeEach
    void setup() {
        testUserId = 1L;
        testDocumentId = UUID.fromString("e34bf791-137d-4340-b8d0-4918f01d2b74");

        this.testCreateDocumentRequest = new CreateDocumentRequest();
        this.testCreateDocumentRequest.setTitle("New Document");
        this.testCreateDocumentRequest.setStatus(Status.PRIVATE);
        this.testCreateDocumentRequest.setTemplateId(1);

        this.testDocumentResponse = new DocumentResponse();
        this.testDocumentResponse.setId(testDocumentId);
        this.testDocumentResponse.setTitle("New Document");
        this.testDocumentResponse.setOwnerId(testUserId);
        this.testDocumentResponse.setStatus(Status.PRIVATE);
        this.testDocumentResponse.setShared(false);
        this.testDocumentResponse.setBranchCount(1);
        this.testDocumentResponse.setTrashed(false);
        this.testDocumentResponse.setFavorite(false);
        this.testDocumentResponse.setMainBranchId(UUID.fromString("e3fab0a2-be56-42ef-a35f-60c299fb1e05"));
        this.testDocumentResponse.setCreatedAt(LocalDateTime.now());
        this.testDocumentResponse.setUpdatedAt(LocalDateTime.now());

        this.testDocument = new Document();
        this.testDocument.setId(1L);
        this.testDocument.setPublicId(testDocumentId);
        this.testDocument.setTitle("New Document");
        this.testDocument.setStatus(Status.PRIVATE);
        this.testDocument.setTrashed(false);
        this.testDocument.setFavorite(false);
        this.testDocument.setCreatedAt(LocalDateTime.now());
        this.testDocument.setUpdatedAt(LocalDateTime.now());

        this.testUpdateDocumentRequest = new UpdateDocumentRequest();
        this.testUpdateDocumentRequest.setTitle("Updated Title");

        this.testTemplate = new Template();
        this.testTemplate.setId(1);
        this.testTemplate.setTemplate("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"}}]}");
        this.testTemplate.setTitle("Test Template");

        this.testUser = new User();
        this.testUser.setId(testUserId);
        this.testUser.setUsername("Test User");
        this.testUser.setEmail("testuser@email.com");
        this.testUser.setPassword("test12345");
    }

    @Nested
    @DisplayName("Create Document Tests")
    class CreateDocumentTests {

        @Test
        @DisplayName("Should create document successfully with valid request")
        void shouldCreateDocumentSuccessfully() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<DocumentFactory> mockedFactory = mockStatic(DocumentFactory.class)) {

                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                mockedFactory.when(() -> DocumentFactory.createNewDocument(
                                any(User.class), anyString(), any(Status.class), any(Template.class)))
                        .thenReturn(testDocument);

                when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
                when(templateRepository.findById(testCreateDocumentRequest.getTemplateId()))
                        .thenReturn(Optional.of(testTemplate));
                when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
                when(documentMapper.toDto(testDocument)).thenReturn(testDocumentResponse);

                DocumentResponse result = documentService.createDocument(testCreateDocumentRequest);

                assertThat(result).isNotNull();
                assertThat(result.getTitle()).isEqualTo("New Document");
                assertThat(result.getStatus()).isEqualTo(Status.PRIVATE);
                verify(documentRepository, times(1)).save(any(Document.class));
                verify(documentMapper, times(1)).toDto(testDocument);
            }
        }

        @Test
        @DisplayName("Should throw TemplateNotFoundException when template does not exist")
        void shouldThrowTemplateNotFoundExceptionWhenTemplateNotFound() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
                when(templateRepository.findById(testCreateDocumentRequest.getTemplateId()))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> documentService.createDocument(testCreateDocumentRequest))
                        .isInstanceOf(TemplateNotFoundException.class);

                verify(documentRepository, never()).save(any(Document.class));
            }
        }
    }

    @Nested
    @DisplayName("Get Document tests")
    class GetDocumentTests {

        @Test
        @DisplayName("Should get document successfully when user is authorized")
        void shouldGetDocumentSuccessfully() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<com.nipun.system.document.permission.PermissionUtils> mockedPermission =
                         mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                mockedPermission.when(() -> com.nipun.system.document.permission.PermissionUtils
                        .isUnauthorizedUser(testUserId, testDocument)).thenReturn(false);

                when(documentRepository.findByPublicId(testDocumentId)).thenReturn(Optional.of(testDocument));
                when(documentMapper.toDto(testDocument)).thenReturn(testDocumentResponse);

                DocumentResponse result = documentService.getDocument(testDocumentId);

                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(testDocumentId);
                verify(documentRepository, times(1)).findByPublicId(testDocumentId);
            }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document does not exist")
        void shouldThrowDocumentNotFoundExceptionWhenDocumentNotFound() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                when(documentRepository.findByPublicId(testDocumentId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> documentService.getDocument(testDocumentId))
                        .isInstanceOf(DocumentNotFoundException.class);
            }
        }

        @Test
        @DisplayName("Should throw UnauthorizedDocumentException when user is not authorized")
        void shouldThrowUnauthorizedExceptionWhenUserNotAuthorized() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class);
                 MockedStatic<com.nipun.system.document.permission.PermissionUtils> mockedPermission =
                         mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                mockedPermission.when(() -> com.nipun.system.document.permission.PermissionUtils
                        .isUnauthorizedUser(testUserId, testDocument)).thenReturn(true);

                when(documentRepository.findByPublicId(testDocumentId)).thenReturn(Optional.of(testDocument));

                assertThatThrownBy(() -> documentService.getDocument(testDocumentId))
                        .isInstanceOf(UnauthorizedDocumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("Get Document Count tests")
    class GetDocumentCountTests {

        @Test
        @DisplayName("Should return correct document count")
        void shouldReturnCorrectDocumentCount() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                when(documentRepository.countAllByOwnerIdAndTrashedIsFalse(testUserId)).thenReturn(5);

                CountResponse result = documentService.getDocumentCount();

                assertThat(result).isNotNull();
                assertThat(result.getCount()).isEqualTo(5L);
                verify(documentRepository, times(1)).countAllByOwnerIdAndTrashedIsFalse(testUserId);
            }
        }
    }

    @Nested
    @DisplayName("Get All Documents tests")
    class GetAllDocumentsTests {

        @Test
        @DisplayName("Should return paginated documents successfully")
        void shouldReturnPaginatedDocuments() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);

                Document doc2 = new Document();
                doc2.setId(2L);
                doc2.setTitle("Document 2");

                List<Document> documents = Arrays.asList(testDocument, doc2);
                Page<Document> documentPage = new PageImpl<>(documents, PageRequest.of(0, 10), 2);

                when(documentRepository.findAllByOwnerId(eq(testUserId), any(PageRequest.class)))
                        .thenReturn(documentPage);
                when(documentMapper.toDto(any(Document.class))).thenReturn(testDocumentResponse);

                PaginatedData result = documentService.getAllDocuments(0, 10);

                assertThat(result).isNotNull();
                assertThat(result.getData()).isNotNull();
                assertThat(result.getTotalElements()).isEqualTo(2);
                assertThat(result.getPageNumber()).isEqualTo(0);
                assertThat(result.getPageSize()).isEqualTo(10);
                verify(documentRepository, times(1)).findAllByOwnerId(eq(testUserId), any(PageRequest.class));
            }
        }
    }

    @Nested
    @DisplayName("Update Document tests")
    class UpdateDocumentTests {

        @Test
        @DisplayName("Should update document successfully")
        void shouldUpdateDocumentSuccessfully() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.of(testDocument));
                when(documentRepository.save(testDocument)).thenReturn(testDocument);
                when(documentMapper.toDto(testDocument)).thenReturn(testDocumentResponse);

                DocumentResponse result = documentService.updateDocument(testDocumentId, testUpdateDocumentRequest);

                assertThat(result).isNotNull();
                assertThat(testDocument.getTitle()).isEqualTo("Updated Title");
                verify(documentRepository, times(1)).save(testDocument);
            }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        void shouldThrowExceptionWhenDocumentNotFoundForUpdate() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> documentService.updateDocument(testDocumentId, testUpdateDocumentRequest))
                        .isInstanceOf(DocumentNotFoundException.class);

                verify(documentRepository, never()).save(any(Document.class));
            }
        }
    }

    @Nested
    @DisplayName("Toggle Favorite tests")
    class ToggleFavoriteTests {

        @Test
        @DisplayName("Should toggle favorite from false to true")
        void shouldToggleFavoriteFromFalseToTrue() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                testDocument.setFavorite(false);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.of(testDocument));
                when(documentRepository.save(testDocument)).thenReturn(testDocument);
                when(documentMapper.toDto(testDocument)).thenReturn(testDocumentResponse);

                DocumentResponse result = documentService.toggleFavorite(testDocumentId);

                assertThat(result).isNotNull();
                assertThat(testDocument.getFavorite()).isTrue();
                verify(documentRepository, times(1)).save(testDocument);
            }
        }

        @Test
        @DisplayName("Should toggle favorite from true to false")
        void shouldToggleFavoriteFromTrueToFalse() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                testDocument.setFavorite(true);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.of(testDocument));
                when(documentRepository.save(testDocument)).thenReturn(testDocument);
                when(documentMapper.toDto(testDocument)).thenReturn(testDocumentResponse);

                DocumentResponse result = documentService.toggleFavorite(testDocumentId);

                assertThat(result).isNotNull();
                assertThat(testDocument.getFavorite()).isFalse();
                verify(documentRepository, times(1)).save(testDocument);
            }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when toggling favorite on non-existent document")
        void shouldThrowExceptionWhenTogglingFavoriteOnNonExistentDocument() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> documentService.toggleFavorite(testDocumentId))
                        .isInstanceOf(DocumentNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("Get Document Favorite Count tests")
    class GetDocumentFavoriteCountTests {

        @Test
        @DisplayName("Should return correct favorite document count")
        void shouldReturnCorrectFavoriteCount() {
            try (MockedStatic<UserIdUtils> mockedUtils = mockStatic(UserIdUtils.class)) {
                mockedUtils.when(UserIdUtils::getUserIdFromContext).thenReturn(testUserId);
                when(documentRepository.countAllFavoriteDocumentByUser(testUserId)).thenReturn(3);

                CountResponse result = documentService.getDocumentFavoriteCount();

                assertThat(result).isNotNull();
                assertThat(result.getCount()).isEqualTo(3L);
                verify(documentRepository, times(1)).countAllFavoriteDocumentByUser(testUserId);
            }
        }
    }
}