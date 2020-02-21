package com.valentyna.vanet.routing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingTable {

    private int controllerId;
    private List<RoutingPathData> routingPathData;

}
