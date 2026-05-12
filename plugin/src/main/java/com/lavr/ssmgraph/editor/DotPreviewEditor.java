package com.lavr.ssmgraph.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.utils.JBCefStreamResourceHandler;
import com.intellij.util.Alarm;
import org.cef.CefApp;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefLoadHandlerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class DotPreviewEditor extends UserDataHolderBase implements FileEditor {

    private static final String SCHEME_NAME = "http";
    private static final String DOMAIN_NAME = "dot-preview";
    private static final String URL = SCHEME_NAME + "://" + DOMAIN_NAME;
    private static final int DEBOUNCE_DELAY_MS = 300;

    private final JBCefBrowser browser;
    private final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);
    private final VirtualFile file;

    public DotPreviewEditor(VirtualFile file) {
        this.browser = new JBCefBrowser();
        this.file = file;

        var document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
            document.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull DocumentEvent event) {
                    alarm.cancelAllRequests();
                    alarm.addRequest(() -> updatePreview(file), DEBOUNCE_DELAY_MS);
                }
            }, this);
        }

        if (JBCefApp.isSupported()) {
            CefApp.getInstance().registerSchemeHandlerFactory(SCHEME_NAME, DOMAIN_NAME, getCefSchemeHandlerFactory());

            browser.getJBCefClient().addLoadHandler(new CefLoadHandlerAdapter() {
                @Override
                public void onLoadEnd(CefBrowser cefBrowser, CefFrame frame, int httpStatusCode) {
                    if (frame.isMain()) {
                        ApplicationManager.getApplication().invokeLater(() -> {
//                            browser.openDevtools();
                            updatePreview(file);
                        });
                    }
                }
            }, browser.getCefBrowser());

            browser.loadURL(URL + "/index.html");
        }
    }

    public void refreshPreview() {
        updatePreview(file);
    }

    private void updatePreview(VirtualFile file) {
        var encoded = ReadAction.compute(() -> {
            var document = FileDocumentManager.getInstance().getDocument(file);
            if (document == null) {
                return null;
            }

            var dotContent = document.getText();
            return Base64.getEncoder().encodeToString(dotContent.getBytes(StandardCharsets.UTF_8));
        });

        if (encoded == null) {
            return;
        }

        browser.getCefBrowser().executeJavaScript("updateGraph('" + encoded + "')", browser.getCefBrowser().getURL(), 0);
    }

    private CefSchemeHandlerFactory getCefSchemeHandlerFactory() {
        return (browser, frame, schemeName, request) -> {
            var path = request.getURL().replace(URL, "");
            var stream = DotPreviewEditor.class.getResourceAsStream("/static" + path);

            var headers = Map.of(
                    "Cross-Origin-Opener-Policy", "same-origin",
                    "Cross-Origin-Embedder-Policy", "require-corp"
            );

            return new JBCefStreamResourceHandler(stream, "text/html", this, headers);
        };
    }

    @Override
    public @NotNull JComponent getComponent() {
        return browser.getComponent();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return browser.getComponent();
    }

    @Override
    public @NotNull String getName() {
        return "Preview";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void dispose() {
        Disposer.dispose(browser);
        Disposer.dispose(alarm);
    }
}