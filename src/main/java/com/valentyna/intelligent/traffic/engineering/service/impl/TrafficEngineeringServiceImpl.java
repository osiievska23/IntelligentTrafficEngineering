package com.valentyna.intelligent.traffic.engineering.service.impl;

import com.valentyna.intelligent.traffic.engineering.domen.RoutingPathData;
import com.valentyna.intelligent.traffic.engineering.graph.Graph;
import com.valentyna.intelligent.traffic.engineering.graph.Vertex;
import com.valentyna.intelligent.traffic.engineering.repository.RoutingPathDataRepository;
import com.valentyna.intelligent.traffic.engineering.service.GraphBuilderService;
import com.valentyna.intelligent.traffic.engineering.service.LoadingAnalysisService;
import com.valentyna.intelligent.traffic.engineering.service.RoutingAnalysisService;
import com.valentyna.intelligent.traffic.engineering.service.TrafficEngineeringService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.IntStream;

@Service
public class TrafficEngineeringServiceImpl implements TrafficEngineeringService {

    private final LoadingAnalysisService loadingAnalysisService;

    private final RoutingAnalysisService routingAnalysisService;

    private final RoutingPathDataRepository routingPathDataRepository;

    private final Graph graph;

    private int totalOperationsAmount = 0;

    public TrafficEngineeringServiceImpl(LoadingAnalysisService loadingAnalysisService,
                                         RoutingAnalysisService routingAnalysisService,
                                         RoutingPathDataRepository routingPathDataRepository,
                                         GraphBuilderService graphBuilderService) {
        this.loadingAnalysisService = loadingAnalysisService;
        this.routingAnalysisService = routingAnalysisService;
        this.routingPathDataRepository = routingPathDataRepository;
        this.graph = graphBuilderService.buildGraph();
    }

    @Override
    public String startTrafficEngineering (Vertex source, Vertex destination) {
        long startTime = System.nanoTime();
        String path = findPathInController(source, destination);

        RoutingPathData routingPathData = routingPathDataRepository.findByPath(path).orElse(null);

        if (routingPathData == null) {
            totalOperationsAmount++;
            routingPathData = createNewRoutingData(path, source, destination);
            routingPathData = routingPathDataRepository.save(routingPathData);
        }

        loadingAnalysisService.performLoadingDataAnalysis();
        routingPathData.setLoading(loadingAnalysisService.calculateLoadCriterion(path));
        routingPathDataRepository.save(routingPathData);
        totalOperationsAmount += loadingAnalysisService.getTotalOperationAmount();

//        String result = "[" + path + "]" + " \n1) Time complexity = " + totalOperationsAmount + Math.log(totalOperationsAmount)
//                + "\n2) Performance time: " + (System.nanoTime() - startTime);
        totalOperationsAmount = 0;
        return path;
    }

    public String findPathInController(Vertex source, Vertex destination) {
        Set<RoutingPathData> routingControllerData = routingPathDataRepository.findAllByControllerId(getControllerId(source));

        // source controller to destination exist, return shortest path
        RoutingPathData routingPathData = routingControllerData.stream()
                .filter(d -> d.getDestination().equals(destination.getLabel()))
                .min(Comparator.comparing(RoutingPathData::getWeight))
                .orElse(null);

        if (routingPathData != null) {
            totalOperationsAmount++;
            return routingPathData.getPath();
        }

        // if controller exist find destination in controller paths
        String path = routingControllerData.stream()
                .map(RoutingPathData::getPath)
                .filter(p -> p.contains(destination.getLabel()))
                .map(p -> p.substring(0, p.indexOf(destination.getLabel())) + destination.getLabel())
                .min(Comparator.comparing(this::getPathWeight))
                .orElse(null);

        if (path != null) {
            totalOperationsAmount++;
            return path;
        }

        // if source does not exist, perform new analysis and try again
        routingAnalysisService.performRoutingDataAnalysis(source, destination);
        totalOperationsAmount += routingAnalysisService.getTotalOperationAmount();
        return findPathInController(source, destination);
    }

    private RoutingPathData createNewRoutingData(String path, Vertex source, Vertex destination) {
        String[] vertices = path.split(" -> ");
        return RoutingPathData.builder()
                .controllerId(Integer.parseInt(vertices[0].substring(1)))
                .adjacentTop(vertices[1])
                .source(source.getLabel())
                .destination(destination.getLabel())
                .path(path)
                .weight(getPathWeight(path))
                .build();
    }

    private int getControllerId(Vertex vertex) {
        return Integer.parseInt(vertex.getLabel().substring(1));
    }

    private double getPathWeight(String path) {
        String[] vertices = path.split(" -> ");
        return IntStream.range(0, vertices.length - 1)
                .mapToDouble(i -> graph.getEdgesWeights().get(vertices[i] + "-" + vertices[i + 1]))
                .sum();
    }
}
