package com.valentyna.intelligent.traffic.engineering.service.impl;

import com.valentyna.intelligent.traffic.engineering.domen.RoutingPathData;
import com.valentyna.intelligent.traffic.engineering.graph.Graph;
import com.valentyna.intelligent.traffic.engineering.panel.GraphData;
import com.valentyna.intelligent.traffic.engineering.repository.RoutingPathDataRepository;
import com.valentyna.intelligent.traffic.engineering.service.GraphBuilderService;
import com.valentyna.intelligent.traffic.engineering.service.LoadingAnalysisService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LoadingAnalysisServiceImpl implements LoadingAnalysisService {

    private final RoutingPathDataRepository routingPathDataRepository;

    private Map<String, Double> chanelLoadings = new HashMap<>();

    private Graph graph;

    private int totalOperationsAmount;

    public LoadingAnalysisServiceImpl(RoutingPathDataRepository routingPathDataRepository, GraphBuilderService graphBuilderService) {
        this.routingPathDataRepository = routingPathDataRepository;
        graph = graphBuilderService.buildGraph();
    }

    @Override
    public void performLoadingDataAnalysis() {
        chanelLoadings = new HashMap<>();
        calculateChanelLoading();
        List<RoutingPathData> routingPathData = routingPathDataRepository.findAll();
        for (RoutingPathData data : routingPathData) {
            double pathLoading = calculateLoadCriterion(data.getPath());
            data.setLoading(pathLoading);
            routingPathDataRepository.save(data);
        }
    }

    @Override
    public double calculateLoadCriterion(String path) {
        List<String> channels = getChannels(path);

        List<Double> channelLoading = channels.stream().map(this::getChannelLoading).collect(Collectors.toList());
        List<Double> channelWeight = channels.stream().map(c -> graph.getEdgesWeights().get(c)).collect(Collectors.toList());
        double pathWeight = channelWeight.stream().mapToDouble(Double::doubleValue).sum();

        double averageChanelLoading = IntStream.range(0, channels.size())
                .mapToDouble(i -> (channelLoading.get(i) * channelWeight.get(i)) / pathWeight)
                .sum() / channels.size();

        double criteria = IntStream.range(0, channels.size())
                .mapToDouble(i -> (channelWeight.get(i) * Math.pow(channelLoading.get(i) - averageChanelLoading, 2)) / pathWeight)
                .sum() + averageChanelLoading;

        totalOperationsAmount += channelLoading.size() * 16 * 16 * 32 * 16 * 16 * 64 * 16 * 32 * 16;
        return criteria;
    }

    @Override
    public int getTotalOperationAmount() {
        int totalOperationsAmount = this.totalOperationsAmount;
        this.totalOperationsAmount = 0;
        return totalOperationsAmount;
    }

    private void calculateChanelLoading() {
        routingPathDataRepository.findAll().stream()
                .map(data -> data.getPath().split(" -> "))
                .forEach(path -> IntStream.range(0, path.length - 1)
                        .forEach(i -> updateChanelLoadingData(path[i], path[i + 1])));
    }

    private Double getChannelLoading(String channel) {
        increase();
        return chanelLoadings.get(channel) != null ? chanelLoadings.get(channel)
                : chanelLoadings.get(channel.split("-")[1] + "-" + channel.split("-")[0]);
    }

    private List<String> getChannels(String path) {
        increase();
        String[] vertices = path.split(" -> ");
        List<String> channels = new ArrayList<>();
        for (int i = 0; i < vertices.length - 1; i++) {
            channels.add(vertices[i] + "-" + vertices[i + 1]);
        }
        return channels;
    }

    private void updateChanelLoadingData(String v1, String v2) {
        increase();
        String key = v2 + "-" + v1;

        if (chanelLoadings.containsKey(key)) {
            chanelLoadings.put(key, chanelLoadings.get(key) + 1.0);
        } else {
            key = v1 + "-" + v2;
            if (chanelLoadings.containsKey(key)) {
                chanelLoadings.put(key, chanelLoadings.get(key) + 1.0);
            } else {
                chanelLoadings.put(key, 1.0);
            }
        }
    }

    void increase() {
        totalOperationsAmount++;
    }
}
