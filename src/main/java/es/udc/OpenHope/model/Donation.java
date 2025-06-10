package es.udc.OpenHope.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Donation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  Campaign campaign;
}
