package com.nipun.system.document.template.exceptions;

public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException() {
        super("Template not found");
    }

    public TemplateNotFoundException(String message) {
        super(message);
    }
}
