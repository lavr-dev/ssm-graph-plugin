package com.lavr.ssmgraph.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class DotNode {
    private String id;
    private final Map<String, String> attributes = new LinkedHashMap<>();
}