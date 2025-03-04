package es.udc.OpenHope.entities;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Registration {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String encryptedPassword;

  @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TokenSession> tokenSessions = new ArrayList<>();

}
