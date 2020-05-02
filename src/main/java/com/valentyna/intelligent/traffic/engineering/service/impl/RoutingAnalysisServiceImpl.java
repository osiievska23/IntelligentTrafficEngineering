package com.valentyna.intelligent.traffic.engineering.service.impl;

import com.valentyna.intelligent.traffic.engineering.domen.RoutingPathData;
import com.valentyna.intelligent.traffic.engineering.graph.AdjacencyVertices;
import com.valentyna.intelligent.traffic.engineering.graph.Graph;
import com.valentyna.intelligent.traffic.engineering.graph.Vertex;
import com.valentyna.intelligent.traffic.engineering.panel.GraphData;
import com.valentyna.intelligent.traffic.engineering.repository.RoutingPathDataRepository;
import com.valentyna.intelligent.traffic.engineering.service.GraphBuilderService;
import com.valentyna.intelligent.traffic.engineering.service.RoutingAnalysisService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class RoutingAnalysisServiceImpl implements RoutingAnalysisService {

    private Graph graph;
    private Vertex source;
    private Vertex destination;
    private List<Vertex> queue = new ArrayList<>();
    private GraphData graphData = new GraphData();

    private final RoutingPathDataRepository routingDataRepository;

    public RoutingAnalysisServiceImpl(RoutingPathDataRepository routingDataRepository,
                                      GraphBuilderService graphBuilderService) {
        this.routingDataRepository = routingDataRepository;
        this.graph = graphBuilderService.buildGraph(graphData.getVerticesAmount(), graphData.getGraphData());
    }

    public void init(Vertex source, Vertex destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public void performRoutingDataAnalysis(Vertex source, Vertex destination) {
        init(source, destination);

        AdjacencyVertices firstVertices = getFirstAdjacencyVertices(); // step 2 [create first vertices layer]
        AdjacencyVertices nextVertices = getNextAdjacencyVertices(firstVertices);

        nextVertices.getCurrentVertices().forEach(v -> routingDataRepository.save(createRoutingPathData(v, this.destination))); // step 3 [fill first tables with data]

        for (Vertex currentVertex : nextVertices.getCurrentVertices()) { // step 4 [exchange of routing information between switches of the same level]
            routingDataRepository.saveAll(gatherRoutingPathInformation(nextVertices, currentVertex));
        }

        while (sourceNotReached(nextVertices)) {
            nextVertices = getNextAdjacencyVertices(nextVertices);

            for (Vertex currentVertex : nextVertices.getCurrentVertices()) {
                gatherRoutingPathInformation(nextVertices, currentVertex).forEach(routingDataRepository::save);

            }

            while (!queue.isEmpty()) {
                Vertex currentVertex = queue.get(0);
                gatherRoutingPathInformation(nextVertices, currentVertex).forEach(routingDataRepository::save);
                queue.remove(currentVertex);
            }
        }
    }

    private List<RoutingPathData> gatherRoutingPathInformation(AdjacencyVertices adjacencyVertices, Vertex currentVertex) {
        List<RoutingPathData> pathInfoList = new ArrayList<>();

        Set<Vertex> possibleAdjVertex = Stream.of(adjacencyVertices.getCurrentVertices(), adjacencyVertices.getPreviousVertices())
                .flatMap(Collection::stream)
                .filter(v -> !isCurrentVertex(currentVertex, v))
                .filter(v -> !isDestinationVertex(v))
                .filter(v -> isAdjacencyVertex(currentVertex, v))
                .filter(v -> !isVisitedAdjacencyVertex(currentVertex, v))
                .collect(Collectors.toSet());

        possibleAdjVertex.stream()
                .filter(v -> tableIsPresent(getControllerId(v)))
                .forEach(v -> pathInfoList.add(createRoutingPathData(currentVertex, v)));

        possibleAdjVertex.stream()
                .filter(v -> !tableIsPresent(getControllerId(v)))
                .forEach(v -> this.queue.add(currentVertex));

        return pathInfoList;
    }

    private RoutingPathData createRoutingPathData(Vertex currentVertex, Vertex adjVertex) {
        String path = getShortestPathFromVertexToDestination(currentVertex, adjVertex);
        return RoutingPathData.builder()
                .controllerId(getControllerId(currentVertex))
                .adjacentTop(adjVertex.getLabel())
                .weight(getPathWeight(path))
                .path(path)
                .source(currentVertex.getLabel())
                .destination(destination.getLabel())
                .build();
    }

    private double getPathWeight(String path) {
        String[] vertices = path.split(" -> ");
        return IntStream.range(0, vertices.length - 1)
                .mapToDouble(i -> this.graph.getEdgesWeights().get(vertices[i] + "-" + vertices[i + 1]))
                .sum();
    }

    private String getShortestPathFromVertexToDestination(Vertex currentVertex, Vertex adjVertex) {
        if (isDestinationVertex(adjVertex)) {
            return currentVertex.getLabel() + " -> " + adjVertex.getLabel();
        }

        List<String> result = new ArrayList<>();
        result.add(currentVertex.getLabel());

        Set<RoutingPathData> routingPathDataList = routingDataRepository.findAllByControllerId(getControllerId(adjVertex));
        return currentVertex.getLabel() + " -> " + getShortestPathInRoutingTable(routingPathDataList);
    }

    private String getShortestPathInRoutingTable(Set<RoutingPathData> routingPathDataList) {
        return routingPathDataList.stream()
                .min(Comparator.comparing(RoutingPathData::getWeight))
                .get()
                .getPath();
    }

    private boolean tableIsPresent(int adjControllerId) {
        return !routingDataRepository.findAllByControllerId(adjControllerId).isEmpty();
    }

    private AdjacencyVertices getFirstAdjacencyVertices() {
        return new AdjacencyVertices(1, Set.of(this.destination), new HashSet<>());
    }

    private AdjacencyVertices getNextAdjacencyVertices(AdjacencyVertices vertices) {
        Set<Vertex> nextVertices = vertices.getCurrentVertices().stream()
                .map(v -> this.graph.getAdjacencyVertices().get(v))
                .flatMap(Collection::stream)
                .filter(v -> !vertices.getCurrentVertices().contains(v))
                .filter(v -> !vertices.getPreviousVertices().contains(v))
                .collect(Collectors.toSet());

        return new AdjacencyVertices(vertices.getId() + 1, nextVertices, vertices.getCurrentVertices());
    }

    private boolean isVisitedAdjacencyVertex(Vertex currentVertex, Vertex adjVertex) {
        Set<RoutingPathData> routingPathDataList = routingDataRepository.findAllByControllerId(getControllerId(currentVertex));

        return routingPathDataList.stream().anyMatch(i -> i.getAdjacentTop().equals(adjVertex.getLabel()));
    }

    private boolean isAdjacencyVertex(Vertex currentVertex, Vertex vertex) {
        return this.graph.getAdjacencyVertices().get(currentVertex).contains(vertex);
    }

    private boolean isDestinationVertex(Vertex adjVertex) {
        return adjVertex.equals(this.destination);
    }

    private boolean isCurrentVertex(Vertex currentVertex, Vertex adjVertex) {
        return adjVertex.equals(currentVertex);
    }

    private boolean sourceNotReached(AdjacencyVertices nextVertices) {
        return !nextVertices.getPreviousVertices().contains(source);
    }

    private int getControllerId(Vertex vertex) {
        return Integer.parseInt(vertex.getLabel().substring(1));
    }

    @Override
    public String getPathFromSourceToDestination(Vertex source, Vertex destination) {
        Set<RoutingPathData> routingPathDataList = routingDataRepository.findAllByControllerId(getControllerId(source));
        String shortestPath = getShortestPathInRoutingTable(routingPathDataList);
        return String.join(", ", shortestPath);
    }
}
