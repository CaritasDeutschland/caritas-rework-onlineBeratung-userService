package de.caritas.cob.userservice.api.model.rocketChat.user;

import de.caritas.cob.userservice.api.model.rocketChat.RocketChatUserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rocket.Chat users.info DTO
 *
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponseDTO {

  private RocketChatUserDTO user;
  private boolean success;
  private String error;
  private String errorType;

}
