package com.lavr.ssmgraph.parser;

import com.lavr.ssmgraph.model.DotEdge;
import com.lavr.ssmgraph.model.DotGraph;
import com.lavr.ssmgraph.model.DotNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class DotToModelParser {

    private static final Pattern GRAPH_PATTERN = Pattern.compile("^\\s*(digraph)\\s+([A-Za-z0-9_.:-]+)\\s*\\{?");

    private static final Pattern NODE_PATTERN =
            Pattern.compile("^\\s*\\\"?([A-Za-z0-9_.:-]+)\\\"?\\s*(\\[(.*)])?\\s*;?\\s*$");

    private static final Pattern EDGE_PATTERN =
            Pattern.compile("^\\s*\\\"?([A-Za-z0-9_.:-]+)\\\"?\\s*->\\s*\\\"?([A-Za-z0-9_.:-]+)\\\"?\\s*(\\[(.*)])?\\s*;?\\s*$");

    private static final Pattern ATTR_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*\"?([^\",\\]]+)\"?");

    public static DotGraph parse(String dotContent) {
        DotGraph graph = new DotGraph();

        for (String line : dotContent.split("\\R")) {
            line = line.trim();

            if (line.isEmpty() || line.equals("}")) {
                continue;
            }

            var graphMatcher = GRAPH_PATTERN.matcher(line);
            if (graphMatcher.find()) {
                graph.setName(graphMatcher.group(2));
                continue;
            }

            var nodeMatcher = NODE_PATTERN.matcher(line);
            if (nodeMatcher.find()) {
                var nodeId = nodeMatcher.group(1);
                var node = new DotNode(nodeId);

                var attrGroup = nodeMatcher.group(3);
                if (attrGroup != null) {
                    node.getAttributes().putAll(parseAttributes(attrGroup));
                }

                graph.getNodes().add(node);
                continue;
            }

            var edgeMatcher = EDGE_PATTERN.matcher(line);
            if (edgeMatcher.find()) {
                var from = edgeMatcher.group(1);
                var to = edgeMatcher.group(2);

                var edge = new DotEdge(from, to);

                var attrGroup = edgeMatcher.group(4);
                if (attrGroup != null) {
                    edge.getAttributes().putAll(parseAttributes(attrGroup));
                }

                graph.getEdges().add(edge);
            }
        }

        return graph;
    }

    private static Map<String, String> parseAttributes(String raw) {
        var attrs = new LinkedHashMap<String, String>();
        var matcher = ATTR_PATTERN.matcher(raw);

        while (matcher.find()) {
            var key = matcher.group(1);
            var value = matcher.group(2);
            attrs.put(key, value);
        }

        return attrs;
    }
}
