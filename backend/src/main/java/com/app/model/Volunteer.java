package com.app.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "volunteers")
public class Volunteer {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "capacity_kg", nullable = false)
    private int capacityKg;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "volunteer_zones", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Column(name = "zone", nullable = false, length = 50)
    private Set<String> zones = new HashSet<>();

    protected Volunteer() {
    }

    public Volunteer(String id, String name, int capacityKg, Set<String> zones) {
        this.id = id;
        this.name = name;
        this.capacityKg = capacityKg;
        this.zones = new HashSet<>(zones);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCapacityKg() { return capacityKg; }
    public Set<String> getZones() { return zones; }
}
