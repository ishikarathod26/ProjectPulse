package io.projectpulse.backend.activity;

import io.projectpulse.backend.activity.dto.ActivityEventDto;
import io.projectpulse.backend.common.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ActivityValidator {

    private static final Pattern TRAVERSAL_PATTERN = Pattern.compile("(\\.\\.[/\\\\])");

    public void validateEvent(ActivityEventDto event) {
        if (event.getClientEventId() == null || event.getClientEventId().isBlank()) {
            throw new BadRequestException("Client event ID is missing");
        }
        if (event.getEventType() == null) {
            throw new BadRequestException("Event type is required");
        }
        if (event.getFilePath() == null || event.getFilePath().isBlank()) {
            throw new BadRequestException("File path is required");
        }

        // Prevent path traversal attacks
        if (TRAVERSAL_PATTERN.matcher(event.getFilePath()).find()) {
            throw new BadRequestException("Illegal path traversal detected in file path: " + event.getFilePath());
        }

        if (event.getEventTimestamp() == null) {
            throw new BadRequestException("Event timestamp is required");
        }
    }
}
