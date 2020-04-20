package com.valentyna.vanet.panel;

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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PlotGraph {

    private int edgeCount_Directed = 0;   // This works with the inner MyEdge class
    private List<String> distinctVertex = new LinkedList<>();
    private List<String> sourceVertex = new LinkedList<>();
    private List<String> targetVertex = new LinkedList<>();
    private List<Double> edgeWeight = new LinkedList<>();
    private List<List<String>> pathFromSourceToDestination = new ArrayList<>();

    public void setGraphData(List<String> vertexes) {
        for (int i = 1; i <= 16; i++) {
            distinctVertex.add("v" + i);
        }

        setGraphEdge("v1", "v2", 0.3);
        setGraphEdge("v1", "v3", 0.1);
        setGraphEdge("v1", "v4", 0.4);

        setGraphEdge("v2", "v3", 0.2);
        setGraphEdge("v2", "v5", 0.2);
        setGraphEdge("v2", "v6", 0.3);

        setGraphEdge("v3", "v4", 0.2);
        setGraphEdge("v3", "v6", 0.3);
        setGraphEdge("v3", "v7", 0.2);

        setGraphEdge("v4", "v8", 0.3);

        setGraphEdge("v5", "v6", 0.2);
        setGraphEdge("v5", "v9", 0.6);

        setGraphEdge("v6", "v10", 0.2);

        setGraphEdge("v7", "v8", 0.3);
        setGraphEdge("v7", "v10", 0.6);
        setGraphEdge("v7", "v11", 0.3);

        setGraphEdge("v8", "v11", 0.6);
        setGraphEdge("v8", "v12", 0.6);

        setGraphEdge("v9", "v10", 0.3);
        setGraphEdge("v9", "v13", 0.6);
        setGraphEdge("v9", "v14", 0.5);

        setGraphEdge("v10", "v13", 0.2);

        setGraphEdge("v11", "v12", 0.2);
        setGraphEdge("v11", "v15", 0.4);

        setGraphEdge("v12", "v15", 0.2);

        setGraphEdge("v13", "v14", 0.3);
        setGraphEdge("v13", "v15", 0.3);
        setGraphEdge("v13", "v16", 0.2);

        setGraphEdge("v14", "v16", 0.6);

        setGraphEdge("v15", "v16", 0.3);
    }

    public void setPathFromSourceToDestination(String path) {
        String[] vertexes = path.split(", ");
        for (int i = 0; i < vertexes.length - 1; i++) {
            pathFromSourceToDestination.add(Arrays.asList(vertexes[i], vertexes[i + 1]));
        }
    }

    public void setGraphEdge(String first, String second, double weight) {
        sourceVertex.add(first);
        targetVertex.add(second);
        edgeWeight.add(weight);
    }

    public void visualizeDirectedGraph() {
        Graph<MyNode, MyLink> graph = new DirectedSparseGraph();

        Hashtable<String, MyNode> Graph_Nodes = new Hashtable<String, PlotGraph.MyNode>();
        LinkedList<MyNode> Source_Node = new LinkedList<PlotGraph.MyNode>();
        LinkedList<MyNode> Target_Node = new LinkedList<PlotGraph.MyNode>();
        LinkedList<MyNode> Graph_Nodes_Only = new LinkedList<PlotGraph.MyNode>();

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
            graph.addEdge(new MyLink(new MyNode[]{Source_Node.get(i), Target_Node.get(i)}, edgeWeight.get(i)), Source_Node.get(i), Target_Node.get(i), EdgeType.DIRECTED);
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

        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(viz);
        frame.pack();
        frame.setVisible(true);
    }

    class MyNode {

        private String id;

        public MyNode(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

//        public String Node_Property() {
//            String node_prop = id;
//            return (node_prop);
//        }
    }

    class MyLink {

        private double weight;
        private MyNode[] vertexes;
        private int id;

        public MyLink(MyNode[] vertexes, double weight) {
            this.id = edgeCount_Directed++;
            this.vertexes = vertexes;
            this.weight = weight;
        }

        public String toString() {
            return "E" + id;
        }

        public double getWeight() {
            return weight;
        }

        public List<String> getVertexes() {
            return Arrays.stream(vertexes).map(MyNode::getId).collect(Collectors.toList());
        }
    }
}
