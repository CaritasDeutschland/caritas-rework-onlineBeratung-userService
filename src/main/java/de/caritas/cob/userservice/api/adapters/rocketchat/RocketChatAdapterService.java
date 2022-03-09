package de.caritas.cob.userservice.api.adapters.rocketchat;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.rocketchat.config.RocketChatConfig;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.MessageResponse;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.RoomResponse;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
@RequiredArgsConstructor
public class RocketChatAdapterService implements MessageClient {

  private static final String ENDPOINT_MUTE_USER = "/method.call/muteUserInRoom";

  private static final String ENDPOINT_ROOM_INFO = "/rooms.info?roomId=";

  private final RocketChatClient rocketChatClient;

  private final RocketChatConfig rocketChatConfig;

  private final RocketChatMapper mapper;

  @Override
  public boolean muteUserInRoom(String ownerId, String username, String roomId) {
    var url = rocketChatConfig.getApiUrl(ENDPOINT_MUTE_USER);
    var muteUser = mapper.muteUserOf(username, roomId);

    try {
      var response = rocketChatClient.postForEntity(url, ownerId, muteUser, MessageResponse.class);
      return userWasInRoom(response) && response.getStatusCode().is2xxSuccessful();
    } catch (HttpClientErrorException exception) {
      log.error("Muting failed.", exception);
      return false;
    }
  }

  @Override
  public Optional<Map<String, Object>> getChatInfo(String roomId, String userId) {
    var url = rocketChatConfig.getApiUrl(ENDPOINT_ROOM_INFO + roomId);

    try {
      var response = rocketChatClient.getForEntity(url, userId, RoomResponse.class);
      return mapper.mapOf(response);
    } catch (HttpClientErrorException exception) {
      log.error("Chat Info failed.", exception);
      return Optional.empty();
    }
  }

  private boolean userWasInRoom(ResponseEntity<MessageResponse> response) {
    var body = response.getBody();
    if (nonNull(body)) {
      var message = body.getMessage();
      return isNull(message) || !message.contains("error-user-not-in-room");
    } else {
      return true;
    }
  }
}