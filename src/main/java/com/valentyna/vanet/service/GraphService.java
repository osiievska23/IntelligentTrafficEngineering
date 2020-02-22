package com.valentyna.vanet.service;

import com.valentyna.vanet.graph.AdjacencyVertices;
import com.valentyna.vanet.graph.Graph;
import com.valentyna.vanet.graph.Vertex;
import com.valentyna.vanet.routing.RoutingPathInformation;
import com.valentyna.vanet.routing.SwitchRoutingTable;

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

public class GraphService {

    private Graph graph;

    private Vertex source;
    private Vertex destination;
    private List<SwitchRoutingTable> switchRoutingTables = new ArrayList<>();
    private List<Vertex> queue = new ArrayList<>();

    public GraphService(Graph graph, Vertex source, Vertex destination) {
        this.graph = graph;
        this.source = source;
        this.destination = destination;
    }

    public List<SwitchRoutingTable> generateRoutingInformationInNetworkSwitches() {

        AdjacencyVertices firstVerticesLayer = new AdjacencyVertices(1, Set.of(this.destination), new HashSet<>());
        AdjacencyVertices secondVerticesLayer = getNextAdjacencyVertices(firstVerticesLayer);

        secondVerticesLayer.getCurrentVertices().forEach(v -> createNewRoutingTable(firstVerticesLayer, v));
        AdjacencyVertices nextVertices = getNextAdjacencyVertices(secondVerticesLayer);

        while (sourceNotReached(nextVertices)) {

            for (Vertex currentVertex : nextVertices.getCurrentVertices()) {
                gatherRoutingInformationFromNextVerticesSet(nextVertices, currentVertex);
            }

            while (!queue.isEmpty()) {
                Vertex currentVertex = queue.get(0);

                gatherRoutingInformationFromNextVerticesSet(nextVertices, currentVertex);

                queue.remove(currentVertex);
            }
            nextVertices = getNextAdjacencyVertices(nextVertices);
        }

        printPathFromSourceToDestination();

        return switchRoutingTables;
    }

    private void gatherRoutingInformationFromNextVerticesSet(AdjacencyVertices adjacencyVerticesSet, Vertex currentVertex) {
        if (tableExist(getControllerId(currentVertex))) {
            updateRoutingTable(adjacencyVerticesSet, currentVertex);
        } else {
            createNewRoutingTable(adjacencyVerticesSet, currentVertex);
        }
    }

    private void createNewRoutingTable(AdjacencyVertices adjacencyVertices, Vertex currentVertex) {
        SwitchRoutingTable switchRoutingTable = new SwitchRoutingTable();

        switchRoutingTable.setControllerId(getControllerId(currentVertex));
        List<RoutingPathInformation> routingPathInformationList = createRoutingPathInformationList(adjacencyVertices, currentVertex);
        switchRoutingTable.setRoutingPathInformations(routingPathInformationList);

        switchRoutingTables.add(switchRoutingTable);
    }

    private void updateRoutingTable(AdjacencyVertices adjacencyVertices, Vertex currentVertex) {
        SwitchRoutingTable switchRoutingTable = getRoutingTableByControllerId(getControllerId(currentVertex));
        List<RoutingPathInformation> newRoutingPathInformationList = createRoutingPathInformationList(adjacencyVertices, currentVertex);
        switchRoutingTable.getRoutingPathInformations().addAll(newRoutingPathInformationList);
    }

    private List<RoutingPathInformation> createRoutingPathInformationList(AdjacencyVertices adjacencyVertices, Vertex currentVertex) {
        List<RoutingPathInformation> pathInfoList = new ArrayList<>();

        if (adjacencyVertices.getCurrentVertices().contains(this.destination)) {
            List<Vertex> path = Arrays.asList(currentVertex, this.destination);
            RoutingPathInformation routingPathInformation = createRoutingPathInformation(currentVertex, path);
            pathInfoList.add(routingPathInformation);
            return pathInfoList;
        }

        Set<Vertex> possibleAdjVertex = new HashSet<>();
        possibleAdjVertex.addAll(adjacencyVertices.getCurrentVertices());
        possibleAdjVertex.addAll(adjacencyVertices.getPreviousVertices());

        for (Vertex adjVertex : possibleAdjVertex) {

            if (vertexCanBePartOfPath(currentVertex, adjVertex)) {

                if (tableExist(getControllerId(adjVertex))) {

                    List<Vertex> path = getShortestPathFromVertexToDestination(currentVertex, getControllerId(adjVertex));
                    RoutingPathInformation routingPathInformation = createRoutingPathInformation(adjVertex, path);
                    pathInfoList.add(routingPathInformation);

                } else {

                    this.queue.add(currentVertex);
                }
            }
        }

        return pathInfoList;
    }

