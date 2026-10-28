package com.lavr.ssmgraph.parser;

import com.lavr.ssmgraph.model.DotGraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModelToDotParser {

    public static String parse(DotGraph graph) {
        var dotBuilder = new StringBuilder();
        dotBuilder.append("digraph ")
                .append(graph.getName() == null || graph.getName().isBlank() ? "StateMachine" : graph.getName())
                .append(" {\n");

        dotBuilder.append(" rankdir=TB;\n");

        for (var node : graph.getNodes()) {
            dotBuilder.append(" ").append(node.getId());
            appendAttributes(dotBuilder, node.getAttributes());
            dotBuilder.append(";\n");
        }

        for (var edge : graph.getEdges()) {
            dotBuilder.append(" ").append(edge.getFrom())
                    .append(" -> ")
                    .append(edge.getTo());
            appendAttributes(dotBuilder, edge.getAttributes());
            dotBuilder.append(";\n");
        }

        dotBuilder.append("}\n");
        return dotBuilder.toString();
    }

    private static void appendAttributes(StringBuilder dotBuilder, Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }

        var orderedAttributes = new LinkedHashMap<>(attributes);
        var pairs = new ArrayList<String>();
        for (var entry : orderedAttributes.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue() == null ? "" : entry.getValue().replace("\"", "\\\\\"");
            pairs.add(key + "=\"" + value + "\"");
        }

        dotBuilder.append(" [")
                .append(String.join(", ", pairs))
                .append("]");
    }
}