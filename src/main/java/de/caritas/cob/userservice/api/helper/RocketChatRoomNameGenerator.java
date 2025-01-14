package de.caritas.cob.userservice.api.helper;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Session;

public class RocketChatRoomNameGenerator {

  private static final String GROUP_CHAT_SUFFIX = "group_chat";

  /**
   * Generates a unique name for the private Rocket.Chat group consisting of the session id and the
   * current time stamp.
   *
   * @param session the session
   * @return the group name
   */
  public String generateGroupName(Session session) {
    return generateName(session.getId(), null);
  }

  /**
   * Generates a unique name for the private Rocket.Chat group consisting of the chat id and the
   * current time stamp.
   *
   * @param chat the chat
   * @return the group name
   */
  public String generateGroupChatName(Chat chat) {
    return generateName(chat.getId(), GROUP_CHAT_SUFFIX);
  }

  /**
   * Generates a unique name for a private Rocket.Chat group with sessionId and suffix
   *
   * @param sessionId the session id
   * @param suffix the suffix
   * @return the group name
   */
  private String generateName(Long sessionId, String suffix) {
    return sessionId + (nonNull(suffix) ? "_" + suffix + "_" : "_") + System.currentTimeMillis();
  }
}
