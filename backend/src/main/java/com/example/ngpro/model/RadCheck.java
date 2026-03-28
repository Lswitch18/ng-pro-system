package com.example.ngpro.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "radcheck")
public class RadCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "attribute")
    private String attribute;

    @Column(name = "op")
    private String op;

    @Column(name = "value")
    private String value;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }
    public String getOp() { return op; }
    public void setOp(String op) { this.op = op; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
