package com.valentyna.intelligent.traffic.engineering.graph;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class Graph {

    private Map<Vertex, List<Vertex>> adjacencyVertices = new HashMap<>();
    private Map<String, Double> edgesWeights = new HashMap<>();

    public void addVertex(String label) {
        adjacencyVertices.putIfAbsent(new Vertex(label), new ArrayList<>());
    }

    public void removeVertex(String label) {
        Vertex vertex = new Vertex(label);
        adjacencyVertices.values().forEach(adj -> adj.remove(vertex));
        adjacencyVertices.remove(vertex);
    }

    public void addEdge(String firstLabel, String secondLabel, double weight) {
        Vertex firstVertex = new Vertex(firstLabel);
        Vertex secondVertex = new Vertex(secondLabel);

        adjacencyVertices.get(firstVertex).add(secondVertex);
        adjacencyVertices.get(secondVertex).add(firstVertex);

        edgesWeights.putIfAbsent(firstLabel + "-" + secondLabel, weight);
        edgesWeights.putIfAbsent(secondLabel + "-" + firstLabel, weight);
    }

    public void removeEdge(String firstLabel, String secondLabel) {
        Vertex firstVertex = new Vertex(firstLabel);
        Vertex secondVertex = new Vertex(secondLabel);

        List<Vertex> firstAdjacent = adjacencyVertices.get(firstVertex);
        List<Vertex> secondAdjacent = adjacencyVertices.get(secondVertex);

        if (firstAdjacent != null) {
            firstAdjacent.remove(secondVertex);
        }

        if (secondAdjacent != null) {
            secondAdjacent.remove(firstVertex);
        }

        edgesWeights.remove(firstLabel + "-" + secondLabel);
        edgesWeights.remove(secondLabel + "-" + firstLabel);
    }

    public void printGraph() {
        for (Map.Entry<Vertex, List<Vertex>> entry : adjacencyVertices.entrySet()) {
            System.out.println(entry.getKey().getLabel() + " -> " + entry.getValue().stream().map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }
}
