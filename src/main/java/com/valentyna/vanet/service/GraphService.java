package com.valentyna.vanet.service;

import com.valentyna.vanet.graph.AdjacencyVertices;
import com.valentyna.vanet.graph.Graph;
import com.valentyna.vanet.graph.Vertex;
import com.valentyna.vanet.routing.RoutingPathInformation;
import com.valentyna.vanet.routing.SwitchRoutingTable;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        generateSwitchRoutingTablesForSecondVerticesLayer(secondVerticesLayer);
        AdjacencyVertices nextVertices = getNextAdjacencyVertices(secondVerticesLayer);

        while (sourceNotReached(nextVertices)) {

            gatherRoutingInformationFromNextVerticesSet(nextVertices);
            nextVertices = getNextAdjacencyVertices(nextVertices);
        }

        printPathFromSourceToDestination();

        return switchRoutingTables;
    }

    private void gatherRoutingInformationFromNextVerticesSet(AdjacencyVertices adjacencyVerticesSet) {
        adjacencyVerticesSet.getCurrentVertices().forEach(v -> updateRoutingTable(adjacencyVerticesSet, v));

        while (!queue.isEmpty()) {
            updateRoutingTable(adjacencyVerticesSet, queue.get(0));
            queue.remove(0);
        }
    }

    private void generateSwitchRoutingTablesForSecondVerticesLayer(AdjacencyVertices secondVerticesLayer) {
        secondVerticesLayer.getCurrentVertices().forEach(v -> createFirstRoutingTables(v));
    }

    private RoutingPathInformation createRoutingPathInformation(Vertex adjacentTop, List<Vertex> path) {
        RoutingPathInformation routingPathInformation = new RoutingPathInformation();

        routingPathInformation.setAdjacentTop(adjacentTop);
        routingPathInformation.setWeight(getPathEdgeWeight(path));
        routingPathInformation.setPath(path);
        routingPathInformation.setDestination(this.destination);

        return routingPathInformation;
    }

    private void createNewRoutingTable(AdjacencyVertices adjacencyVertices, Vertex currentVertex) {
        SwitchRoutingTable switchRoutingTable = new SwitchRoutingTable();

        switchRoutingTable.setControllerId(getControllerId(currentVertex));
        List<RoutingPathInformation> newRoutingPathInformationList = calculateRoutingPathData(adjacencyVertices, currentVertex);
        switchRoutingTable.setRoutingPathData(newRoutingPathInformationList);

        switchRoutingTables.add(switchRoutingTable);
    }

    private void createFirstRoutingTables(Vertex currentVertex) {
        SwitchRoutingTable switchRoutingTable = new SwitchRoutingTable();

        switchRoutingTable.setControllerId(getControllerId(currentVertex));

        List<RoutingPathInformation> pathDataList = new ArrayList<>();

        List<Vertex> path = Arrays.asList(currentVertex, destination);

        RoutingPathInformation routingPathInformation = createRoutingPathInformation(currentVertex, path);

        pathDataList.add(routingPathInformation);

        switchRoutingTable.setRoutingPathData(pathDataList);

        switchRoutingTables.add(switchRoutingTable);
    }

    private void updateRoutingTable(AdjacencyVertices adjacencyVertices, Vertex currentVertex) {

        if (!tableExist(getControllerId(currentVertex))) {

            createNewRoutingTable(adjacencyVertices, currentVertex);
        } else {

            SwitchRoutingTable switchRoutingTable = getRoutingTableByControllerId(getControllerId(currentVertex));
            List<RoutingPathInformation> newRoutingPathInformationList = calculateRoutingPathData(adjacencyVertices, currentVertex);
            switchRoutingTable.getRoutingPathData().addAll(newRoutingPathInformationList);
        }
    }

    private List<RoutingPathInformation> calculateRoutingPathData(AdjacencyVertices adjacencyVertices, Vertex currentVertex) {
        List<RoutingPathInformation> dataList = new ArrayList<>();

        Set<Vertex> possibleAdjVertex = new HashSet<>();
        possibleAdjVertex.addAll(adjacencyVertices.getCurrentVertices());
        possibleAdjVertex.addAll(adjacencyVertices.getPreviousVertices());

        for (Vertex adjVertex : possibleAdjVertex) {

            if (vertexCanBePartOfPath(currentVertex, adjVertex) && currentVertexHasNotPathDataWithThisAdjVertex(currentVertex, adjVertex)) {

                if (tableExist(getControllerId(adjVertex))) {

                    List<Vertex> path = getShortestPathFromAdjVertexToDestination(getControllerId(adjVertex), currentVertex);

                    RoutingPathInformation routingPathInformation = createRoutingPathInformation(adjVertex, path);
                    dataList.add(routingPathInformation);

                } else {

                    this.queue.add(currentVertex);
                }
            }
        }

        return dataList;
    }

    private boolean currentVertexHasNotPathDataWithThisAdjVertex(Vertex currentVertex, Vertex adjVertex) {
        SwitchRoutingTable switchRoutingTable = getRoutingTableByControllerId(getControllerId(currentVertex));

        if (switchRoutingTable == null) {
            return true;
        }

        for (RoutingPathInformation pathData : switchRoutingTable.getRoutingPathData()) {

            if (pathData.getAdjacentTop().equals(adjVertex)) {
                return false;
            }
        }

        return true;
    }

    private boolean vertexCanBePartOfPath(Vertex currentVertex, Vertex adjVertex) {
        return !adjVertex.equals(currentVertex) && !adjVertex.equals(this.destination) && this.graph.getAdjacencyVertices().get(currentVertex).contains(adjVertex);
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

    private List<Vertex> getShortestPathFromAdjVertexToDestination(int adjControllerId, Vertex currentVertex) {
        List<Vertex> result = new ArrayList<>();
        result.add(currentVertex);

        if (tableExist(adjControllerId)) {

            SwitchRoutingTable switchRoutingTable = getRoutingTableByControllerId(adjControllerId);
            result.addAll(getShortestPathInRoutingTable(switchRoutingTable, currentVertex));
        }

        return result;
    }

    private List<Vertex> getShortestPathInRoutingTable(SwitchRoutingTable switchRoutingTable, Vertex currentVertex) {
        List<Vertex> result = new ArrayList<>();

        double min = 100000000;

        for (RoutingPathInformation pathData : switchRoutingTable.getRoutingPathData()) {
            if (pathData.getWeight() < min && !pathData.getAdjacentTop().equals(currentVertex)) {
                min = pathData.getWeight();
                result = pathData.getPath();
            }
        }
        return result;
    }

    private boolean tableExist(int adjControllerId) {
        for (SwitchRoutingTable table : switchRoutingTables) {

            if (table.getControllerId() == adjControllerId) {
                return true;
            }
        }

        return false;
    }

    private SwitchRoutingTable getRoutingTableByControllerId(int adjControllerId) {
        for (SwitchRoutingTable table : switchRoutingTables) {

            if (table.getControllerId() == adjControllerId) {
                return table;
            }
        }
        return null;
    }

    private AdjacencyVertices getNextAdjacencyVertices(AdjacencyVertices previousSet) {
        int nextAdjacencyVerticesLayerNumber = previousSet.getNumber() + 1;

        Set<Vertex> nextVertices = new HashSet<>();
        previousSet.getCurrentVertices().forEach(vertex -> nextVertices.addAll(this.graph.getAdjacencyVertices().get(vertex)));

        nextVertices.removeAll(previousSet.getCurrentVertices());
        nextVertices.removeAll(previousSet.getPreviousVertices());

        return new AdjacencyVertices(nextAdjacencyVerticesLayerNumber, nextVertices, previousSet.getCurrentVertices());
    }

    private int getControllerId(Vertex vertex) {
        return Integer.valueOf(vertex.getLabel().substring(1));
    }

    private boolean sourceNotReached(AdjacencyVertices nextVertices) {
        return !nextVertices.getPreviousVertices().contains(source);
    }

    private void printPathFromSourceToDestination() {
        SwitchRoutingTable sourceSwitchRoutingTable = getRoutingTableByControllerId(getControllerId(this.source));
        List<Vertex> shortestPath = getShortestPathInRoutingTable(sourceSwitchRoutingTable, this.source);
        String result = shortestPath.stream().map(Vertex::getLabel).collect(Collectors.joining(", "));
        System.out.println(result);
    }
}
