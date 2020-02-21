package com.valentyna.vanet.service;

import com.valentyna.vanet.graph.AdjacencyVertices;
import com.valentyna.vanet.graph.Graph;
import com.valentyna.vanet.graph.Vertex;
import com.valentyna.vanet.routing.RoutingPathData;
import com.valentyna.vanet.routing.RoutingTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphService {

    private Graph graph;

    private Vertex source;
    private Vertex destination;

    private List<RoutingTable> routingTables = new ArrayList<>();

    private AdjacencyVertices firstVerticesLayer;
    private AdjacencyVertices secondVerticesLayer;
    private List<Vertex> queue = new ArrayList<>();

    public GraphService(Graph graph, Vertex source, Vertex destination) {
        this.graph = graph;
        this.source = source;
        this.destination = destination;
    }

    public List<RoutingTable> generateRoutingInformationInNetworkSwitches() {

        this.firstVerticesLayer = new AdjacencyVertices(1, Set.of(destination), new HashSet<>());
        this.secondVerticesLayer = getNextAdjacencyVertices(firstVerticesLayer);

        secondVerticesLayer.getCurrentVertices().forEach(v -> createFirstRoutingTables(v, destination, source));

        secondVerticesLayer.getCurrentVertices().forEach(v -> updateRoutingTable(secondVerticesLayer, v, destination, source));

        AdjacencyVertices nextVertices = getNextAdjacencyVertices(secondVerticesLayer);

        while (!nextVertices.getPreviousVertices().contains(source)) {
            AdjacencyVertices finalNextVertices = nextVertices;
            nextVertices.getCurrentVertices().forEach(v -> updateRoutingTable(finalNextVertices, v, destination, source));

            while (!queue.isEmpty()) {
                updateRoutingTable(finalNextVertices, queue.get(0), destination, source);
                queue.remove(0);
            }

            nextVertices = getNextAdjacencyVertices(nextVertices);
        }

        printPathFromSourceToDestination();

        return routingTables;
    }

    private void createFirstRoutingTables(Vertex currentVertex, Vertex destination, Vertex source) {
        RoutingTable routingTable = new RoutingTable();

        routingTable.setControllerId(getControllerId(currentVertex));

        List<RoutingPathData> pathDataList = new ArrayList<>();

        RoutingPathData routingPathData = new RoutingPathData();

        List<Vertex> path = new ArrayList<>();
        path.add(currentVertex);
        path.add(destination);

        routingPathData.setAdjacentTop(currentVertex);
        routingPathData.setWeight(getEdgeWeight(currentVertex, destination));
        routingPathData.setPath(path);
        routingPathData.setDestination(destination);

        pathDataList.add(routingPathData);
        routingTable.setRoutingPathData(pathDataList);

        routingTables.add(routingTable);
    }

    private void updateRoutingTable(AdjacencyVertices adjacencyVertices, Vertex currentVertex, Vertex destination, Vertex source) {

        if (!tableExist(getControllerId(currentVertex))) {

            createNewRoutingTable(adjacencyVertices, currentVertex, destination, source);
        } else {

            RoutingTable routingTable = getRoutingTableByControllerId(getControllerId(currentVertex));
            List<RoutingPathData> newRoutingPathDataList = calculateRoutingPathData(adjacencyVertices, currentVertex, destination, source);
            routingTable.getRoutingPathData().addAll(newRoutingPathDataList);
        }
    }

    private void createNewRoutingTable(AdjacencyVertices adjacencyVertices, Vertex currentVertex, Vertex destination, Vertex source) {
        RoutingTable routingTable = new RoutingTable();

        routingTable.setControllerId(getControllerId(currentVertex));
        List<RoutingPathData> newRoutingPathDataList = calculateRoutingPathData(adjacencyVertices, currentVertex, destination, source);
        routingTable.setRoutingPathData(newRoutingPathDataList);

        routingTables.add(routingTable);
    }

    private List<RoutingPathData> calculateRoutingPathData(AdjacencyVertices adjacencyVertices, Vertex currentVertex, Vertex destination, Vertex source) {
        List<RoutingPathData> dataList = new ArrayList<>();

        Set<Vertex> possibleAdjVertex = new HashSet<>();
        possibleAdjVertex.addAll(adjacencyVertices.getCurrentVertices());
        possibleAdjVertex.addAll(adjacencyVertices.getPreviousVertices());

        for (Vertex adjVertex : possibleAdjVertex) {

            if (vertexCanBePartOfPath(currentVertex, adjVertex, destination) && currentVertexHasNotPathDataWithThisAdjVertex(currentVertex, adjVertex)) {

                if (tableExist(getControllerId(adjVertex))) {

                    RoutingPathData routingPathData = createRoutingData(currentVertex, adjVertex, destination);
                    dataList.add(routingPathData);

                } else {

                    this.queue.add(currentVertex);
                }
            }
        }

        return dataList;
    }

    private boolean currentVertexHasNotPathDataWithThisAdjVertex(Vertex currentVertex, Vertex adjVertex) {
        RoutingTable routingTable = getRoutingTableByControllerId(getControllerId(currentVertex));

        if (routingTable == null) {
            return true;
        }

        for (RoutingPathData pathData : routingTable.getRoutingPathData()) {

            if (pathData.getAdjacentTop().equals(adjVertex)) {
                return false;
            }
        }

        return true;
    }

    private boolean vertexCanBePartOfPath(Vertex currentVertex, Vertex adjVertex, Vertex destination) {
        return !adjVertex.equals(currentVertex) && !adjVertex.equals(destination) && this.graph.getAdjacencyVertices().get(currentVertex).contains(adjVertex);
    }

    private RoutingPathData createRoutingData(Vertex currentVertex, Vertex adjVertex, Vertex destination) {
        RoutingPathData routingPathData = new RoutingPathData();

        routingPathData.setAdjacentTop(adjVertex);
        routingPathData.setDestination(destination);

        List<Vertex> path = new ArrayList<>();
        path.add(currentVertex);
        path.addAll(getShortestPathFromAdjVertexToDestination(getControllerId(adjVertex), currentVertex));

        routingPathData.setPath(path);
        routingPathData.setWeight(getPathEdgeWeight(path));

        return routingPathData;
    }

    private double getPathEdgeWeight(List<Vertex> path) {
        double total = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            String key = path.get(i).getLabel() + "-" + path.get(i + 1).getLabel();
            double weight = this.graph.getEdgesWeights().get(key);
            total += weight;
        }

        return total;
    }

    private double getEdgeWeight(Vertex currentVertex, Vertex vertex) {
        return this.graph.getEdgesWeights().get(currentVertex.getLabel() + "-" + vertex.getLabel());
    }

    private List<Vertex> getShortestPathFromAdjVertexToDestination(int adjControllerId, Vertex currentVertex) {
        List<Vertex> result = new ArrayList<>();

        if (tableExist(adjControllerId)) {

            RoutingTable routingTable = getRoutingTableByControllerId(adjControllerId);
            result = getShortestPathInRoutingTable(routingTable, currentVertex);
        }

        return result;
    }

    private List<Vertex> getShortestPathInRoutingTable(RoutingTable routingTable, Vertex currentVertex) {
        List<Vertex> result = new ArrayList<>();

        double min = 100000000;

        for (RoutingPathData pathData : routingTable.getRoutingPathData()) {
            if (pathData.getWeight() < min && !pathData.getAdjacentTop().equals(currentVertex)) {
                min = pathData.getWeight();
                result = pathData.getPath();
            }
        }
        return result;
    }

    private boolean tableExist(int adjControllerId) {
        for (RoutingTable table : routingTables) {

            if (table.getControllerId() == adjControllerId) {
                return true;
            }
        }

        return false;
    }

    private RoutingTable getRoutingTableByControllerId(int adjControllerId) {
        for (RoutingTable table : routingTables) {

            if (table.getControllerId() == adjControllerId) {
                return table;
            }
        }
        return null;
    }

    private AdjacencyVertices getNextAdjacencyVertices(AdjacencyVertices previousSet) {
        int nextNumber = previousSet.getNumber() + 1;
        Set<Vertex> nextVertices = new HashSet<>();
        previousSet.getCurrentVertices().forEach(vertex -> nextVertices.addAll(this.graph.getAdjacencyVertices().get(vertex)));
        nextVertices.removeAll(previousSet.getCurrentVertices());
        nextVertices.removeAll(previousSet.getPreviousVertices());
        return new AdjacencyVertices(nextNumber, nextVertices, previousSet.getCurrentVertices());
    }

    private int getControllerId(Vertex vertex) {
        return Integer.valueOf(vertex.getLabel().substring(1));
    }

    private void printPathFromSourceToDestination() {
        RoutingTable sourceRoutingTable = getRoutingTableByControllerId(getControllerId(this.source));
        List<Vertex> shortestPath = getShortestPathInRoutingTable(sourceRoutingTable, this.source);
        String result = shortestPath.stream().map(Vertex::getLabel).collect(Collectors.joining(", "));
        System.out.println(result);
    }
}
