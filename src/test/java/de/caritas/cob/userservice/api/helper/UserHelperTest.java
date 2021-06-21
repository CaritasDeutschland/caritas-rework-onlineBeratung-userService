package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_HOST_BASE_URL;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_VALUE_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_LINK_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.HOST_BASE_URL;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_CONSULTANT_DECODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_CONSULTANT_ENCODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_ENCODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_TOO_LONG;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_TOO_SHORT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.FieldSetter.setField;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserHelperTest {

  @InjectMocks
  private UserHelper userHelper;

  @Mock
  private ConsultingTypeManager consultingTypeManager;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    setField(userHelper,
        userHelper.getClass().getDeclaredField(FIELD_NAME_EMAIL_DUMMY_SUFFIX),
        FIELD_VALUE_EMAIL_DUMMY_SUFFIX);
    setField(userHelper,
        userHelper.getClass().getDeclaredField(FIELD_NAME_HOST_BASE_URL), HOST_BASE_URL);
  }

  @Test
  public void isUsernameValid_Should_ReturnFalse_WhenUsernameIsTooShort() {
    assertFalse(userHelper.isUsernameValid(USERNAME_TOO_SHORT));
  }

  @Test
  public void isUsernameValid_Should_ReturnFalse_WhenUsernameIsTooLong() {
    assertFalse(userHelper.isUsernameValid(USERNAME_TOO_LONG));
  }

  @Test
  public void getDummyEmail_Should_ReturnRcUserIdWithDummyEmailSuffix() {
    assertEquals(USER_ID + FIELD_VALUE_EMAIL_DUMMY_SUFFIX, userHelper.getDummyEmail(USER_ID));
  }

  @Test
  public void generateChatUrl_Should_ReturnChatLinkWithConsultingTypeUrlNameAndEncodedChatId() {
    when(consultingTypeManager.getConsultingTypeSettings(0))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);
    assertEquals(CHAT_LINK_SUCHT, userHelper.generateChatUrl(CHAT_ID, 0));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnFalse_WhenDecodedUsernamesDontMatch() {
    assertFalse(userHelper.doUsernamesMatch(USERNAME_CONSULTANT_DECODED, USERNAME_DECODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnFalse_WhenEncodedUsernamesDontMatch() {
    assertFalse(userHelper.doUsernamesMatch(USERNAME_CONSULTANT_ENCODED, USERNAME_ENCODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnFalse_WhenEncodedAndDecodedUsernamesDontMatch() {
    assertFalse(userHelper.doUsernamesMatch(USERNAME_CONSULTANT_ENCODED, USERNAME_DECODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnTrue_WhenDecodedUsernamesMatch() {
    assertTrue(userHelper.doUsernamesMatch(USERNAME_DECODED, USERNAME_DECODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnTrue_WhenEncodedUsernamesMatch() {
    assertTrue(userHelper.doUsernamesMatch(USERNAME_ENCODED, USERNAME_ENCODED));
  }

  @Test
  public void doUsernamesMatch_Should_ReturnTrue_WhenEncodedAndDecodedUsernamesMatch() {
    assertTrue(userHelper.doUsernamesMatch(USERNAME_ENCODED, USERNAME_DECODED));
  }
}
