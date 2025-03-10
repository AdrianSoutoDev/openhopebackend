package es.udc.OpenHope.model;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class User extends Registration {
  public User(String email, String encryptedPassword) {
    super(email, encryptedPassword);
  }
}
