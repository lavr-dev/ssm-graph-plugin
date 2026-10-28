package com.lavr.ssmgraph.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class DotGraph {

    private String name;
    private Map<String, String> attributes = new LinkedHashMap<>();
    private List<DotNode> nodes = new ArrayList<>();
    private List<DotEdge> edges = new ArrayList<>();
}