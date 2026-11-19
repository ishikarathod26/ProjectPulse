package io.projectpulse.backend.collaboration;

import io.projectpulse.backend.activity.ActivityRepository;
import io.projectpulse.backend.collaboration.dto.*;
import io.projectpulse.backend.organization.Organization;
import io.projectpulse.backend.project.Project;
import io.projectpulse.backend.project.ProjectRepository;
import io.projectpulse.backend.project.ProjectService;
import io.projectpulse.backend.user.SystemRole;
import io.projectpulse.backend.user.User;
import io.projectpulse.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CollaborationServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ProjectNoteRepository noteRepository;

    @Mock
    private StatusUpdateRepository statusUpdateRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityRepository activityRepository;

    private CollaborationService collaborationService;

    @BeforeEach
    void setUp() {
        collaborationService = new CollaborationService(
                messageRepository,
                noteRepository,
                statusUpdateRepository,
                projectRepository,
                projectService,
                userRepository,
                activityRepository
        );
    }

    @Test
    void sendMessage_Success() {
        User user = new User("dev@example.com", "hash", "Dev User", SystemRole.MEMBER);
        user.setId(5L);
        Project project = new Project(new Organization(), "Core", "CORE", "Desc", user);
        project.setId(10L);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        Message savedMsg = new Message(project, user, "Hello team!");
        savedMsg.setId(100L);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMsg);

        SendMessageRequest request = new SendMessageRequest("Hello team!");
        MessageResponse response = collaborationService.sendMessage(10L, request, 5L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Hello team!", response.getContent());
        assertEquals("Dev User", response.getUserFullName());
        verify(projectService).validateProjectAccess(project, 5L);
    }

    @Test
    void createNote_Success() {
        User user = new User("dev@example.com", "hash", "Dev User", SystemRole.MEMBER);
        user.setId(5L);
        Project project = new Project(new Organization(), "Core", "CORE", "Desc", user);
        project.setId(10L);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        ProjectNote savedNote = new ProjectNote(project, user, null, "Meeting Notes", "Discussed milestone 1");
        savedNote.setId(200L);
        when(noteRepository.save(any(ProjectNote.class))).thenReturn(savedNote);

        CreateNoteRequest request = new CreateNoteRequest("Meeting Notes", "Discussed milestone 1", null);
        NoteResponse response = collaborationService.createNote(10L, request, 5L);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals("Meeting Notes", response.getTitle());
    }

    @Test
    void createStatusUpdate_Success() {
        User user = new User("dev@example.com", "hash", "Dev User", SystemRole.MEMBER);
        user.setId(5L);
        Project project = new Project(new Organization(), "Core", "CORE", "Desc", user);
        project.setId(10L);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        StatusUpdate update = new StatusUpdate(project, user, "ON_TRACK", "Sprint 1 Green", "All PRs merged");
        update.setId(300L);
        when(statusUpdateRepository.save(any(StatusUpdate.class))).thenReturn(update);

        CreateStatusUpdateRequest request = new CreateStatusUpdateRequest("ON_TRACK", "Sprint 1 Green", "All PRs merged");
        StatusUpdateResponse response = collaborationService.createStatusUpdate(10L, request, 5L);

        assertNotNull(response);
        assertEquals(300L, response.getId());
        assertEquals("ON_TRACK", response.getStatusLabel());
        assertEquals("Sprint 1 Green", response.getSummary());
    }
}
