package de.caritas.cob.userservice.api.workflow.delete.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.ALL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.workflow.delete.service.provider.InactivePrivateGroupsProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteInactiveSessionsAndUserServiceTest {

  @InjectMocks private DeleteInactiveSessionsAndUserService deleteInactiveSessionsAndUserService;

  @Mock private WorkflowErrorMailService workflowErrorMailService;
  @Mock private WorkflowErrorLogService workflowErrorLogService;
  @Mock private UserRepository userRepository;
  @Mock private SessionRepository sessionRepository;
  @Mock private DeleteUserAccountService deleteUserAccountService;
  @Mock private DeleteSessionService deleteSessionService;
  @Mock private InactivePrivateGroupsProvider inactivePrivateGroupsProvider;

  @Test
  void deleteInactiveSessionsAndUsers_Should_SendWorkflowErrorsMail_When_userNotFoundReason() {
    // given
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap =
        new HashMap<>() {
          {
            put(user.getUserId(), Collections.singletonList(session.getGroupId()));
          }
        };
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Collections.singletonList(session));
    DeletionWorkflowError deletionWorkflowError = Mockito.mock(DeletionWorkflowError.class);
    when(deleteUserAccountService.performUserDeletion(user))
        .thenReturn(Collections.singletonList(deletionWorkflowError));

    // when
    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    // then
    verify(workflowErrorLogService, Mockito.times(1)).logWorkflowErrors(Collections.emptyList());
    verify(workflowErrorMailService, Mockito.times(1))
        .buildAndSendErrorMail(argThat(list -> !list.isEmpty()));
  }

  @Test
  void
      deleteInactiveSessionsAndUsers_Should_DeleteEntireUserAccount_WhenUserHasOnlyInactiveSessions() {
    // given
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Session session2 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap =
        new HashMap<>() {
          {
            put(user.getUserId(), Arrays.asList(session1.getGroupId(), session2.getGroupId()));
          }
        };
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Arrays.asList(session1, session2));

    // when
    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    // then
    verify(deleteUserAccountService, Mockito.times(1)).performUserDeletion(user);
  }

  @Test
  void
      deleteInactiveSessionsAndUsers_Should_DeleteSingleSession_WhenUserHasActiveAndInactiveSessions() {
    // given
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Session session2 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap =
        new HashMap<>() {
          {
            put(user.getUserId(), Collections.singletonList(session1.getGroupId()));
          }
        };
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Arrays.asList(session1, session2));

    // when
    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    // then
    verify(deleteSessionService, Mockito.times(1)).performSessionDeletion(session1);
  }

  @Test
  void
      deleteInactiveSessionsAndUsers_Should_logWorkflowErrorMail_WhenUserHasActiveAndInactiveSessionsAndHasErrors() {
    // given
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Session session2 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap =
        new HashMap<>() {
          {
            put(user.getUserId(), Collections.singletonList(session1.getGroupId()));
          }
        };
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Arrays.asList(session1, session2));
    DeletionWorkflowError deletionWorkflowError =
        DeletionWorkflowError.builder()
            .deletionSourceType(ASKER)
            .deletionTargetType(ALL)
            .identifier(null)
            .reason("Session with rc group id could not be found.")
            .timestamp(nowInUtc())
            .build();
    when(deleteSessionService.performSessionDeletion(session1))
        .thenReturn(Collections.singletonList(deletionWorkflowError));

    // when
    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    // then
    verify(workflowErrorLogService, Mockito.times(1))
        .logWorkflowErrors(argThat(list -> !list.isEmpty()));
    verify(workflowErrorMailService, Mockito.times(1))
        .buildAndSendErrorMail(Collections.emptyList());
  }

  @Test
  void
      deleteInactiveSessionsAndUsers_Should_notLogError_WhenSessionCouldNotBeFound_BecauseItMayHaveBeenDeletedByPreviousWorkflowRun() {
    // given
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Session session2 = easyRandom.nextObject(Session.class);
    Session session3 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap =
        new HashMap<>() {
          {
            put(user.getUserId(), Collections.singletonList(session1.getGroupId()));
          }
        };
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.of(user));
    when(sessionRepository.findByUser(user)).thenReturn(Arrays.asList(session2, session3));

    // when
    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    // then
    verify(workflowErrorLogService, Mockito.never()).logWorkflowErrors(Mockito.anyList());
    verify(workflowErrorMailService, Mockito.never()).buildAndSendErrorMail(Mockito.anyList());
  }

  @Test
  void deleteInactiveSessionsAndUsers_Should_AttemptToDeleteUserSessions_WhenUserCouldNotBeFound() {
    // given
    EasyRandom easyRandom = new EasyRandom();
    User user = easyRandom.nextObject(User.class);
    Session session1 = easyRandom.nextObject(Session.class);
    Map<String, List<String>> userWithInactiveGroupsMap =
        new HashMap<>() {
          {
            put(user.getUserId(), Collections.singletonList(session1.getGroupId()));
          }
        };
    when(inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap())
        .thenReturn(userWithInactiveGroupsMap);
    when(userRepository.findByRcUserIdAndDeleteDateIsNull(anyString()))
        .thenReturn(Optional.empty());

    // when
    deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();

    // then
    deleteSessionService.performSessionDeletion(session1);
  }
}
