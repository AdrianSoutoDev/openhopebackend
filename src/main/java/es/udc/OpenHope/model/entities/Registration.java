package es.udc.OpenHope.model.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Registration {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String encryptedPassword;

  public Registration(){}

  public Registration(String email, String encryptedPassword){
    this.email = email;
    this.encryptedPassword = encryptedPassword;
  }

}
