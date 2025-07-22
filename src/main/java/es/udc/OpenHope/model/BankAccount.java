package es.udc.OpenHope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class BankAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String resourceId;

  @Column(nullable = false)
  private String iban;

  private String name;

  @Column(nullable = false)
  private String ownerName;

  @ManyToOne
  private Aspsp aspsp;

  @ManyToOne
  @JoinColumn(name = "account_id")
  private Account account;
}
