package com.marius.access_management_service;

import java.util.Map;

public record ValidationErrorResponse(String error, Map<String, String> fields) {
}
