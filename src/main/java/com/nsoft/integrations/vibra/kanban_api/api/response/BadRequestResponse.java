package com.nsoft.integrations.vibra.kanban_api.api.response;

import java.util.Map;

public record BadRequestResponse(String message, Map<String, String> errors) {}
