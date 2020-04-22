package com.valentyna.vanet;

import com.valentyna.vanet.graph.Graph;
import com.valentyna.vanet.graph.GraphBuilder;
import com.valentyna.vanet.graph.Vertex;
import com.valentyna.vanet.panel.PlotGraph;
import com.valentyna.vanet.service.GraphService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartAppListener implements ApplicationListener<ApplicationReadyEvent> {

    private final GraphService graphService;
    private final PlotGraph plotGraph;

    public StartAppListener(GraphService graphService) {
        this.graphService = graphService;
        this.plotGraph = new PlotGraph();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        GraphBuilder builder = new GraphBuilder();

        Graph graph = builder.buildFirst();
        Vertex source = new Vertex("v1");
        Vertex destination = new Vertex("v16");

        graphService.init(graph, source, destination);
        graphService.generateRoutingInformationInNetworkSwitches();

//        plotGraph.setGraphData(graph.getAdjacencyVertices().entrySet().stream()
//                .map(e -> e.getKey().getLabel())
//                .collect(Collectors.toList()));
//        plotGraph.setPathFromSourceToDestination(graphService.getPathFromSourceToDestination());
//        plotGraph.visualizeDirectedGraph();

//        Graph graph2 = builder.buildSecond();
//        Vertex source2 = new Vertex("v5");
//        Vertex destination2 = new Vertex("v0");

//        GraphService graphService2 = new GraphService(graph2, source2, destination2);
//        graphService2.generateRoutingInformationInNetworkSwitches();
    }
}
