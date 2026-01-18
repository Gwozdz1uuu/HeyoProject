package org.gwozdz1uu.heyobackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String location;

    private String website;

    @Column(name = "phone_number")
    private String phoneNumber;
}
