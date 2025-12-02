package com.nipun.system.document.share;

import com.nipun.system.document.base.Document;
import com.nipun.system.document.base.DocumentMapper;
import com.nipun.system.document.base.DocumentRepository;
import com.nipun.system.document.base.dtos.DocumentResponse;
import com.nipun.system.document.base.exceptions.DocumentNotFoundException;
import com.nipun.system.document.permission.exceptions.UnauthorizedDocumentException;
import com.nipun.system.document.share.dtos.ShareDocumentRequest;
import com.nipun.system.document.share.dtos.SharedDocumentResponse;
import com.nipun.system.shared.dtos.CountResponse;
import com.nipun.system.shared.dtos.PaginatedData;
import com.nipun.system.user.User;
import com.nipun.system.user.UserRepository;
import com.nipun.system.user.exceptions.UserNotFoundException;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SharedDocumentService Tests")
class SharedDocumentServiceTest {
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SharedDocumentRepository sharedDocumentRepository;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private SharedDocumentMapper sharedDocumentMapper;
    @Mock
    private SharedDocumentFactory sharedDocumentFactory;

    @InjectMocks
    private SharedDocumentService sharedDocumentService;

    private ShareDocumentRequest testShareDocumentRequest;
    private SharedDocumentResponse testSharedDocumentResponse;
    private UUID testDocumentId;
    private Long testUserId;
    private Long testSharedUserId;
    private Document testDocument;
    private User testSharedUser;
    private SharedDocument testSharedDocument;

    @BeforeEach
    void setup() {
        testDocumentId = UUID.randomUUID();
        testUserId = 1L;
        testSharedUserId = 2L;

        testShareDocumentRequest = new ShareDocumentRequest();
        testShareDocumentRequest.setUserId(testSharedUserId);
        testShareDocumentRequest.setPermission(Permission.READ_WRITE);

        testSharedDocumentResponse = new SharedDocumentResponse();
        testSharedDocumentResponse.setUserId(testSharedUserId);
        testSharedDocumentResponse.setEmail("shared@test.com");
        testSharedDocumentResponse.setUsername("shareduser");
        testSharedDocumentResponse.setPermission(Permission.READ_WRITE);

        User testOwner = new User();
        testOwner.setId(testUserId);
        testOwner.setEmail("owner@test.com");

        testSharedUser = new User();
        testSharedUser.setId(testSharedUserId);
        testSharedUser.setEmail("shared@test.com");

        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setPublicId(testDocumentId);

        testSharedDocument = new SharedDocument();
        testSharedDocument.setId(1L);
        testSharedDocument.setDocument(testDocument);
        testSharedDocument.setSharedUser(testSharedUser);
        testSharedDocument.setPermission(Permission.READ_ONLY);
    }

    @Nested
    @DisplayName("Share Document Tests")
    class ShareDocumentTests {

        @Test
        @DisplayName("Should successfully share document with new user")
        void shouldShareDocumentWithNewUser() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.of(testDocument));
                when(userRepository.findById(testSharedUserId))
                        .thenReturn(Optional.of(testSharedUser));
                when(sharedDocumentRepository.findByDocumentIdAndSharedUserId(testDocument.getId(), testSharedUserId))
                        .thenReturn(Optional.empty());
                when(sharedDocumentFactory.createNewSharedDocument(testSharedUser, testDocument))
                        .thenReturn(testSharedDocument);
                when(documentRepository.save(any(Document.class)))
                        .thenReturn(testDocument);
                when(sharedDocumentMapper.toSharedDocumentDto(testSharedDocument))
                        .thenReturn(testSharedDocumentResponse);

                SharedDocumentResponse result = sharedDocumentService.shareDocument(testDocumentId, testShareDocumentRequest);

                assertNotNull(result);
                assertEquals(testSharedUserId, result.getUserId());
                assertEquals(Permission.READ_WRITE, result.getPermission());

