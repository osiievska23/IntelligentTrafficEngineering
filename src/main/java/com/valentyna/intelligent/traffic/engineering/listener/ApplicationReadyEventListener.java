package com.valentyna.intelligent.traffic.engineering.listener;

import com.valentyna.intelligent.traffic.engineering.graph.Vertex;
import com.valentyna.intelligent.traffic.engineering.service.TrafficEngineeringService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    private final TrafficEngineeringService trafficEngineeringService;

    public ApplicationReadyEventListener(TrafficEngineeringService trafficEngineeringService) {
        this.trafficEngineeringService = trafficEngineeringService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        trafficEngineeringService.getPathFromSourceToDestination(new Vertex("v1"), new Vertex("v16"));
        trafficEngineeringService.getPathFromSourceToDestination(new Vertex("v7"), new Vertex("v15"));

//        System.out.println(routingAnalysisService.getPathFromSourceToDestination(new Vertex("v16"), new Vertex("v16")));

//        plotGraph.setGraphData(graph.getAdjacencyVertices().entrySet().stream()
//                .map(e -> e.getKey().getLabel())
//                .collect(Collectors.toList()));
//        plotGraph.setPathFromSourceToDestination(graphService.getPathFromSourceToDestination());
//        plotGraph.visualizeDirectedGraph();
    }
}
