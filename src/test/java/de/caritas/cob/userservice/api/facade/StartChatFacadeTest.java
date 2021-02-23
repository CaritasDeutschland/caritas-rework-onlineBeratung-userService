package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.ACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.INACTIVE_CHAT;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.ChatHelper;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;

@RunWith(MockitoJUnitRunner.class)
public class StartChatFacadeTest {

  @InjectMocks
  private StartChatFacade startChatFacade;
  @Mock
  private ChatHelper chatHelper;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private ChatService chatService;
  @Mock
  private Chat chat;

  /**
   * Method: startChat
   */
  @Test
  public void startChat_Should_ThrowRequestForbiddenException_WhenConsultantHasNoPermissionForChat() {

    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT))
        .thenReturn(false);

    try {
      startChatFacade.startChat(ACTIVE_CHAT, CONSULTANT);
      fail("Expected exception: RequestForbiddenException");
    } catch (ForbiddenException sequestForbiddenException) {
      assertTrue("Excepted RequestForbiddenException thrown", true);
    }

  }

  @Test
  public void startChat_Should_ThrowConflictException_WhenChatIsAlreadyStarted() {

    when(chatHelper.isChatAgenciesContainConsultantAgency(ACTIVE_CHAT, CONSULTANT))
        .thenReturn(true);

    try {
      startChatFacade.startChat(ACTIVE_CHAT, CONSULTANT);
      fail("Expected exception: ConflictException");
    } catch (ConflictException conflictException) {
      assertTrue("Excepted ConflictException thrown", true);
    }

  }

  @Test
  public void startChat_Should_ThrowInternalServerError_WhenChatHasNoGroupId() {

    when(chat.isActive()).thenReturn(false);
    when(chat.getGroupId()).thenReturn(null);

    when(chatHelper.isChatAgenciesContainConsultantAgency(chat, CONSULTANT)).thenReturn(true);

    try {
      startChatFacade.startChat(chat, CONSULTANT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException internalServerErrorException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }

  }

  @Test
  public void startChat_Should_AddConsultantToRocketChatGroup()
      throws RocketChatAddUserToGroupException {

    when(chatHelper.isChatAgenciesContainConsultantAgency(INACTIVE_CHAT, CONSULTANT))
        .thenReturn(true);

    startChatFacade.startChat(INACTIVE_CHAT, CONSULTANT);

    verify(rocketChatService, times(1)).addUserToGroup(CONSULTANT.getRocketChatId(),
        INACTIVE_CHAT.getGroupId());

  }

  @Test
  public void startChat_Should_SetChatActiveAndSaveChat() throws SaveChatException {

    when(chat.getGroupId()).thenReturn(RC_GROUP_ID);

    when(chatHelper.isChatAgenciesContainConsultantAgency(chat, CONSULTANT)).thenReturn(true);

    startChatFacade.startChat(chat, CONSULTANT);

    verify(chat, times(1)).setActive(true);
    verify(chatService, times(1)).saveChat(chat);

  }

}
