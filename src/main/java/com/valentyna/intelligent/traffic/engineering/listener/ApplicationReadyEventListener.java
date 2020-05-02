package com.valentyna.intelligent.traffic.engineering.listener;

import com.valentyna.intelligent.traffic.engineering.graph.Vertex;
import com.valentyna.intelligent.traffic.engineering.panel.GraphData;
import com.valentyna.intelligent.traffic.engineering.panel.PlotGraph;
import com.valentyna.intelligent.traffic.engineering.service.GraphBuilderService;
import com.valentyna.intelligent.traffic.engineering.service.TrafficEngineeringService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    private final TrafficEngineeringService trafficEngineeringService;
    private final PlotGraph plotGraph;
    private final GraphData graphData;

    public ApplicationReadyEventListener(TrafficEngineeringService trafficEngineeringService, GraphBuilderService graphBuilderService) {
        this.trafficEngineeringService = trafficEngineeringService;
        this.plotGraph = new PlotGraph();
        this.graphData = new GraphData();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        String path1 = trafficEngineeringService.getPathFromSourceToDestination(new Vertex("v1"), new Vertex("v9"));
        String path2 = trafficEngineeringService.getPathFromSourceToDestination(new Vertex("v4"), new Vertex("v8"));

        plotGraph(Arrays.asList(path1, path2));
    }

    private void plotGraph(List<String> pathList) {
        plotGraph.setGraphData(graphData.getVerticesAmount(), graphData.getGraphData());
        pathList.forEach(plotGraph::setPathFromSourceToDestination);
        plotGraph.visualizeDirectedGraph();
    }
}
