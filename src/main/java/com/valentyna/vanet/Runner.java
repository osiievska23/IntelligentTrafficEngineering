package com.valentyna.vanet;

import com.valentyna.vanet.graph.Graph;
import com.valentyna.vanet.graph.GraphBuilder;
import com.valentyna.vanet.graph.Vertex;
import com.valentyna.vanet.service.GraphService;

public class Runner {

    public static void main(String[] args) {
        GraphBuilder builder = new GraphBuilder();

        Graph graph = builder.build();
        Vertex source = new Vertex("v1");
        Vertex destination = new Vertex("v16");

        GraphService graphService = new GraphService(graph, source, destination);

        graphService.generateRoutingInformationInNetworkSwitches();
    }
}
