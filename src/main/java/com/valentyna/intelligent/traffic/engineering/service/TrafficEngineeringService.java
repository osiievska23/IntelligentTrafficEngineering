package com.valentyna.intelligent.traffic.engineering.service;

import com.valentyna.intelligent.traffic.engineering.graph.Vertex;

public interface TrafficEngineeringService {

    void getPathFromSourceToDestination(Vertex source, Vertex destination);
}
