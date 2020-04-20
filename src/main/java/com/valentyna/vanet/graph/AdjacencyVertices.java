package com.valentyna.vanet.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class AdjacencyVertices {

    private int id;
    private Set<Vertex> currentVertices;
    private Set<Vertex> previousVertices;
}