                verify(documentRepository).findByPublicIdAndOwnerId(testDocumentId, testUserId);
                verify(userRepository).findById(testSharedUserId);
                verify(sharedDocumentFactory).createNewSharedDocument(testSharedUser, testDocument);
                verify(documentRepository).save(testDocument);
                verify(sharedDocumentMapper).toSharedDocumentDto(testSharedDocument);
            }
        }

        @Test
        @DisplayName("Should update permission for existing shared document")
        void shouldUpdatePermissionForExistingShare() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.of(testDocument));
                when(userRepository.findById(testSharedUserId))
                        .thenReturn(Optional.of(testSharedUser));
                when(sharedDocumentRepository.findByDocumentIdAndSharedUserId(testDocument.getId(), testSharedUserId))
                        .thenReturn(Optional.of(testSharedDocument));
                when(documentRepository.save(any(Document.class)))
                        .thenReturn(testDocument);
                when(sharedDocumentMapper.toSharedDocumentDto(testSharedDocument))
                        .thenReturn(testSharedDocumentResponse);

                SharedDocumentResponse result = sharedDocumentService.shareDocument(testDocumentId, testShareDocumentRequest);

                assertNotNull(result);
                verify(sharedDocumentFactory, never()).createNewSharedDocument(any(), any());
                verify(documentRepository).save(testDocument);
            }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        void shouldThrowExceptionWhenDocumentNotFound() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.empty());

                assertThrows(DocumentNotFoundException.class,
                        () -> sharedDocumentService.shareDocument(testDocumentId, testShareDocumentRequest));

                verify(userRepository, never()).findById(any());
            }
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when shared user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.of(testDocument));
                when(userRepository.findById(testSharedUserId))
                        .thenReturn(Optional.empty());

                assertThrows(UserNotFoundException.class,
                        () -> sharedDocumentService.shareDocument(testDocumentId, testShareDocumentRequest));
            }
        }
    }

    @Nested
    @DisplayName("Get All Shared Users Tests")
    class GetAllSharedUsersTests {

        @Test
        @DisplayName("Should return list of shared users for document owner")
        void shouldReturnSharedUsersForOwner() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class);
                 MockedStatic<com.nipun.system.document.permission.PermissionUtils> permissionUtils =
                         mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                testDocument.getSharedUsers().add(testSharedDocument);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));
                permissionUtils.when(() -> com.nipun.system.document.permission.PermissionUtils
                                .isUnauthorizedUser(testUserId, testDocument))
                        .thenReturn(false);
                when(sharedDocumentMapper.toSharedDocumentDto(testSharedDocument))
                        .thenReturn(testSharedDocumentResponse);

                List<SharedDocumentResponse> result = sharedDocumentService.getAllSharedUsers(testDocumentId);

                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(testSharedUserId, result.getFirst().getUserId());

                verify(documentRepository).findByPublicId(testDocumentId);
            }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not found")
        void shouldThrowExceptionWhenDocumentNotFound() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.empty());

                assertThrows(DocumentNotFoundException.class,
                        () -> sharedDocumentService.getAllSharedUsers(testDocumentId));
            }
        }

        @Test
        @DisplayName("Should throw UnauthorizedDocumentException for unauthorized user")
        void shouldThrowExceptionForUnauthorizedUser() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class);
                 MockedStatic<com.nipun.system.document.permission.PermissionUtils> permissionUtils =
                         mockStatic(com.nipun.system.document.permission.PermissionUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicId(testDocumentId))
                        .thenReturn(Optional.of(testDocument));
                permissionUtils.when(() -> com.nipun.system.document.permission.PermissionUtils
                                .isUnauthorizedUser(testUserId, testDocument))
                        .thenReturn(true);

                assertThrows(UnauthorizedDocumentException.class,
                        () -> sharedDocumentService.getAllSharedUsers(testDocumentId));
            }
        }
    }

    @Nested
    @DisplayName("Get Shared Document Count Tests")
    class GetSharedDocumentCountTests {

        @Test
        @DisplayName("Should return count of shared documents for user")
        void shouldReturnSharedDocumentCount() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(sharedDocumentRepository.countAllBySharedUserId(testUserId))
                        .thenReturn(5);

                CountResponse result = sharedDocumentService.getSharedDocumentWithUserCount();

                assertNotNull(result);
                assertEquals(5L, result.getCount());

                verify(sharedDocumentRepository).countAllBySharedUserId(testUserId);
            }
        }
    }

    @Nested
    @DisplayName("Get All Shared Documents Tests")
    class GetAllSharedDocumentsTests {

        @Test
        @DisplayName("Should return paginated shared documents")
        void shouldReturnPaginatedSharedDocuments() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                List<Document> documents = List.of(testDocument);
                Page<Document> documentPage = new PageImpl<>(documents, PageRequest.of(0, 10), 1);
                DocumentResponse documentResponse = new DocumentResponse();

                when(documentRepository.findAllSharedDocumentsWithUser(eq(testUserId), any(PageRequest.class)))
                        .thenReturn(documentPage);
                when(documentMapper.toDto(testDocument))
                        .thenReturn(documentResponse);

                PaginatedData result = sharedDocumentService.getAllSharedDocumentsWithUser(0, 10);

                assertNotNull(result);
                assertEquals(0, result.getPageNumber());
                assertEquals(10, result.getPageSize());
                assertEquals(1, result.getTotalPages());
                assertEquals(1L, result.getTotalElements());

                verify(documentRepository).findAllSharedDocumentsWithUser(eq(testUserId), any(PageRequest.class));
            }
        }
    }

    @Nested
    @DisplayName("Remove Document Access Tests")
    class RemoveDocumentAccessTests {

        @Test
        @DisplayName("Should remove document access for shared user")
        void shouldRemoveAccessForSharedUser() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(sharedDocumentRepository.findByDocumentPublicIdAndSharedUserId(testDocumentId, testUserId))
                        .thenReturn(Optional.of(testSharedDocument));

                sharedDocumentService.removeDocumentAccess(testDocumentId);

                verify(sharedDocumentRepository).delete(testSharedDocument);
            }
        }

        @Test
        @DisplayName("Should throw UnauthorizedDocumentException when shared document not found")
        void shouldThrowExceptionWhenSharedDocumentNotFound() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(sharedDocumentRepository.findByDocumentPublicIdAndSharedUserId(testDocumentId, testUserId))
                        .thenReturn(Optional.empty());

                assertThrows(UnauthorizedDocumentException.class,
                        () -> sharedDocumentService.removeDocumentAccess(testDocumentId));

                verify(sharedDocumentRepository, never()).delete(any());
            }
        }

        @Test
        @DisplayName("Should remove document access by owner for specific user")
        void shouldRemoveAccessByOwnerForUser() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.of(testDocument));
                when(documentRepository.save(testDocument))
                        .thenReturn(testDocument);

                sharedDocumentService.removeDocumentAccess(testDocumentId, testSharedUserId);

                verify(documentRepository).findByPublicIdAndOwnerId(testDocumentId, testUserId);
                verify(documentRepository).save(testDocument);
            }
        }

        @Test
        @DisplayName("Should throw DocumentNotFoundException when removing access by non-owner")
        void shouldThrowExceptionWhenRemovingAccessByNonOwner() {
            try (MockedStatic<com.nipun.system.shared.utils.UserIdUtils> userIdUtils =
                         mockStatic(com.nipun.system.shared.utils.UserIdUtils.class)) {

                userIdUtils.when(com.nipun.system.shared.utils.UserIdUtils::getUserIdFromContext)
                        .thenReturn(testUserId);

                when(documentRepository.findByPublicIdAndOwnerId(testDocumentId, testUserId))
                        .thenReturn(Optional.empty());

                assertThrows(DocumentNotFoundException.class,
                        () -> sharedDocumentService.removeDocumentAccess(testDocumentId, testSharedUserId));

                verify(documentRepository, never()).save(any());
            }
        }
    }
}