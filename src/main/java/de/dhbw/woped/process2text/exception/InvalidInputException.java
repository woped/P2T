package de.dhbw.woped.process2text.exception;

/**
 * Exception thrown when input validation fails.
 *
 * <p>This exception is thrown when the provided input does not meet
 * the expected format or validation criteria. Common scenarios include:
 * <ul>
 *   <li>Invalid XML structure in process models</li>
 *   <li>Unsupported process model format</li>
 *   <li>Missing required elements in the input</li>
 *   <li>Malformed PNML or BPMN content</li>
 * </ul>
 *
 * <p>This is a checked exception, requiring callers to explicitly
 * handle or propagate it.
 *
 * @author WoPeD Team
 * @see de.dhbw.woped.process2text.controller.GlobalExceptionHandler
 */
public class InvalidInputException extends Exception {

  /**
   * Constructs a new InvalidInputException with the specified detail message.
   *
   * @param message the detail message describing the validation failure
   */
  public InvalidInputException(String message) {
    super(message);
  }
}
