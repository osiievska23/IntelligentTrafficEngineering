package com.valentyna.intelligent.traffic.engineering.graph.plot;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphDraw extends JFrame {

    private Map<String, Node> nodes = new HashMap<>();
    private Map<String, Edge> edges = new HashMap<>();
    private ArrayList<String> pathEdges = new ArrayList<>();
    private final int WIDTH = 40;
    private final int HEIGHT = 40;

    public GraphDraw(String name) {
        this.setTitle(name);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void addNode(int x, int y, String label) {
        nodes.put(label, new Node(x, y, label));
        this.repaint();
    }

    public void addEdge(String first, String second) {
        edges.put(first + "-" + second, new Edge(nodes.get(first), nodes.get(second)));
        this.repaint();
    }

    public void printPath(String[] path) {
        for (int i = 0; i < path.length - 1; i++) {
            pathEdges.add(path[i] + "-" + path[i + 1]);
            pathEdges.add(path[i + 1] + "-" + path[i]);
        }
        this.repaint();
    }

    public void setSmallGraph() {
        nodes.put("v1", new Node(80, 200, "v1"));
        nodes.put("v2", new Node(150, 100, "v2"));
        nodes.put("v3", new Node(150, 300, "v3"));
        nodes.put("v4", new Node(210, 200, "v4"));
        nodes.put("v5", new Node(270, 80, "v5"));
        nodes.put("v6", new Node(370, 100, "v6"));
        nodes.put("v8", new Node(380, 310, "v8"));
        nodes.put("v7", new Node(350, 200, "v7"));
        nodes.put("v9", new Node(450, 200, "v9"));

        edges.put("v1" + "-" + "v2", new Edge(nodes.get("v1"), nodes.get("v2")));
        edges.put("v1" + "-" + "v3", new Edge(nodes.get("v1"), nodes.get("v3")));
        edges.put("v2" + "-" + "v3", new Edge(nodes.get("v2"), nodes.get("v3")));
        edges.put("v2" + "-" + "v4", new Edge(nodes.get("v2"), nodes.get("v4")));
        edges.put("v2" + "-" + "v5", new Edge(nodes.get("v2"), nodes.get("v5")));
        edges.put("v3" + "-" + "v4", new Edge(nodes.get("v3"), nodes.get("v4")));
        edges.put("v3" + "-" + "v7", new Edge(nodes.get("v3"), nodes.get("v7")));
        edges.put("v3" + "-" + "v8", new Edge(nodes.get("v3"), nodes.get("v8")));
        edges.put("v4" + "-" + "v5", new Edge(nodes.get("v4"), nodes.get("v5")));
        edges.put("v4" + "-" + "v6", new Edge(nodes.get("v4"), nodes.get("v6")));
        edges.put("v4" + "-" + "v7", new Edge(nodes.get("v4"), nodes.get("v7")));
        edges.put("v5" + "-" + "v6", new Edge(nodes.get("v5"), nodes.get("v6")));
        edges.put("v6" + "-" + "v7", new Edge(nodes.get("v6"), nodes.get("v7")));
        edges.put("v6" + "-" + "v9", new Edge(nodes.get("v6"), nodes.get("v9")));
        edges.put("v7" + "-" + "v8", new Edge(nodes.get("v7"), nodes.get("v8")));
        edges.put("v7" + "-" + "v9", new Edge(nodes.get("v7"), nodes.get("v9")));
        edges.put("v8" + "-" + "v9", new Edge(nodes.get("v8"), nodes.get("v9")));

        this.repaint();
    }


    public void paint(Graphics g) {
        FontMetrics f = g.getFontMetrics();
        int nodeHeight = Math.max(this.HEIGHT, f.getHeight());

        g.setColor(Color.black);
        for (String e : edges.keySet()) {
            g.setColor(pathEdges.contains(e) ? Color.red : Color.black);
            g.drawLine(edges.get(e).getFirst().getX(), edges.get(e).getFirst().getY(),
                    edges.get(e).getSecond().getX(), edges.get(e).getSecond().getY());
        }

        for (String label : nodes.keySet()) {
            int nodeWidth = Math.max(this.WIDTH, f.stringWidth(label) + this.WIDTH / 2);
            g.setColor(Color.white);
            g.fillOval(nodes.get(label).getX() - nodeWidth / 2, nodes.get(label).getY() - nodeHeight / 2, nodeWidth, nodeHeight);
            g.setColor(Color.black);
            g.drawOval(nodes.get(label).getX() - nodeWidth / 2, nodes.get(label).getY() - nodeHeight / 2, nodeWidth, nodeHeight);
            g.drawString(label, nodes.get(label).getX() - f.stringWidth(label) / 2, nodes.get(label).getY() + f.getHeight() / 2);
        }
    }
}
