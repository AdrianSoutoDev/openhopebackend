package es.udc.OpenHope.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Organization extends Registration {

  @Column(nullable = false)
  private String name;
  private String description;
  private String image;
}
