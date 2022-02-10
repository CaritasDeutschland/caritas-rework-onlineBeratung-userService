package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.port.in.IdentityManaging;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityManager implements IdentityManaging {

  private final IdentityClient keycloakService;

  @Override
  public Optional<String> setUpOneTimePassword(String username, String email) {
    return keycloakService.initiateEmailVerification(username, email);
  }

  @Override
  public void setUpOneTimePassword(String username, String initialCode, String secret) {
    keycloakService.setUpOtpCredential(username, initialCode, secret);
  }

  @Override
  public void deleteOneTimePassword(String username) {
    keycloakService.deleteOtpCredential(username);
  }
}
