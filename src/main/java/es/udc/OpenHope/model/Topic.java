package es.udc.OpenHope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "organization_id"}),
        @UniqueConstraint(columnNames = {"name", "campaign_id"})
    }
)
public class Topic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToOne
  Organization organization;

  @ManyToOne
  Campaign campaign;

  public Topic(String name, Organization organization) {
    this.name = name;
    this.organization = organization;
  }

  public Topic(String name, Campaign campaign) {
    this.campaign = campaign;
    this.name = name;
  }
}