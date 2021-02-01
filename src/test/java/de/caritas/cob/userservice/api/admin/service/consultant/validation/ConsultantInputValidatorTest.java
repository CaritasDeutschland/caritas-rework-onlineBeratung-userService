package de.caritas.cob.userservice.api.admin.service.consultant.validation;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_AVAILABLE;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.USERNAME_NOT_AVAILABLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.CreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantInputValidatorTest {

  @InjectMocks
  private ConsultantInputValidator consultantInputValidator;

  @Mock
  private Validator validator;

  @Test
  public void validateCreateConsultantDTO_ShouldNot_throwException_When_consultantIsNotAbsent() {
    AbsenceInputValidation createConsultantDTO = new
        CreateConsultantDTOAbsenceInputAdapter(new CreateConsultantDTO()
        .absent(false));

    try {
      this.consultantInputValidator.validateAbsence(createConsultantDTO);
    } catch (CustomValidationHttpStatusException e) {
      fail("Exception should not be thrown");
    }
  }

  @Test
  public void validateCreateConsultantDTO_ShouldNot_throwException_When_consultantIsAbsentAndAbsenceMessageIsSet() {
    AbsenceInputValidation createConsultantDTO = new
        CreateConsultantDTOAbsenceInputAdapter(new CreateConsultantDTO()
        .absent(true)
        .absenceMessage("Absent"));

    try {
      this.consultantInputValidator.validateAbsence(createConsultantDTO);
    } catch (CustomValidationHttpStatusException e) {
      fail("Exception should not be thrown");
    }
  }

  @Test
  public void validateCreateConsultantDTO_Should_throwExpectedException_When_consultantIsAbsentAndAbsenceMessageIsEmpty() {
    AbsenceInputValidation createConsultantDTO = new
        CreateConsultantDTOAbsenceInputAdapter(new CreateConsultantDTO()
        .absent(true)
        .absenceMessage(null));

    try {
      this.consultantInputValidator.validateAbsence(createConsultantDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader(), notNullValue());
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0),
          is(MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER.name()));
    }
  }

  @Test
  public void validateKeycloakResponse_ShouldNot_throwException_When_keycloakResponseDTOIsValid() {
    KeycloakCreateUserResponseDTO responseDTO = new KeycloakCreateUserResponseDTO();
    responseDTO.setUserId("userId");

    try {
      this.consultantInputValidator.validateKeycloakResponse(responseDTO);
    } catch (Exception e) {
      fail("Exception should not be thrown");
    }
  }

  @Test(expected = KeycloakException.class)
  public void validateKeycloakResponse_Should_throwKeycloakException_When_userIdIsNullAndUsernameAndEmailIsValid() {
    KeycloakCreateUserResponseDTO responseDTO = new KeycloakCreateUserResponseDTO();
    CreateUserResponseDTO createUserResponseDTO = new CreateUserResponseDTO();
    createUserResponseDTO.setEmailAvailable(1);
    createUserResponseDTO.setUsernameAvailable(1);
    responseDTO.setResponseDTO(createUserResponseDTO);

    this.consultantInputValidator.validateKeycloakResponse(responseDTO);
  }

  @Test
  public void validateKeycloakResponse_Should_throwExpectedException_When_userIdIsNullAndUsernameIsInvalid() {
    KeycloakCreateUserResponseDTO responseDTO = new KeycloakCreateUserResponseDTO();
    CreateUserResponseDTO createUserResponseDTO = new CreateUserResponseDTO();
    createUserResponseDTO.setEmailAvailable(1);
    createUserResponseDTO.setUsernameAvailable(0);
    responseDTO.setResponseDTO(createUserResponseDTO);

    try {
      this.consultantInputValidator.validateKeycloakResponse(responseDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader(), notNullValue());
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0),
          is(USERNAME_NOT_AVAILABLE.name()));
    }
  }

  @Test
  public void validateKeycloakResponse_Should_throwExpectedException_When_userIdIsNullAndEmailIsInvalid() {
    KeycloakCreateUserResponseDTO responseDTO = new KeycloakCreateUserResponseDTO();
    CreateUserResponseDTO createUserResponseDTO = new CreateUserResponseDTO();
    createUserResponseDTO.setEmailAvailable(0);
    createUserResponseDTO.setUsernameAvailable(1);
    responseDTO.setResponseDTO(createUserResponseDTO);

    try {
      this.consultantInputValidator.validateKeycloakResponse(responseDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader(), notNullValue());
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0),
          is(EMAIL_NOT_AVAILABLE.name()));
    }
  }

}