    private RoutingPathInformation createRoutingPathInformation(Vertex adjacentTop, List<Vertex> path) {
        RoutingPathInformation routingPathInformation = new RoutingPathInformation();

        routingPathInformation.setAdjacentTop(adjacentTop);
        routingPathInformation.setWeight(getPathWeight(path));
        routingPathInformation.setPath(path);
        routingPathInformation.setDestination(this.destination);

        return routingPathInformation;
    }

    private double getPathWeight(List<Vertex> path) {
        return IntStream.range(0, path.size() - 1)
                .mapToDouble(i -> this.graph.getEdgesWeights().get(path.get(i).getLabel() + "-" + path.get(i + 1).getLabel()))
                .sum();
    }

    private List<Vertex> getShortestPathFromVertexToDestination(Vertex currentVertex, int adjControllerId) {
        List<Vertex> result = new ArrayList<>();
        result.add(currentVertex);

        SwitchRoutingTable switchRoutingTable = getRoutingTableByControllerId(adjControllerId);
        result.addAll(getShortestPathInRoutingTable(switchRoutingTable, currentVertex));

        return result;
    }

    private List<Vertex> getShortestPathInRoutingTable(SwitchRoutingTable switchRoutingTable, Vertex currentVertex) {
        return switchRoutingTable.getRoutingPathInformations().stream()
                .filter(i -> !isCurrentVertex(i.getAdjacentTop(), currentVertex))
                .min(Comparator.comparing(RoutingPathInformation::getWeight))
                .map(RoutingPathInformation::getPath)
                .orElse(Collections.emptyList());
    }

    private boolean tableExist(int adjControllerId) {
        return switchRoutingTables.stream()
                .anyMatch(t -> t.getControllerId() == adjControllerId);
    }

    private SwitchRoutingTable getRoutingTableByControllerId(int adjControllerId) {
        return switchRoutingTables.stream()
                .filter(t -> t.getControllerId() == adjControllerId)
                .findAny()
                .orElse(null);
    }

    private AdjacencyVertices getNextAdjacencyVertices(AdjacencyVertices previousSet) {
        int nextNumber = previousSet.getNumber() + 1;

        Set<Vertex> nextVertices = previousSet.getCurrentVertices().stream()
                .map(v -> this.graph.getAdjacencyVertices().get(v))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        nextVertices.removeAll(previousSet.getCurrentVertices());
        nextVertices.removeAll(previousSet.getPreviousVertices());

        return new AdjacencyVertices(nextNumber, nextVertices, previousSet.getCurrentVertices());
    }

    private boolean vertexCanBePartOfPath(Vertex currentVertex, Vertex vertex) {
        return !isCurrentVertex(currentVertex, vertex)
                && !isVertexDestination(vertex)
                && isAdjacencyVertex(currentVertex, vertex)
                && !isVisitedAdjacencyVertex(currentVertex, vertex);
    }

    private boolean isVisitedAdjacencyVertex(Vertex currentVertex, Vertex adjVertex) {
        SwitchRoutingTable switchRoutingTable = getRoutingTableByControllerId(getControllerId(currentVertex));
        return switchRoutingTable != null && switchRoutingTable.getRoutingPathInformations().stream()
                .anyMatch(i -> i.getAdjacentTop().equals(adjVertex));
    }

    private boolean isAdjacencyVertex(Vertex currentVertex, Vertex vertex) {
        return this.graph.getAdjacencyVertices().get(currentVertex).contains(vertex);
    }

    private boolean isVertexDestination(Vertex adjVertex) {
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

    private void printPathFromSourceToDestination() {
        SwitchRoutingTable sourceSwitchRoutingTable = getRoutingTableByControllerId(getControllerId(this.source));
        List<Vertex> shortestPath = getShortestPathInRoutingTable(sourceSwitchRoutingTable, this.source);
        String result = shortestPath.stream().map(Vertex::getLabel).collect(Collectors.joining(", "));
        System.out.println(result);
    }
}
