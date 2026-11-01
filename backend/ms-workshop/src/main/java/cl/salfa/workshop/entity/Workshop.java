package cl.salfa.workshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workshops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String city;

    @Column(length = 150)
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}