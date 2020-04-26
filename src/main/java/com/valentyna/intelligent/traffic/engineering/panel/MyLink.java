package com.valentyna.intelligent.traffic.engineering.panel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class MyLink {

    private double weight;
    private MyNode[] vertexes;

    public Set<String> getVertexes() {
        return Arrays.stream(vertexes).map(MyNode::getId).collect(Collectors.toSet());
    }
}
