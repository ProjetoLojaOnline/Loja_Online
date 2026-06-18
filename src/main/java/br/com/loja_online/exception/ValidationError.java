package br.com.loja_online.exception;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationError extends StandardError {
  private List<FieldMessage> errors = new ArrayList<>();

  public ValidationError(){
    super();
  }

  public ValidationError(Long timestamp, Integer status, String error, String message, String path) {
    super(timestamp, status, error, message, path);
  }

  public void addError(String fieldName, String message) {
    this.errors.add(new FieldMessage(fieldName, message));
  }
}
