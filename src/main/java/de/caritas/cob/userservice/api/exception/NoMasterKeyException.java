package de.caritas.cob.userservice.api.exception;

public class NoMasterKeyException extends RuntimeException {

  private static final long serialVersionUID = 362702101121444833L;

  /**
   * Exception, when no master key is set
   *
   * @param message
   */
  public NoMasterKeyException(String message) {
    super(message);
  }
}
