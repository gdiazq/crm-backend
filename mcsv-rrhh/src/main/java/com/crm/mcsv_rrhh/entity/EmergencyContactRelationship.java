package com.crm.mcsv_rrhh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emergency_contact_relationships")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmergencyContactRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
