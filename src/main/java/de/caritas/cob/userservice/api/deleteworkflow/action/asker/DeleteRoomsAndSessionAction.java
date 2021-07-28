package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ROCKET_CHAT;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatDeleteGroupException;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataRepository;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class DeleteRoomsAndSessionAction {

  protected final @NonNull SessionRepository sessionRepository;
  protected final @NonNull SessionDataRepository sessionDataRepository;
  protected final @NonNull MonitoringRepository monitoringRepository;
  protected final @NonNull RocketChatService rocketChatService;

  void deleteRocketChatGroup(String rcGroupId,
      List<DeletionWorkflowError> workflowErrors) {
    if (isNotBlank(rcGroupId)) {
      try {
        this.rocketChatService.deleteGroupAsTechnicalUser(rcGroupId);
      } catch (RocketChatDeleteGroupException e) {
        LogService.logDeleteWorkflowError(e);
        workflowErrors.add(
            DeletionWorkflowError.builder()
                .deletionSourceType(ASKER)
                .deletionTargetType(ROCKET_CHAT)
                .identifier(rcGroupId)
                .reason("Deletion of Rocket.Chat group failed")
                .timestamp(nowInUtc())
                .build()
        );
      }
    }
  }

  void deleteMonitorings(Session session, List<DeletionWorkflowError> workflowErrors) {
    try {
      var monitorings = this.monitoringRepository.findBySessionId(session.getId());
      this.monitoringRepository.deleteAll(monitorings);
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      workflowErrors.add(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DATABASE)
              .identifier(String.valueOf(session.getId()))
              .reason("Unable to delete monitorings from session")
              .timestamp(nowInUtc())
              .build()
      );
    }
  }

  void deleteSessionData(Session session, List<DeletionWorkflowError> workflowErrors) {
    try {
      var sessionData = this.sessionDataRepository.findBySessionId(session.getId());
      this.sessionDataRepository.deleteAll(sessionData);
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      workflowErrors.add(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DATABASE)
              .identifier(String.valueOf(session.getId()))
              .reason("Unable to delete session data from session")
              .timestamp(nowInUtc())
              .build()
      );
    }
  }

  protected void deleteSession(Session session, List<DeletionWorkflowError> workflowErrors) {
    try {
      this.sessionRepository.delete(session);
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      workflowErrors.add(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DATABASE)
              .identifier(String.valueOf(session.getId()))
              .reason("Unable to delete session")
              .timestamp(nowInUtc())
              .build()
      );
    }
  }

  void performSessionDeletion(Session session, List<DeletionWorkflowError> workflowErrors) {

    deleteRocketChatGroup(session.getGroupId(), workflowErrors);
    deleteRocketChatGroup(session.getFeedbackGroupId(), workflowErrors);
    deleteMonitorings(session, workflowErrors);
    deleteSessionData(session, workflowErrors);
    deleteSession(session, workflowErrors);

  }

}
