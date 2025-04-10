package es.udc.OpenHope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Entity
@NoArgsConstructor
public class Campaign {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Date startAt;

  private Date dateLimit;
  private Long economicTarget;
  private String image;
  private Float minimumDonation;

  @ManyToOne
  private Organization organization;

  public boolean isOnGoing() {
    //TODO si la fecha actual está entre startAt y dateLimit, si hay dateLimit, o si el ammountCollected() supera o iguala
    // el economicTarget, si hay economicTarget;
    return true;
  }

  public Float ammountCollected() {
    //TODO suma del importe de las donacionaciones
    return 0f;
  }

  public Float percentageCollected() {
    //TODO si tiene economicTarget, porcentaje entre el economicTarget y ammountCollected();
    return 0f;
  }

}
