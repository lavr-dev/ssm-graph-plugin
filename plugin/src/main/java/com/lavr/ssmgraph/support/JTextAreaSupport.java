package com.lavr.ssmgraph.support;

import javax.swing.*;

public class JTextAreaSupport {

    public static void addStateToSelection(String state, JTextArea selectedStatesArea) {
        if (state.isEmpty()) {
            return;
        }

        var current = selectedStatesArea.getText().trim();
        selectedStatesArea.setText(current.isEmpty() ? state : current + "," + state);
    }
}