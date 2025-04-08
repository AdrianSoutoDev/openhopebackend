package es.udc.OpenHope.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@NoArgsConstructor
public class Organization extends Account {

  @Column(unique = true, nullable = false)
  private String name;

  @Lob
  private String description;
  private String image;

  public Organization(String email, String encryptedPassword, String name) {
    super(email, encryptedPassword);
    this.name = name;
  }

  public Organization(String email, String encryptedPassword, String name, String description, String image) {
    super(email, encryptedPassword);
    this.name = name;
    this.description = description;
    this.image = image;
  }
}
