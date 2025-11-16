package com.nipun.system.document.template;

import com.nipun.system.document.template.dtos.TemplateRequest;
import com.nipun.system.document.template.dtos.TemplateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/documents/templates")
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplates(
            @RequestBody @Valid TemplateRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        var template = templateService.createTemplate(request);

        var uri = uriBuilder
                .path("/documents/templates/{templateId}")
                .buildAndExpand(template.getId())
                .toUri();
        return ResponseEntity.created(uri).body(template);
    }

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> getTemplate(
            @PathVariable(name = "templateId") long templateId
    ) {
        var templateResponse = templateService.getTemplate(templateId);
        return ResponseEntity.ok(templateResponse);
    }

    @PatchMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> getResponse(
            @PathVariable(name = "templateId") long templateId,
            @RequestBody @Valid TemplateRequest request
    ) {
        var templateResponse = templateService.updateTemplate(templateId, request);
        return ResponseEntity.ok(templateResponse);
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable(name = "templateId") long templateId
    ) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}
