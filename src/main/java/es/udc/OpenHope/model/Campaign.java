package es.udc.OpenHope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.Set;

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

  @Lob
  private String description;

  @ManyToOne
  private Organization organization;

  @ManyToMany
  Set<Category> categories;

  private Date finalizedDate;

  @OneToOne
  private BankAccount bankAccount;

  @OneToMany(mappedBy = "campaign")
  private Set<Topic> topics;

  public Campaign(String name, Date startAt, Date dateLimit, Long economicTarget, Float minimumDonation, String image,
                  Organization organization, String description, Set<Category> categories) {
    this.name = name;
    this.startAt = startAt;
    this.dateLimit = dateLimit;
    this.economicTarget = economicTarget;
    this.minimumDonation = minimumDonation;
    this.image = image;
    this.organization = organization;
    this.description = description;
    this.categories = categories;
  }

  public Campaign(String name, Date startAt, Date dateLimit, Long economicTarget, Float minimumDonation, String image,
                  Organization organization, String description, Set<Category> categories, Date finalizedDate) {
    this.name = name;
    this.startAt = startAt;
    this.dateLimit = dateLimit;
    this.economicTarget = economicTarget;
    this.minimumDonation = minimumDonation;
    this.image = image;
    this.organization = organization;
    this.description = description;
    this.categories = categories;
    this.finalizedDate = finalizedDate;
  }
}
