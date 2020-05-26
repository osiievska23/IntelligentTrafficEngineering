package com.valentyna.intelligent.traffic.engineering.listener;

import com.valentyna.intelligent.traffic.engineering.graph.Vertex;
import com.valentyna.intelligent.traffic.engineering.graph.plot.GraphDraw;
import com.valentyna.intelligent.traffic.engineering.panel.GraphData;
import com.valentyna.intelligent.traffic.engineering.panel.PlotGraph;
import com.valentyna.intelligent.traffic.engineering.service.TrafficEngineeringService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

@Component
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    private final TrafficEngineeringService trafficEngineeringService;
    private final PlotGraph plotGraph;
    private final GraphData graphData;

    private GraphDraw graphDraw = new GraphDraw("Graph");
    private JFrame frame = new JFrame("Intelligent traffic engineering");

    private JTextField x;
    private JTextField y;
    private JTextField label;

    private JTextField first;
    private JTextField second;

    private JTextField source;
    private JTextField destination;

    private JLabel result;

    public ApplicationReadyEventListener(TrafficEngineeringService trafficEngineeringService) {
        this.trafficEngineeringService = trafficEngineeringService;
        this.plotGraph = new PlotGraph();
        this.graphData = new GraphData();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        start();
//        String path1 = trafficEngineeringService.startTrafficEngineering(new Vertex("v1"), new Vertex("v16"));

//        String path1 = trafficEngineeringService.startTrafficEngineering(new Vertex("v1"), new Vertex("v16"));
//        String path2 = trafficEngineeringService.startTrafficEngineering(new Vertex("v3"), new Vertex("v12"));
//
//        System.out.println(path1);
//        System.out.println(path2);
//        plotGraph(Arrays.asList(path1, path2));
//        plotGraph(Arrays.asList(path1));
    }

    private ActionListener addNodeListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            graphDraw.addNode(Integer.parseInt(x.getText()), Integer.parseInt(y.getText()), label.getText());
        }
    };

    private ActionListener addEdgeListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            graphDraw.addEdge(first.getText(), second.getText());
        }
    };

    private ActionListener startCalculationListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String path = trafficEngineeringService.startTrafficEngineering(new Vertex(source.getText()), new Vertex(destination.getText()));
            graphDraw.printPath(path.split(" -> "));
            String[] vertices = path.split(" -> ");
            String res = "<html>";
            for (int i = 0; i < vertices.length; i++) {
                if (i % 5 == 0 && i != 0) {
                    res += "<br/><br/>";
                }
                if (i == vertices.length - 1) {
                    res += vertices[i];
                    res += "<html>";
                } else {
                    res += vertices[i] + " -> ";
                }
            }
            result.setText(res);
        }
    };

    private void setCommandPanel() {
        frame.setLayout(null);

        // node panel
        JPanel setNodePanel = new JPanel(null);
        setNodePanel.setBackground(Color.white);
        setNodePanel.setBounds(10, 10, 280, 150);

        JLabel xLabel = new JLabel("x: ");
        xLabel.setBounds(40, 20, 80, 30);

        x = new JTextField();
        x.setBounds(70, 20, 200, 30);

        JLabel yLabel = new JLabel("y: ");
        yLabel.setBounds(40, 50, 80, 30);

        y = new JTextField();
        y.setBounds(70, 50, 200, 30);

        JLabel labelLabel = new JLabel("Назва вершини:");
        labelLabel.setBounds(10, 80, 200, 30);

        label = new JTextField();
        label.setBounds(120, 80, 150, 30);

        JButton addNodeButton = new JButton("Встановити вершину");
        addNodeButton.setBounds(70, 115, 200, 30);
        addNodeButton.setBackground(Color.blue);
        addNodeButton.addActionListener(addNodeListener);

        // edge panel
        JPanel setEdgePanel = new JPanel(null);
        setEdgePanel.setBackground(Color.white);
        setEdgePanel.setBounds(10, 170, 280, 160);

        JLabel firstLabel = new JLabel("Перша вершина: ");
        firstLabel.setBounds(10, 20, 140, 30);

        first = new JTextField();
        first.setBounds(130, 20, 140, 30);

        JLabel secondLabel = new JLabel("Друга вершина: ");
        secondLabel.setBounds(10, 50, 140, 30);

        second = new JTextField();
        second.setBounds(130, 50, 140, 30);

        JLabel weightLabel = new JLabel("Вага ребра:");
        weightLabel.setBounds(10, 80, 200, 30);

        JTextField weight = new JTextField();
        weight.setBounds(130, 80, 140, 30);

        JButton addEdgeButton = new JButton("Зв'язати вершини");
        addEdgeButton.setBounds(70, 115, 200, 30);
        addEdgeButton.setBackground(Color.blue);
        addEdgeButton.addActionListener(addEdgeListener);

        // set source and destination
        JPanel setPathPanel = new JPanel(null);
        setPathPanel.setBackground(Color.white);
        setPathPanel.setBounds(10, 340, 280, 120);

        JLabel sourceLabel = new JLabel("Стартова вершина: ");
        sourceLabel.setBounds(10, 20, 150, 30);

        source = new JTextField();
        source.setBounds(130, 20, 140, 30);

        JLabel destinationLabel = new JLabel("Кінцева вершина: ");
        destinationLabel.setBounds(10, 50, 150, 30);

        destination = new JTextField();
        destination.setBounds(130, 50, 140, 30);

        JButton startButton = new JButton("Визначити маршрут");
        startButton.setBounds(70, 85, 200, 30);
        startButton.setBackground(Color.blue);
        startButton.addActionListener(startCalculationListener);

        // set result and destination
        JPanel resultPanel = new JPanel(null);
        resultPanel.setBackground(Color.white);
        resultPanel.setBounds(10, 470, 280, 80);

        result = new JLabel();
        result.setBounds(10, 10, 500, 60);

        // set node panel components
        setNodePanel.add(xLabel);
        setNodePanel.add(yLabel);
        setNodePanel.add(labelLabel);

        setNodePanel.add(x);
        setNodePanel.add(y);
        setNodePanel.add(label);

        setNodePanel.add(addNodeButton);

        // set edge panel components
        setEdgePanel.add(firstLabel);
        setEdgePanel.add(secondLabel);
        setEdgePanel.add(weightLabel);

        setEdgePanel.add(first);
        setEdgePanel.add(second);
        setEdgePanel.add(weight);

        setEdgePanel.add(addEdgeButton);

        // set path components
        setPathPanel.add(sourceLabel);
        setPathPanel.add(destinationLabel);

        setPathPanel.add(source);
        setPathPanel.add(destination);

        setPathPanel.add(startButton);

        // set result panel components
        resultPanel.add(result);

        // set panels
        frame.add(setNodePanel);
        frame.add(setEdgePanel);
        frame.add(setPathPanel);
        frame.add(resultPanel);

        frame.pack();

        frame.setSize(300, 600);
        frame.setVisible(true);
    }

    public void start() {
        graphDraw.setSize(600, 600);
        graphDraw.setVisible(true);

        setCommandPanel();

        graphDraw.setSmallGraph();

//        graphDraw.addNode(50, 100, "v1");
//        graphDraw.addNode(50, 200, "v2");
//        graphDraw.addNode(150, 200, "v3");
//        graphDraw.addNode(150, 300, "v7");
//        graphDraw.addNode(650, 300, "v12");
//        graphDraw.addNode(50, 400, "v4");
//        graphDraw.addNode(150, 400, "v6");
//        graphDraw.addNode(600, 400, "v15");
//        graphDraw.addNode(750, 400, "v17");
//        graphDraw.addNode(830, 460, "v18");
//        graphDraw.addNode(750, 500, "v19");
//        graphDraw.addNode(750, 240, "v16");
//        graphDraw.addNode(150, 500, "v5");
//        graphDraw.addNode(400, 500, "v21");
//        graphDraw.addNode(600, 500, "v20");
//        graphDraw.addNode(200, 80, "v8");
//        graphDraw.addNode(350, 75, "v9");
//        graphDraw.addNode(350, 210, "v10");
//        graphDraw.addNode(550, 75, "v13");
//        graphDraw.addNode(550, 250, "v11");
//        graphDraw.addNode(700, 100, "v14");
//
//        graphDraw.addEdge("v1", "v2");
//        graphDraw.addEdge("v1", "v3");
//        graphDraw.addEdge("v2", "v3");
//        graphDraw.addEdge("v2", "v4");
//        graphDraw.addEdge("v3", "v7");
//        graphDraw.addEdge("v7", "v6");
//        graphDraw.addEdge("v6", "v5");
//        graphDraw.addEdge("v4", "v6");
//        graphDraw.addEdge("v4", "v5");
//        graphDraw.addEdge("v1", "v8");
//        graphDraw.addEdge("v8", "v9");
//        graphDraw.addEdge("v9", "v13");
//        graphDraw.addEdge("v13", "v14");
//        graphDraw.addEdge("v8", "v10");
//        graphDraw.addEdge("v7", "v10");
//        graphDraw.addEdge("v9", "v10");
//        graphDraw.addEdge("v13", "v11");
//        graphDraw.addEdge("v10", "v11");
//        graphDraw.addEdge("v11", "v12");
//        graphDraw.addEdge("v12", "v14");
//        graphDraw.addEdge("v14", "v16");
//        graphDraw.addEdge("v16", "v17");
//        graphDraw.addEdge("v17", "v18");
//        graphDraw.addEdge("v17", "v19");
//        graphDraw.addEdge("v19", "v18");
//        graphDraw.addEdge("v15", "v12");
//        graphDraw.addEdge("v15", "v17");
//        graphDraw.addEdge("v15", "v21");
//        graphDraw.addEdge("v21", "v5");
//        graphDraw.addEdge("v21", "v20");
//        graphDraw.addEdge("v20", "v17");
//        graphDraw.addEdge("v20", "v19");
    }

    private void plotGraph(List<String> pathList) {
        plotGraph.setGraphData(graphData.getVerticesAmount(), graphData.getGraphData());
        pathList.forEach(plotGraph::setPathFromSourceToDestination);
        plotGraph.visualizeDirectedGraph();
    }
}
