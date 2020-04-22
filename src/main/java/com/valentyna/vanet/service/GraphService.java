package com.valentyna.vanet.service;

import com.valentyna.vanet.graph.AdjacencyVertices;
import com.valentyna.vanet.graph.Graph;
import com.valentyna.vanet.graph.Vertex;
import com.valentyna.vanet.repository.RoutingPathDataRepository;
import com.valentyna.vanet.routing.RoutingPathData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class GraphService {

    private Graph graph;
    private Vertex source;
    private Vertex destination;

    private List<Vertex> queue = new ArrayList<>();

    @Autowired
    private RoutingPathDataRepository routingPathDataRepository;

    public void init(Graph graph, Vertex source, Vertex destination) {
        this.graph = graph;
        this.source = source;
        this.destination = destination;
    }

    public void generateRoutingInformationInNetworkSwitches() {
        AdjacencyVertices firstVertices = getFirstAdjacencyVertices(); // step 2 [create first vertices layer]
        AdjacencyVertices nextVertices = getNextAdjacencyVertices(firstVertices);

        nextVertices.getCurrentVertices().forEach(v -> routingPathDataRepository.save(createRoutingPathData(v, destination))); // step 3 [fill first tables with data]

        for (Vertex currentVertex : nextVertices.getCurrentVertices()) { // step 4 [exchange of routing information between switches of the same level]
            routingPathDataRepository.saveAll(gatherRoutingPathInformation(nextVertices, currentVertex));
        }

        while (sourceNotReached(nextVertices)) {
            nextVertices = getNextAdjacencyVertices(nextVertices);

            for (Vertex currentVertex : nextVertices.getCurrentVertices()) {
                gatherRoutingPathInformation(nextVertices, currentVertex).forEach(d -> routingPathDataRepository.save(d));

            }

            while (!queue.isEmpty()) {
                Vertex currentVertex = queue.get(0);
                gatherRoutingPathInformation(nextVertices, currentVertex).forEach(d -> routingPathDataRepository.save(d));
                queue.remove(currentVertex);
            }
        }

        System.out.println(getPathFromSourceToDestination());
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
        List<String> path = getShortestPathFromVertexToDestination(currentVertex, adjVertex);
        return RoutingPathData.builder()
                .controllerId(getControllerId(currentVertex))
                .adjacentTop(adjVertex.getLabel())
                .weight(getPathWeight(path))
                .path(path)
                .destination(this.destination.getLabel())
                .build();
    }

    private double getPathWeight(List<String> path) {
        return IntStream.range(0, path.size() - 1)
                .mapToDouble(i -> this.graph.getEdgesWeights().get(path.get(i) + "-" + path.get(i + 1)))
                .sum();
    }

    private List<String> getShortestPathFromVertexToDestination(Vertex currentVertex, Vertex adjVertex) {
        if (isDestinationVertex(adjVertex)) {
            return Arrays.asList(currentVertex.getLabel(), adjVertex.getLabel());
        }

        List<String> result = new ArrayList<>();
        result.add(currentVertex.getLabel());

        Set<RoutingPathData> routingPathDataList = routingPathDataRepository.findAllByControllerIdAndFetchPath(getControllerId(adjVertex));
        result.addAll(getShortestPathInRoutingTable(routingPathDataList, currentVertex));

        return result;
    }

    private List<String> getShortestPathInRoutingTable(Set<RoutingPathData> routingPathDataList, Vertex currentVertex) {
        return routingPathDataList.stream()
                .filter(d -> !d.getAdjacentTop().equals(currentVertex.getLabel()))
                .min(Comparator.comparing(RoutingPathData::getWeight))
                .get()
                .getPath();
    }

    private boolean tableIsPresent(int adjControllerId) {
        return !routingPathDataRepository.findAllByControllerId(adjControllerId).isEmpty();
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
        Set<RoutingPathData> routingPathDataList = routingPathDataRepository.findAllByControllerId(getControllerId(currentVertex));

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
        return Integer.valueOf(vertex.getLabel().substring(1));
    }

    public String getPathFromSourceToDestination() {
        Set<RoutingPathData> routingPathDataList = routingPathDataRepository.findAllByControllerIdAndFetchPath(getControllerId(source));
        List<String> shortestPath = getShortestPathInRoutingTable(routingPathDataList, source);
        String result = String.join(", ", shortestPath);
//        String result = shortestPath.stream().map(Vertex::getLabel).collect(Collectors.joining(", "));
        return result;
    }
}
