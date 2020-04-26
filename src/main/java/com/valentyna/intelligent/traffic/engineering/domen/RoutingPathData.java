package com.valentyna.intelligent.traffic.engineering.domen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "routing_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingPathData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "routing_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "controllerId")
    private int controllerId;

    @Column(name = "adjacent_vertex")
    private String adjacentTop;

    @Column(name = "path_weight")
    private double weight;

    @Column(name = "path_loading")
    private double loading;

    @Column(name = "source")
    private String source;

    @Column(name = "destination")
    private String destination;

    @Column(name = "path")
    private String path;
}
