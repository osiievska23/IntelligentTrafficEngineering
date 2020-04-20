package com.valentyna.vanet.service;

import com.valentyna.vanet.graph.AdjacencyVertices;
import com.valentyna.vanet.graph.Graph;
import com.valentyna.vanet.graph.Vertex;
import com.valentyna.vanet.routing.ControllerRoutingTable;
import com.valentyna.vanet.routing.RoutingPathData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GraphService {

    private Graph graph;
    private Vertex source;
    private Vertex destination;

    private List<ControllerRoutingTable> controllerRoutingTables = new ArrayList<>();
    private List<Vertex> queue = new ArrayList<>();

    public GraphService(Graph graph, Vertex source, Vertex destination) {
        this.graph = graph;
        this.source = source;
        this.destination = destination;
    }

    public List<ControllerRoutingTable> generateRoutingInformationInNetworkSwitches() {

        createEmptyRoutingTablesForExistentVertices(); // step 1 [create empty tables for all vertices]

        AdjacencyVertices firstVertices = getFirstAdjacencyVertices(); // step 2 [create first vertices layer]
        AdjacencyVertices nextVertices = getNextAdjacencyVertices(firstVertices);

        nextVertices.getCurrentVertices().forEach(this::gatherFirstRoutingPathInformation); // step 3 [fill first tables with data]

        for (Vertex currentVertex : nextVertices.getCurrentVertices()) { // step 4 [exchange of routing information between switches of the same level]
            updateRoutingTable(nextVertices, currentVertex);
        }

        while (sourceNotReached(nextVertices)) {
            nextVertices = getNextAdjacencyVertices(nextVertices);

            for (Vertex currentVertex : nextVertices.getCurrentVertices()) {
                updateRoutingTable(nextVertices, currentVertex);
            }

            while (!queue.isEmpty()) {
                Vertex currentVertex = queue.get(0);
                updateRoutingTable(nextVertices, currentVertex);
                queue.remove(currentVertex);
            }
        }

        System.out.println(getPathFromSourceToDestination());
        return controllerRoutingTables;
    }

    private void createEmptyRoutingTablesForExistentVertices() {
        graph.getAdjacencyVertices()
                .forEach((key, value) -> controllerRoutingTables.add(
                        ControllerRoutingTable.builder()
                                .id(getControllerId(key))
                                .routingPathInfo(new ArrayList<>())
                                .empty(true)
                                .build()));
    }

    private void updateRoutingTable(AdjacencyVertices adjacencyVertices, Vertex currentVertex) {
        ControllerRoutingTable routingTable = getRoutingTableByControllerId(getControllerId(currentVertex));
        routingTable.addNewRoutingData(gatherRoutingPathInformation(adjacencyVertices, currentVertex));
        routingTable.setEmpty(false);
    }

    private void gatherFirstRoutingPathInformation(Vertex currentVertex) {
        ControllerRoutingTable routingTable = getRoutingTableByControllerId(getControllerId(currentVertex));

        List<RoutingPathData> pathInfoList = new ArrayList<>();
        RoutingPathData routingPathData = createRoutingPathData(currentVertex, this.destination);
        pathInfoList.add(routingPathData);

        routingTable.addNewRoutingData(pathInfoList);
        routingTable.setEmpty(false);
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
                .filter(v -> tableIsNotEmpty(getControllerId(v)))
                .forEach(v -> pathInfoList.add(createRoutingPathData(currentVertex, v)));

        possibleAdjVertex.stream()
                .filter(v -> !tableIsNotEmpty(getControllerId(v)))
                .forEach(v -> this.queue.add(currentVertex));

        return pathInfoList;
    }

    private RoutingPathData createRoutingPathData(Vertex currentVertex, Vertex adjVertex) {
        List<Vertex> path = getShortestPathFromVertexToDestination(currentVertex, adjVertex);
        return RoutingPathData.builder()
                .adjacentTop(adjVertex)
                .weight(getPathWeight(path))
                .path(path)
                .destination(this.destination)
                .build();
    }

    private double getPathWeight(List<Vertex> path) {
        return IntStream.range(0, path.size() - 1)
                .mapToDouble(i -> this.graph.getEdgesWeights().get(path.get(i).getLabel() + "-" + path.get(i + 1).getLabel()))
                .sum();
    }

    private List<Vertex> getShortestPathFromVertexToDestination(Vertex currentVertex, Vertex adjVertex) {
        if (isDestinationVertex(adjVertex)) {
            return Arrays.asList(currentVertex, adjVertex);
        }

        List<Vertex> result = new ArrayList<>();
        result.add(currentVertex);

        ControllerRoutingTable routingTable = getRoutingTableByControllerId(getControllerId(adjVertex));
        result.addAll(getShortestPathInRoutingTable(routingTable, currentVertex));

        return result;
    }

    private List<Vertex> getShortestPathInRoutingTable(ControllerRoutingTable routingTable, Vertex currentVertex) {
        return routingTable.getRoutingPathInfo().stream()
                .filter(i -> !isCurrentVertex(i.getAdjacentTop(), currentVertex))
                .min(Comparator.comparing(RoutingPathData::getWeight))
                .map(RoutingPathData::getPath)
                .orElse(Collections.emptyList());
    }

    private boolean tableIsNotEmpty(int adjControllerId) {
        return controllerRoutingTables.stream()
                .anyMatch(t -> t.getId() == adjControllerId && !t.isEmpty());
    }

    private ControllerRoutingTable getRoutingTableByControllerId(int adjControllerId) {
        return controllerRoutingTables.stream()
                .filter(t -> t.getId() == adjControllerId)
                .findAny()
                .orElse(null);
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
        ControllerRoutingTable routingTable = getRoutingTableByControllerId(getControllerId(currentVertex));
        return routingTable != null && routingTable.getRoutingPathInfo().stream()
                .anyMatch(i -> i.getAdjacentTop().equals(adjVertex));
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
        ControllerRoutingTable sourceControllerRoutingTable = getRoutingTableByControllerId(getControllerId(this.source));
        List<Vertex> shortestPath = getShortestPathInRoutingTable(sourceControllerRoutingTable, this.source);
        String result = shortestPath.stream().map(Vertex::getLabel).collect(Collectors.joining(", "));
        return result;
    }
}
