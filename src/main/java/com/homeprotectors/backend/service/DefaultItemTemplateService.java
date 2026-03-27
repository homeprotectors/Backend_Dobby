package com.homeprotectors.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeprotectors.backend.dto.defaultitem.DefaultChoreTemplate;
import com.homeprotectors.backend.dto.defaultitem.DefaultItemTemplate;
import com.homeprotectors.backend.dto.defaultitem.DefaultStockTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

@Service
public class DefaultItemTemplateService {

    private static final String TEMPLATE_PATH = "default-items/default-items.json";

    private final ObjectMapper objectMapper;
    private DefaultItemTemplate cachedTemplate;

    public DefaultItemTemplateService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public synchronized DefaultItemTemplate getTemplate() {
        if (cachedTemplate == null) {
            cachedTemplate = loadAndValidateTemplate();
        }
        return cachedTemplate;
    }

    private DefaultItemTemplate loadAndValidateTemplate() {
        try (InputStream inputStream = new ClassPathResource(TEMPLATE_PATH).getInputStream()) {
            DefaultItemTemplate template = objectMapper.readValue(inputStream, DefaultItemTemplate.class);
            validateTemplate(template);
            return template;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load default item template.", e);
        }
    }

    private void validateTemplate(DefaultItemTemplate template) {
        if (template == null) {
            throw new IllegalStateException("Default item template must not be null.");
        }

        if (template.version() <= 0) {
            throw new IllegalStateException("Default item template version must be positive.");
        }

        for (DefaultChoreTemplate chore : safeList(template.chores())) {
            validateChoreTemplate(chore);
        }

        for (DefaultStockTemplate stock : safeList(template.stocks())) {
            validateStockTemplate(stock);
        }
    }

    private void validateChoreTemplate(DefaultChoreTemplate template) {
        if (template == null) {
            throw new IllegalStateException("Default chore template must not be null.");
        }
        if (isBlank(template.title())) {
            throw new IllegalStateException("Default chore title must not be blank.");
        }
        if (template.recurrenceType() == null) {
            throw new IllegalStateException("Default chore recurrenceType must not be null.");
        }
        if (template.roomCategory() == null) {
            throw new IllegalStateException("Default chore roomCategory must not be null.");
        }

        Set<String> selectedCycle = template.selectedCycle() == null ? Collections.emptySet() : template.selectedCycle();
        if (!template.recurrenceType().isValidSelection(selectedCycle)) {
            throw new IllegalStateException("Invalid selectedCycle for default chore: " + template.title());
        }
    }

    private void validateStockTemplate(DefaultStockTemplate template) {
        if (template == null) {
            throw new IllegalStateException("Default stock template must not be null.");
        }
        if (isBlank(template.name())) {
            throw new IllegalStateException("Default stock name must not be blank.");
        }
        if (template.unitQuantity() == null || template.unitQuantity() < 0) {
            throw new IllegalStateException("Default stock unitQuantity must be zero or greater.");
        }
        if (template.unitDays() == null || template.unitDays() < 1) {
            throw new IllegalStateException("Default stock unitDays must be one or greater.");
        }
        if (template.updatedQuantity() == null || template.updatedQuantity() < 0) {
            throw new IllegalStateException("Default stock updatedQuantity must be zero or greater.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static <T> java.util.List<T> safeList(java.util.List<T> values) {
        return values == null ? Collections.emptyList() : values.stream().filter(Objects::nonNull).toList();
    }
}
