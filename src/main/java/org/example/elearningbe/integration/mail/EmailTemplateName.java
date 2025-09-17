package org.example.elearningbe.integration.mail;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailTemplateName {
    ACTIVATE_ACCOUNT("activate-account");

    private final String templateName;
}