package com.lavr.ssmgraph.support;

import com.intellij.openapi.vfs.VirtualFile;
import com.lavr.ssmgraph.parser.DotToModelParser;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

public class ComboBoxSupport {
    public static void updateStatesFromDotFile(VirtualFile file,
                                               DefaultComboBoxModel<String> statesComboBoxModel,
                                               JComboBox<String> exclusionsStatesComboBox,
                                               JComboBox<String> colorStatesComboBox)
            throws IOException {
        var dotContent = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);
        var dotGraph = DotToModelParser.parse(dotContent);

        statesComboBoxModel.removeAllElements();
        dotGraph.getNodes().stream()
                .map(state -> state.getId())
                .sorted(Comparator.naturalOrder())
                .forEach(statesComboBoxModel::addElement);

        if (statesComboBoxModel.getSize() > 0) {
            exclusionsStatesComboBox.setSelectedIndex(0);
            colorStatesComboBox.setSelectedIndex(0);
        }
    }
}