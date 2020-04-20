package com.valentyna.vanet.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControllerRoutingTable {

    private int id;
    private List<RoutingPathData> routingPathInfo;
    private boolean empty;

    public void addNewRoutingData(List<RoutingPathData> newInformation) {
        routingPathInfo.addAll(newInformation);
    }
}
