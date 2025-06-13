package es.udc.OpenHope.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends Account {
  public User(String email, String encryptedPassword) {
    super(email, encryptedPassword);
  }
}
