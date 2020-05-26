package com.valentyna.intelligent.traffic.engineering.service;

public interface LoadingAnalysisService {

    void performLoadingDataAnalysis();

    double calculateLoadCriterion(String path);

    int getTotalOperationAmount();
}
