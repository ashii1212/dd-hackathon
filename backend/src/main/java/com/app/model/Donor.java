package com.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "donors")
public class Donor {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String zone;

    protected Donor() {
    }

    public Donor(String id, String name, String zone) {
        this.id = id;
        this.name = name;
        this.zone = zone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getZone() { return zone; }
}
