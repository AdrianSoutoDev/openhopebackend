package es.udc.OpenHope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Consent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String consentId;

  @Column(nullable = false)
  private String aspsp;

  @Column(nullable = false)
  private String provider;

  @ManyToOne
  private Account account;
}
