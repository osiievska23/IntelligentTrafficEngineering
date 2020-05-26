package com.valentyna.intelligent.traffic.engineering.service.impl;

import com.valentyna.intelligent.traffic.engineering.graph.Graph;
import com.valentyna.intelligent.traffic.engineering.panel.GraphData;
import com.valentyna.intelligent.traffic.engineering.service.GraphBuilderService;
import org.springframework.stereotype.Service;

@Service
public class GraphBuilderServiceImpl implements GraphBuilderService {

    @Override
    public Graph buildGraph() {
        GraphData graphData = new GraphData();
        Graph graph = new Graph();

        for (int i = 1; i <= graphData.getVerticesAmount(); i++) {
            graph.addVertex("v" + i);
        }

        for (String[] data : graphData.getGraphData()) {
            graph.addEdge(data[0], data[1], Double.parseDouble(data[2]));
        }

        return graph;
    }
}
