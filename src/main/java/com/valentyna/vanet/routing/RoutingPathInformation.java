package com.valentyna.vanet.routing;

import com.valentyna.vanet.graph.Vertex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingPathInformation {

    private Vertex adjacentTop;
    private double weight;
    private double loading;
    private Vertex destination;
    private List<Vertex> path;
}
