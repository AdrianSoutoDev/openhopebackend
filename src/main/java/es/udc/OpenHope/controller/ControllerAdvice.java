package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.ErrorDto;
import es.udc.OpenHope.exception.*;
import es.udc.OpenHope.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorDto handleGenericException(Exception e) {
    String message = Messages.get("error.generic");
    return new ErrorDto(message);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleValidationException(MethodArgumentNotValidException e) {

    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getFieldErrors().forEach((fieldError) -> {
      String fieldName = fieldError.getField();
      String errorMessage = fieldError.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return errors;
  }

  @ExceptionHandler(DuplicateEmailException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorDto handleDuplicateEmailException(DuplicateEmailException e) {
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(DuplicateOrganizationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorDto handleDuplicateOrganizationException(DuplicateOrganizationException e) {
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorDto handleDuplicateInvalidCredentialsException(InvalidCredentialsException e) {
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(MaxCategoriesExceededException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorDto handleMaxCategoriesExceededException(MaxCategoriesExceededException e) {
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorDto handleNoSuchElementException(NoSuchElementException e) {
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(SecurityException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorDto handleSecurityException(SecurityException e) {
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(DuplicatedCampaignException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorDto handleDuplicatedCampaignException(DuplicatedCampaignException e) {
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorDto handleIllegalArgumentException(IllegalArgumentException e) {
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorDto handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    return new ErrorDto(e.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorDto handleUnauthorizedExceptionException(UnauthorizedException e) {
    System.out.println("UnauthorizedException: " + e.getMessage());
    String message = Messages.get("error.generic");
    return new ErrorDto(message);
  }

}
