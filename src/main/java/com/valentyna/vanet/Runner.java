package com.valentyna.vanet;

import com.valentyna.vanet.service.GraphService;

public class Runner {

    public static void main(String[] args) {
        GraphService graphService = new GraphService();
        graphService.run();
    }
}
