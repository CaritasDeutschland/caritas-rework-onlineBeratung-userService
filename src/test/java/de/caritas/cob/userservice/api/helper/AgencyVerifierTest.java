package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import org.jeasy.random.EasyRandom;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class AgencyVerifierTest {

  @InjectMocks
  private AgencyVerifier agencyVerifier;

  @Mock
  private AgencyService agencyService;

  private EasyRandom easyRandom = new EasyRandom();

  @Test
  public void getVerifiedAgency_Should_ThrowInternalServerErrorException_When_AgencyServiceHelperFails() {
    when(agencyService.getAgencyWithoutCaching(AGENCY_ID))
        .thenThrow(new InternalServerErrorException(""));

    try {
      agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT);
      fail("Expected exception: InternalServerErrorException");
    } catch (InternalServerErrorException serviceException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void getVerifiedAgency_Should_ThrowBadRequestException_When_AgencyIsNotAssignedToGivenConsultingType() {
    when(agencyService.getAgencyWithoutCaching(AGENCY_ID)).thenReturn(AGENCY_DTO_SUCHT);

    try {
      agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_KREUZBUND);
      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  @Test
  public void getVerifiedAgency_Should_ReturnCorrectAgency_When_AgencyIsFoundAndValid() {
    when(agencyService.getAgencyWithoutCaching(AGENCY_ID)).thenReturn(AGENCY_DTO_SUCHT);

    AgencyDTO agency = agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT);

    assertEquals(AGENCY_ID, agency.getId());
  }

  @Test(expected = BadRequestException.class)
  public void checkIfConsultingTypeMatchesToAgency_Should_ThrowBadRequestException_When_ConsultingTypeDoesNotMatchToAgency() {
    when(agencyService.getAgencyWithoutCaching(any())).thenReturn(AGENCY_DTO_SUCHT);

    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setConsultingType(String.valueOf(CONSULTING_TYPE_KREUZBUND.getValue()));
    agencyVerifier.checkIfConsultingTypeMatchesToAgency(userDTO);
  }

  @Test
  public void checkIfConsultingTypeMatchesToAgency_ShouldNot_ThrowException_When_ConsultingTypeMatchesToAgency() {
    when(agencyService.getAgencyWithoutCaching(any())).thenReturn(AGENCY_DTO_SUCHT);

    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setConsultingType(String.valueOf(CONSULTING_TYPE_SUCHT.getValue()));
    agencyVerifier.checkIfConsultingTypeMatchesToAgency(userDTO);
  }

  @Test
  public void doesConsultingTypeMatchToAgency_Should_ReturnTrue_When_AgencyIsAssignedToGivenConsultingType() {
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_SUCHT);

    boolean response =
        agencyVerifier.doesConsultingTypeMatchToAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT);

    assertTrue(response);
  }

  @Test
  public void doesConsultingTypeMatchToAgency_Should_ReturnFalse_When_AgencyIsNotAssignedToGivenConsultingType() {
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT)).thenReturn(null);

    boolean response =
        agencyVerifier.doesConsultingTypeMatchToAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT);

    assertFalse(response);
  }
}
