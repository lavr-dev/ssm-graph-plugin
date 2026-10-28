package com.lavr.ssmgraph.support;

import com.lavr.ssmgraph.model.DotEdge;
import com.lavr.ssmgraph.model.DotGraph;
import com.lavr.ssmgraph.model.DotNode;
import com.lavr.ssmgraph.parser.DotToModelParser;
import com.lavr.ssmgraph.parser.ModelToDotParser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateButtonSupport {

    public static String updateDot(String dotContent, String excludedStates, Map<String, String> stateColors) {
        var currDotGraph = DotToModelParser.parse(dotContent);
        var excludedStateSet = normalizeStates(excludedStates);
        var updatedDotGraph = new DotGraph()
                .setName(currDotGraph.getName())
                .setAttributes(currDotGraph.getAttributes())
                .setNodes(getUpdatedNodes(currDotGraph, excludedStateSet, stateColors))
                .setEdges(getUpdatedEdges(currDotGraph, excludedStateSet));
        return ModelToDotParser.parse(updatedDotGraph);
    }

    private static List<DotNode> getUpdatedNodes(DotGraph dotGraph, Set<String> excludedStateSet, Map<String, String> stateColors) {
        return dotGraph.getNodes().stream()
                .filter(node -> isNotExcluded(node.getId(), excludedStateSet))
                .peek(node -> setColor(node.getId(), node.getAttributes(), stateColors))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static List<DotEdge> getUpdatedEdges(DotGraph dotGraph, Set<String> excludedStateSet) {
        return dotGraph.getEdges().stream()
                .filter(edge -> isNotExcluded(edge, excludedStateSet))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static void setColor(String nodeId, Map<String, String> attributes, Map<String, String> stateColors) {
        if (stateColors.isEmpty()) {
            return;
        }
        var color = stateColors.get(nodeId.toLowerCase());
        if (color == null) {
            return;
        }
        attributes.put("style", "filled");
        attributes.put("fillcolor", color);
    }

    private static Set<String> normalizeStates(String rawStates) {
        var states = rawStates.trim();
        if (states.isEmpty()) {
            return Set.of();
        }

        return Arrays.stream(states.split("[,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private static boolean isNotExcluded(DotEdge edge, Set<String> excludedStateSet) {
        return isNotExcluded(edge.getFrom(), excludedStateSet) && isNotExcluded(edge.getTo(), excludedStateSet);
    }

    private static boolean isNotExcluded(String id, Set<String> excludedStateSet) {
        return !excludedStateSet.contains(id.toLowerCase());
    }
}