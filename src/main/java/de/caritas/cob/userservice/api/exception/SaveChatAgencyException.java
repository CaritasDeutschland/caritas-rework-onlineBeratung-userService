package de.caritas.cob.userservice.api.exception;

public class SaveChatAgencyException extends Exception {

  private static final long serialVersionUID = 5563690206628141695L;

  /**
   * Exception when saving the chat agency to database fails
   * 
   * @param ex
   */
  public SaveChatAgencyException(String message, Exception ex) {
    super(message, ex);
  }

}
