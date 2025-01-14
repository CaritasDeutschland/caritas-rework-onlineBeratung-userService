package de.caritas.cob.userservice.api.service.sessionlist;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.session.SessionTopicEnrichmentService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service class to enrich a session of an consultant with required Rocket.Chat data. */
@Service
@RequiredArgsConstructor
public class ConsultantSessionEnricher {

  private final @NonNull SessionListAnalyser sessionListAnalyser;
  private final @NonNull RocketChatRoomInformationProvider rocketChatRoomInformationProvider;

  @Autowired(required = false)
  private SessionTopicEnrichmentService sessionTopicEnrichmentService;

  @Value("${feature.topics.enabled}")
  private boolean topicsFeatureEnabled;

  public List<ConsultantSessionResponseDTO> updateRequiredConsultantSessionValues(
      List<ConsultantSessionResponseDTO> consultantSessionResponseDTOs,
      String rcToken,
      Consultant consultant) {

    var rocketChatRoomInformation =
        this.rocketChatRoomInformationProvider.retrieveRocketChatInformation(
            RocketChatCredentials.builder()
                .rocketChatToken(rcToken)
                .rocketChatUserId(consultant.getRocketChatId())
                .build());

    consultantSessionResponseDTOs.forEach(
        consultantSessionResponseDTO ->
            this.enrichConsultantSession(
                consultantSessionResponseDTO, rocketChatRoomInformation, consultant));
    return consultantSessionResponseDTOs;
  }

  private void enrichConsultantSession(
      ConsultantSessionResponseDTO consultantSessionResponseDTO,
      RocketChatRoomInformation rocketChatRoomInformation,
      Consultant consultant) {
    var session = consultantSessionResponseDTO.getSession();
    var groupId = session.getGroupId();

    session.setMessagesRead(
        sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
            rocketChatRoomInformation.getReadMessages(), groupId));

    var messageUpdater = new AvailableLastMessageUpdater(this.sessionListAnalyser);
    messageUpdater.updateSessionWithAvailableLastMessage(
        consultantSessionResponseDTO.getSession(),
        consultantSessionResponseDTO::setLatestMessage,
        rocketChatRoomInformation,
        consultant.getRocketChatId());

    enrichSessionWithTopic(consultantSessionResponseDTO);
  }

  private void enrichSessionWithTopic(ConsultantSessionResponseDTO consultantSessionResponseDTO) {
    if (topicsFeatureEnabled) {
      sessionTopicEnrichmentService.enrichSessionWithTopicData(
          consultantSessionResponseDTO.getSession());
    }
  }
}
