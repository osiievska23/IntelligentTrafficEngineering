package com.valentyna.vanet.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "routing_data")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingPathData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "routing_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "controlerId")
    private int controllerId;

    @Column(name = "adjacent_vertex")
    private String adjacentTop;

    @Column(name = "path_weight")
    private double weight;

    @Column(name = "path_loading")
    private double loading;

    @Column(name = "destination")
    private String destination;

    @ElementCollection
    private List<String> path;
}
