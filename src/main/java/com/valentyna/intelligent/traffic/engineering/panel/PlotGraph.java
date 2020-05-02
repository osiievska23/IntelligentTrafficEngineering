package com.valentyna.intelligent.traffic.engineering.panel;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlotGraph {

    private List<String> distinctVertex = new LinkedList<>();
    private List<String> sourceVertex = new LinkedList<>();
    private List<String> targetVertex = new LinkedList<>();
    private List<Double> edgeWeight = new LinkedList<>();
    private List<Set<String>> pathFromSourceToDestination = new ArrayList<>();
    private List<String> unreachableVertexes = new ArrayList<>();

    public void setGraphData(int amountOfVertices, String[][] graphData) {
        for (int i = 1; i <= amountOfVertices; i++) {
            distinctVertex.add("v" + i);
        }

        for (String[] data : graphData) {
            sourceVertex.add(data[0]);
            targetVertex.add(data[1]);
            edgeWeight.add(Double.parseDouble(data[2]));
        }
    }

    public void setPathFromSourceToDestination(String path) {
        String[] vertexes = path.split(" -> ");
        for (int i = 0; i < vertexes.length - 1; i++) {
            Set<String> set = new HashSet<>();
            set.addAll(Arrays.asList(vertexes[i], vertexes[i + 1]));
            pathFromSourceToDestination.add(set);
        }
    }

    public void visualizeDirectedGraph() {
        Graph<MyNode, MyLink> graph = new DirectedSparseGraph();

        Hashtable<String, MyNode> Graph_Nodes = new Hashtable<>();
        LinkedList<MyNode> Source_Node = new LinkedList<>();
        LinkedList<MyNode> Target_Node = new LinkedList<>();
        LinkedList<MyNode> Graph_Nodes_Only = new LinkedList<>();

        for (int i = 0; i < distinctVertex.size(); i++) {
            String node_name = distinctVertex.get(i);
            MyNode data = new MyNode(node_name);
            Graph_Nodes.put(distinctVertex.get(i), data);
            Graph_Nodes_Only.add(data);
        }

        //Now convert all source and target nodes into objects
        for (int t = 0; t < sourceVertex.size(); t++) {
            Source_Node.add(Graph_Nodes.get(sourceVertex.get(t)));
            Target_Node.add(Graph_Nodes.get(targetVertex.get(t)));
        }

        //Now add nodes and edges to the graph
        for (int i = 0; i < edgeWeight.size(); i++) {
            graph.addEdge(new MyLink(edgeWeight.get(i), new MyNode[]{Source_Node.get(i), Target_Node.get(i)}), Source_Node.get(i), Target_Node.get(i), EdgeType.DIRECTED);
        }

        Layout<MyNode, MyLink> layout = new FRLayout<>(graph);
        layout.setSize(new Dimension(600, 600));

        BasicVisualizationServer<MyNode, MyLink> viz = new BasicVisualizationServer<MyNode, MyLink>(layout);
        viz.setPreferredSize(new Dimension(600, 600));

        Transformer<MyNode, String> vertexLabelTransformer = new Transformer<MyNode, String>() {
            public String transform(MyNode vertex) {
                return vertex.getId();
            }
        };

        Transformer<MyNode, Paint> vertexColorTransformer = new Transformer<MyNode, Paint>() {
            @Override
            public Paint transform(MyNode myNode) {
                return pathFromSourceToDestination.stream().flatMap(Set::stream).collect(Collectors.toSet()).contains(myNode.getId())
                        ? Color.red : Color.blue;
            }
        };

        Transformer<MyLink, String> edgeLabelTransformer = new Transformer<MyLink, String>() {
            public String transform(MyLink edge) {
                return String.valueOf(edge.getWeight());

            }
        };

        Transformer<MyLink, Paint> edgePaintTransformer = new Transformer<MyLink, Paint>() {
            @Override
            public Paint transform(MyLink myLink) {
                return pathFromSourceToDestination.contains(myLink.getVertexes()) ? Color.red : Color.black;
            }
        };

        Predicate<Context<Graph<MyNode, MyLink>, MyLink>> edgeArrowPredicate = new Predicate<Context<Graph<MyNode, MyLink>, MyLink>>() {
            @Override
            public boolean evaluate(Context<Graph<MyNode, MyLink>, MyLink> graphMyLinkContext) {
                return false;
            }
        };

        viz.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);
        viz.getRenderContext().setEdgeDrawPaintTransformer(edgePaintTransformer);
        viz.getRenderContext().setEdgeArrowPredicate(edgeArrowPredicate);
        viz.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);
        viz.getRenderContext().setVertexFillPaintTransformer(vertexColorTransformer);

        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(viz);
        frame.pack();
        frame.setVisible(true);
    }
}
