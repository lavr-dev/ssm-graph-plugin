package com.lavr.ssmgraph.support;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ColorTableSupport {

    public static void addState(String rawState, DefaultTableModel model) {
        if (rawState == null || rawState.isEmpty() || hasState(model, rawState)) {
            return;
        }
        model.addRow(new Object[]{rawState, "White"});
    }

    public static void removeSelectedRows(JTable table, DefaultTableModel model) {
        var selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            return;
        }

        Arrays.stream(table.getSelectedRows())
                .boxed()
                .sorted(Comparator.reverseOrder())
                .forEach(model::removeRow);
    }

    public static Map<String, String> getStateColors(DefaultTableModel model) {
        var stateColors = new LinkedHashMap<String, String>();
        model.getDataVector().forEach(row -> stateColors.put(row.get(0).toString().toLowerCase(), row.get(1).toString()));
        return stateColors;
    }

    private static boolean hasState(DefaultTableModel model, String state) {
        return model.getDataVector().stream()
                .anyMatch(row -> state.equalsIgnoreCase(row.get(0).toString().trim()));
    }
}