package com.valentyna.intelligent.traffic.engineering.service;

import com.valentyna.intelligent.traffic.engineering.graph.Vertex;

public interface RoutingAnalysisService {

    void performRoutingDataAnalysis(Vertex source, Vertex destination);

    int getTotalOperationAmount();
}
