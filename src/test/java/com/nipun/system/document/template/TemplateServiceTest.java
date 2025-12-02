package com.nipun.system.document.template;

import com.nipun.system.document.template.dtos.TemplateRequest;
import com.nipun.system.document.template.dtos.TemplateResponse;
import com.nipun.system.document.template.exceptions.TemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Template Service Unit Test")
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateMapper templateMapper;

    @InjectMocks
    private TemplateService templateService;

    private long templateId;
    private Template template;
    private TemplateRequest templateRequest;
    private TemplateResponse templateResponse;

    @BeforeEach
    void setup() {
        templateId = 1L;

        templateRequest = new TemplateRequest();
        templateRequest.setTemplate("<html>Template Content</html>");
        templateRequest.setTitle("Test Template");

        template = new Template();
        template.setId(templateId);
        template.setTemplate("<html>Template Content</html>");
        template.setTitle("Test Template");

        templateResponse = new TemplateResponse();
        templateResponse.setId(templateId);
        templateResponse.setTemplate("<html>Template Content</html>");
        templateResponse.setTitle("Test Template");
    }

    @Nested
    @DisplayName("Create Template Tests")
    class CreateTemplateTests {

        @Test
        @DisplayName("Should create template successfully")
        void shouldCreateTemplateSuccessfully() {
            when(templateMapper.toEntity(templateRequest)).thenReturn(template);
            when(templateRepository.save(template)).thenReturn(template);
            when(templateMapper.toDto(template)).thenReturn(templateResponse);

            TemplateResponse result = templateService.createTemplate(templateRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(templateId);
            assertThat(result.getTitle()).isEqualTo("Test Template");
            assertThat(result.getTemplate()).isEqualTo("<html>Template Content</html>");

            verify(templateMapper).toEntity(templateRequest);
            verify(templateRepository).save(template);
            verify(templateMapper).toDto(template);
        }

        @Test
        @DisplayName("Should handle null request gracefully")
        void shouldHandleNullRequest() {
            when(templateMapper.toEntity(null)).thenReturn(template);
            when(templateRepository.save(template)).thenReturn(template);
            when(templateMapper.toDto(template)).thenReturn(templateResponse);

            TemplateResponse result = templateService.createTemplate(null);

            assertThat(result).isNotNull();
            verify(templateMapper).toEntity(null);
            verify(templateRepository).save(template);
        }
    }

    @Nested
    @DisplayName("Get All Templates Tests")
    class GetAllTemplatesTests {

        @Test
        @DisplayName("Should return all templates")
        void shouldReturnAllTemplates() {
            Template template2 = new Template();
            template2.setId(2L);
            template2.setTitle("Template 2");
            template2.setTemplate("<html>Content 2</html>");

            TemplateResponse response2 = new TemplateResponse();
            response2.setId(2L);
            response2.setTitle("Template 2");
            response2.setTemplate("<html>Content 2</html>");

            List<Template> templates = Arrays.asList(template, template2);

            when(templateRepository.findAll()).thenReturn(templates);
            when(templateMapper.toDto(template)).thenReturn(templateResponse);
            when(templateMapper.toDto(template2)).thenReturn(response2);

            List<TemplateResponse> result = templateService.getAllTemplates();

            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Template");
            assertThat(result.get(1).getTitle()).isEqualTo("Template 2");

            verify(templateRepository).findAll();
            verify(templateMapper, times(2)).toDto(any(Template.class));
        }

        @Test
        @DisplayName("Should return empty list when no templates exist")
        void shouldReturnEmptyListWhenNoTemplatesExist() {
            when(templateRepository.findAll()).thenReturn(List.of());

            List<TemplateResponse> result = templateService.getAllTemplates();

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(templateRepository).findAll();
            verify(templateMapper, never()).toDto(any(Template.class));
        }
    }

    @Nested
    @DisplayName("Get Template Tests")
    class GetTemplateTests {

        @Test
        @DisplayName("Should get template by id successfully")
        void shouldGetTemplateByIdSuccessfully() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(templateMapper.toDto(template)).thenReturn(templateResponse);

            TemplateResponse result = templateService.getTemplate(templateId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(templateId);
            assertThat(result.getTitle()).isEqualTo("Test Template");

            verify(templateRepository).findById(templateId);
            verify(templateMapper).toDto(template);
        }

        @Test
        @DisplayName("Should throw TemplateNotFoundException when template not found")
        void shouldThrowExceptionWhenTemplateNotFound() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> templateService.getTemplate(templateId))
                    .isInstanceOf(TemplateNotFoundException.class);

            verify(templateRepository).findById(templateId);
            verify(templateMapper, never()).toDto(any(Template.class));
        }

        @Test
        @DisplayName("Should throw exception for non-existent template id")
        void shouldThrowExceptionForNonExistentId() {
            long nonExistentId = 999L;
            when(templateRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> templateService.getTemplate(nonExistentId))
                    .isInstanceOf(TemplateNotFoundException.class);

            verify(templateRepository).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("Update Template Tests")
    class UpdateTemplateTests {

        @Test
        @DisplayName("Should update template successfully")
        void shouldUpdateTemplateSuccessfully() {
            TemplateRequest updateRequest = new TemplateRequest();
            updateRequest.setTitle("Updated Title");
            updateRequest.setTemplate("<html>Updated Content</html>");

            Template updatedTemplate = new Template();
            updatedTemplate.setId(templateId);
            updatedTemplate.setTitle("Updated Title");
            updatedTemplate.setTemplate("<html>Updated Content</html>");

            TemplateResponse updatedResponse = new TemplateResponse();
            updatedResponse.setId(templateId);
            updatedResponse.setTitle("Updated Title");
            updatedResponse.setTemplate("<html>Updated Content</html>");

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(templateRepository.save(template)).thenReturn(updatedTemplate);
            when(templateMapper.toDto(updatedTemplate)).thenReturn(updatedResponse);

            TemplateResponse result = templateService.updateTemplate(templateId, updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated Title");
            assertThat(result.getTemplate()).isEqualTo("<html>Updated Content</html>");

            verify(templateRepository).findById(templateId);
            verify(templateRepository).save(template);
            verify(templateMapper).toDto(updatedTemplate);
        }

        @Test
        @DisplayName("Should throw TemplateNotFoundException when updating non-existent template")
        void shouldThrowExceptionWhenUpdatingNonExistentTemplate() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> templateService.updateTemplate(templateId, templateRequest))
                    .isInstanceOf(TemplateNotFoundException.class);

            verify(templateRepository).findById(templateId);
            verify(templateRepository, never()).save(any(Template.class));
        }

        @Test
        @DisplayName("Should update only title when template content is same")
        void shouldUpdateOnlyTitle() {
            TemplateRequest updateRequest = new TemplateRequest();
            updateRequest.setTitle("New Title Only");
            updateRequest.setTemplate("<html>Template Content</html>");

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(templateRepository.save(template)).thenReturn(template);
            when(templateMapper.toDto(template)).thenReturn(templateResponse);

            TemplateResponse result = templateService.updateTemplate(templateId, updateRequest);

            assertThat(result).isNotNull();
            verify(templateRepository).findById(templateId);
            verify(templateRepository).save(template);
        }
    }

    @Nested
    @DisplayName("Delete Template Tests")
    class DeleteTemplateTests {

        @Test
        @DisplayName("Should delete template successfully")
        void shouldDeleteTemplateSuccessfully() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            doNothing().when(templateRepository).delete(template);

            templateService.deleteTemplate(templateId);

            verify(templateRepository).findById(templateId);
            verify(templateRepository).delete(template);
        }

        @Test
        @DisplayName("Should throw TemplateNotFoundException when deleting non-existent template")
        void shouldThrowExceptionWhenDeletingNonExistentTemplate() {
            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> templateService.deleteTemplate(templateId))
                    .isInstanceOf(TemplateNotFoundException.class);

            verify(templateRepository).findById(templateId);
            verify(templateRepository, never()).delete(any(Template.class));
        }

        @Test
        @DisplayName("Should handle delete for non-existent id")
        void shouldHandleDeleteForNonExistentId() {
            long nonExistentId = 999L;
            when(templateRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> templateService.deleteTemplate(nonExistentId))
                    .isInstanceOf(TemplateNotFoundException.class);

            verify(templateRepository).findById(nonExistentId);
            verify(templateRepository, never()).delete(any(Template.class));
        }
    }
}