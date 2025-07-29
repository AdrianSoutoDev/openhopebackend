package es.udc.OpenHope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
public class Donation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Campaign campaign;

  @ManyToOne
  private BankAccount bankAccount;

  @Column(nullable = false)
  private Float amount;

  @Column(nullable = false)
  private Timestamp date;

  @Column
  private String PaymentId;

  @Column
  private boolean confirmed;

  public Donation(Campaign campaign, BankAccount bankAccount, Float amount, Timestamp date) {
    this.campaign = campaign;
    this.bankAccount = bankAccount;
    this.amount = amount;
    this.date = date;
    this.setConfirmed(false);
  }
}
