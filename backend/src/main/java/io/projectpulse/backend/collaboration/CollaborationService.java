package io.projectpulse.backend.collaboration;

import io.projectpulse.backend.activity.Activity;
import io.projectpulse.backend.activity.ActivityRepository;
import io.projectpulse.backend.collaboration.dto.*;
import io.projectpulse.backend.common.BadRequestException;
import io.projectpulse.backend.common.ResourceNotFoundException;
import io.projectpulse.backend.project.Project;
import io.projectpulse.backend.project.ProjectRepository;
import io.projectpulse.backend.project.ProjectService;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollaborationService {

    private final MessageRepository messageRepository;
    private final ProjectNoteRepository noteRepository;
    private final StatusUpdateRepository statusUpdateRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    public CollaborationService(MessageRepository messageRepository,
                                ProjectNoteRepository noteRepository,
                                StatusUpdateRepository statusUpdateRepository,
                                ProjectRepository projectRepository,
                                ProjectService projectService,
                                UserRepository userRepository,
                                ActivityRepository activityRepository) {
        this.messageRepository = messageRepository;
        this.noteRepository = noteRepository;
        this.statusUpdateRepository = statusUpdateRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional
    public MessageResponse sendMessage(Long projectId, SendMessageRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        projectService.validateProjectAccess(project, userId);
        User user = userRepository.findById(userId).orElseThrow();

        Message message = new Message(project, user, request.getContent().trim());
        message = messageRepository.save(message);

        return mapToMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getRecentMessages(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        projectService.validateProjectAccess(project, userId);

        List<Message> messages = messageRepository.findTop50ByProjectIdOrderByCreatedAtDesc(projectId);
        Collections.reverse(messages); // Oldest first for chat timeline
        return messages.stream().map(this::mapToMessageResponse).collect(Collectors.toList());
    }

    @Transactional
    public NoteResponse createNote(Long projectId, CreateNoteRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        projectService.validateProjectAccess(project, userId);
        User user = userRepository.findById(userId).orElseThrow();

        Activity activity = null;
        if (request.getActivityId() != null) {
            activity = activityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + request.getActivityId()));
            if (!activity.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Activity does not belong to this project");
            }
        }

        ProjectNote note = new ProjectNote(project, user, activity, request.getTitle().trim(), request.getContent().trim());
        note = noteRepository.save(note);

        return mapToNoteResponse(note);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getProjectNotes(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        projectService.validateProjectAccess(project, userId);

        return noteRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::mapToNoteResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StatusUpdateResponse createStatusUpdate(Long projectId, CreateStatusUpdateRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        projectService.validateProjectAccess(project, userId);
        User user = userRepository.findById(userId).orElseThrow();

        StatusUpdate update = new StatusUpdate(
                project,
                user,
                request.getStatusLabel().trim().toUpperCase(),
                request.getSummary().trim(),
                request.getDetails() != null ? request.getDetails().trim() : null
        );
        update = statusUpdateRepository.save(update);

        return mapToStatusUpdateResponse(update);
    }

    @Transactional(readOnly = true)
    public List<StatusUpdateResponse> getStatusUpdates(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        projectService.validateProjectAccess(project, userId);

        return statusUpdateRepository.findTop10ByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::mapToStatusUpdateResponse)
                .collect(Collectors.toList());
    }

    private MessageResponse mapToMessageResponse(Message msg) {
        return new MessageResponse(
                msg.getId(),
                msg.getProject().getId(),
                msg.getUser().getId(),
                msg.getUser().getFullName(),
                msg.getUser().getEmail(),
                msg.getContent(),
                msg.getCreatedAt()
        );
    }

    private NoteResponse mapToNoteResponse(ProjectNote note) {
        return new NoteResponse(
                note.getId(),
                note.getProject().getId(),
                note.getUser().getId(),
                note.getUser().getFullName(),
                note.getActivity() != null ? note.getActivity().getId() : null,
                note.getActivity() != null ? note.getActivity().getFilePath() : null,
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    private StatusUpdateResponse mapToStatusUpdateResponse(StatusUpdate su) {
        return new StatusUpdateResponse(
                su.getId(),
                su.getProject().getId(),
                su.getUser().getId(),
                su.getUser().getFullName(),
                su.getStatusLabel(),
                su.getSummary(),
                su.getDetails(),
                su.getCreatedAt()
        );
    }
}
