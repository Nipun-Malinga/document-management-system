package com.nipun.system.document.template;

import com.nipun.system.document.template.dtos.TemplateRequest;
import com.nipun.system.document.template.dtos.TemplateResponse;
import com.nipun.system.document.template.exceptions.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TemplateService {
    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;

    public TemplateResponse createTemplate(TemplateRequest request) {
        var template = templateMapper.toEntity(request);
        return templateMapper.toDto(templateRepository.save(template));
    }

    public List<TemplateResponse> getAllTemplates() {
        var templates = templateRepository.findAll();
        return templates.stream().map(templateMapper::toDto).toList();
    }


    public TemplateResponse getTemplate(long templateId) {
        var template = templateRepository.findById(templateId).orElseThrow(TemplateNotFoundException::new);
        return templateMapper.toDto(template);
    }

    public TemplateResponse updateTemplate(long templateId, TemplateRequest request) {
        var template = templateRepository.findById(templateId).orElseThrow(TemplateNotFoundException::new);
        template.setTitle(request.getTitle());
        template.setTemplate(request.getTemplate());

        return templateMapper.toDto(templateRepository.save(template));
    }

    public void deleteTemplate(long templateId) {
        var template = templateRepository.findById(templateId).orElseThrow(TemplateNotFoundException::new);
        templateRepository.delete(template);
    }
}
