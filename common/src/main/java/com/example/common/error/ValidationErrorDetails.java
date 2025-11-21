package com.example.common.error;

import java.util.Map;

public record ValidationErrorDetails(
        Map<String, String> fieldErrors
) {}
