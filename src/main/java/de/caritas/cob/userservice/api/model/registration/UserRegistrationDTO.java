package de.caritas.cob.userservice.api.model.registration;

public interface UserRegistrationDTO {

  Long getAgencyId();

  String getPostcode();

  String getConsultingType();

  boolean isNewUserAccount();

  String getConsultantId();
}
