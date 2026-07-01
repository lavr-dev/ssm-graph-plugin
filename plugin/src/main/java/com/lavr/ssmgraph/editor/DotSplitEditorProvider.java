package com.lavr.ssmgraph.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DotSplitEditorProvider implements FileEditorProvider, DumbAware {

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return "dot".equalsIgnoreCase(file.getExtension());
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        var textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);
        var previewEditor = new DotPreviewEditor(file);
        return new TextEditorWithPreview(textEditor, previewEditor, "DotPreview", TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW);
    }

    @Override
    public @NotNull String getEditorTypeId() {
        return "dot-split-editor";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}