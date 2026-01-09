package com.nipun.system.document.template;

import com.nipun.system.document.template.dtos.TemplateRequest;
import com.nipun.system.document.template.dtos.TemplateResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/documents/templates")
public class TemplateController {

    private final TemplateService templateService;

    @RateLimiter(name = "globalLimiter")
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

    @RateLimiter(name = "globalLimiter")
    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @RateLimiter(name = "globalLimiter")
    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> getTemplate(
            @PathVariable(name = "templateId") long templateId
    ) {
        var templateResponse = templateService.getTemplate(templateId);
        return ResponseEntity.ok(templateResponse);
    }

    @RateLimiter(name = "globalLimiter")
    @PatchMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> getResponse(
            @PathVariable(name = "templateId") long templateId,
            @RequestBody @Valid TemplateRequest request
    ) {
        var templateResponse = templateService.updateTemplate(templateId, request);
        return ResponseEntity.ok(templateResponse);
    }

    @RateLimiter(name = "globalLimiter")
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable(name = "templateId") long templateId
    ) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}
