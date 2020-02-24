package com.valentyna.vanet;

import com.valentyna.vanet.graph.Graph;
import com.valentyna.vanet.graph.GraphBuilder;
import com.valentyna.vanet.graph.Vertex;
import com.valentyna.vanet.service.GraphService;

public class Runner {

    public static void main(String[] args) {
        GraphBuilder builder = new GraphBuilder();

        Graph graph = builder.buildFirst();
        Vertex source = new Vertex("v1");
        Vertex destination = new Vertex("v11");

        GraphService graphService = new GraphService(graph, source, destination);
        graphService.generateRoutingInformationInNetworkSwitches();

        Graph graph2 = builder.buildSecond();
        Vertex source2 = new Vertex("v5");
        Vertex destination2 = new Vertex("v0");

        GraphService graphService2 = new GraphService(graph2, source2, destination2);
        graphService2.generateRoutingInformationInNetworkSwitches();
    }
}
