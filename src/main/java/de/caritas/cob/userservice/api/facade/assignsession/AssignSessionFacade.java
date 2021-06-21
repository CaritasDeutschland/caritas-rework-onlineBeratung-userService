package de.caritas.cob.userservice.api.facade.assignsession;

import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatRemoveFromGroupOperationService;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatRollbackService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps for accepting an enquiry and/or assigning a session to a
 * consultant.
 */
@Service
@RequiredArgsConstructor
public class AssignSessionFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull RocketChatRollbackService rocketChatRollbackService;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull EmailNotificationFacade emailNotificationFacade;
  private final @NonNull SessionToConsultantVerifier sessionToConsultantVerifier;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull UnauthorizedMembersProvider unauthorizedMembersProvider;

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   */
  public void assignSession(Session session, Consultant consultant) {
    var consultantSessionDTO = ConsultantSessionDTO.builder()
        .consultant(consultant)
        .session(session)
        .build();
    sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO);

    var initialConsultant = session.getConsultant();
    SessionStatus initialStatus = session.getStatus();
    List<GroupMemberDTO> initialMembers =
        this.rocketChatFacade.retrieveRocketChatMembers(session.getGroupId());
    List<GroupMemberDTO> initialFeedbackGroupMembers =
        this.rocketChatFacade.retrieveRocketChatMembers(session.getFeedbackGroupId());

    try {
      updateSessionInDatabase(session, consultant, initialStatus);
      updateRocketChatRooms(session, consultant);
    } catch (Exception exception) {
      initiateRollback(session, initialConsultant, initialStatus, initialMembers,
          initialFeedbackGroupMembers);
      throw exception;
    }
    sendEmailForConsultantChange(session, consultant);
  }

  private void updateRocketChatRooms(Session session, Consultant consultant) {
    addConsultantToRocketChatGroup(session.getGroupId(), consultant);
    var memberList =
        rocketChatFacade.retrieveRocketChatMembers(session.getGroupId());
    removeUnauthorizedMembersFromGroup(session, consultant, memberList);

    if (session.hasFeedbackChat()) {
      addConsultantToRocketChatGroup(session.getFeedbackGroupId(), consultant);
      var feedbackMemberList =
          rocketChatFacade.retrieveRocketChatMembers(session.getFeedbackGroupId());
      removeUnauthorizedMembersFromFeedbackGroup(session, consultant, feedbackMemberList);
    }
  }

  private void addConsultantToRocketChatGroup(String rcGroupId, Consultant consultant) {
    rocketChatFacade.addUserToRocketChatGroup(consultant.getRocketChatId(), rcGroupId);
    rocketChatFacade.removeSystemMessagesFromRocketChatGroup(rcGroupId);
  }

  private void updateSessionInDatabase(Session session, Consultant consultant,
      SessionStatus initialStatus) {
    sessionService.updateConsultantAndStatusForSession(session, consultant,
        initialStatus == SessionStatus.NEW ? SessionStatus.IN_PROGRESS : initialStatus);
  }

  private void removeUnauthorizedMembersFromGroup(Session session, Consultant consultant,
      List<GroupMemberDTO> memberList) {
    List<Consultant> consultantsToRemoveFromRocketChat =
        unauthorizedMembersProvider.obtainConsultantsToRemove(session.getGroupId(), session,
            consultant, memberList);

    RocketChatRemoveFromGroupOperationService
        .getInstance(this.rocketChatFacade, this.keycloakAdminClientService, this.consultingTypeManager)
        .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat))
        .removeFromGroupOrRollbackOnFailure();
  }

  private void removeUnauthorizedMembersFromFeedbackGroup(Session session,
      Consultant consultant, List<GroupMemberDTO> memberList) {
    List<Consultant> consultantsToRemoveFromRocketChat =
        unauthorizedMembersProvider.obtainConsultantsToRemove(session.getFeedbackGroupId(), session,
            consultant, memberList);

    RocketChatRemoveFromGroupOperationService
        .getInstance(this.rocketChatFacade, this.keycloakAdminClientService, this.consultingTypeManager)
        .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat))
        .removeFromFeedbackGroupOrRollbackOnFailure();
  }

  private void initiateRollback(Session session, Consultant initialConsultant,
      SessionStatus initialStatus, List<GroupMemberDTO> initialMembers,
      List<GroupMemberDTO> initialFeedbackGroupMembers) {
    this.rocketChatRollbackService.rollbackRemoveUsersFromRocketChatGroup(session.getGroupId(),
        initialMembers);
    this.rocketChatRollbackService
        .rollbackRemoveUsersFromRocketChatGroup(session.getFeedbackGroupId(),
            initialFeedbackGroupMembers);
    this.sessionService
        .updateConsultantAndStatusForSession(session, initialConsultant, initialStatus);
  }

  private void sendEmailForConsultantChange(Session session, Consultant consultant) {
    if (!authenticatedUser.getUserId().equals(consultant.getId())) {
      emailNotificationFacade.sendAssignEnquiryEmailNotification(consultant,
          authenticatedUser.getUserId(), session.getUser().getUsername());
    }
  }

}
