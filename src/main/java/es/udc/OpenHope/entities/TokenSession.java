package es.udc.OpenHope.entities;

import jakarta.persistence.*;

@Entity
public class TokenSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String token;

  private Boolean isValid;

  @ManyToOne
  @JoinColumn(name = "registration_id", nullable = false)
  private Registration registration;

}
