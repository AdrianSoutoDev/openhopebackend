package es.udc.OpenHope.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@NoArgsConstructor
public class Organization extends Account {

  @Column(unique = true, nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String image;

  @ManyToMany
  Set<Category> categories;

  @OneToMany(mappedBy = "organization")
  private Set<Topic> topics;

  public Organization(String email, String encryptedPassword, String name) {
    super(email, encryptedPassword);
    this.name = name;
  }

  public Organization(String email, String encryptedPassword, String name, String description, String image, Set<Category> categories) {
    super(email, encryptedPassword);
    this.name = name;
    this.description = description;
    this.image = image;
    this.categories = categories;
  }

  public Organization(String email, String encryptedPassword, String name, String description, String image) {
    this(email, encryptedPassword, name, description, image, new HashSet<Category>());
  }
}
