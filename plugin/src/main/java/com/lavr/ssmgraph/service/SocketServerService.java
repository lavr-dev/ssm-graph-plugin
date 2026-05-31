package com.lavr.ssmgraph.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.text.CaseUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service(Service.Level.PROJECT)
public final class SocketServerService implements Disposable {
    private static final Logger LOG = Logger.getInstance(SocketServerService.class);
    private static final int PORT = 4321;
    private static final String OUTPUT_DIR_NAME = "ssmGraph";
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    private final Project project;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public SocketServerService(Project project) {
        this.project = project;
    }

    @Override
    public void dispose() {
        stopServer();
    }

    public synchronized void restartServer() {
        stopServer();
        startServer();
    }

    private void startServer() {
        running = true;

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);

                LOG.info("[SSM Plugin] Socket server started on port " + PORT);
                System.out.println("[SSM Plugin] Socket server started on port " + PORT);

                while (running) {
                    try (Socket client = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
                            sb.append(line).append("\n");
                        }
                        String fullData = sb.toString().trim();

                        if (!fullData.isEmpty()) {
                            handleData(fullData);
                        }
                    } catch (Exception e) {
                        if (running) {
                            LOG.warn("Receive error", e);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warn("Server stopped", e);
            }
        });
    }

    private void handleData(String json) {
        LOG.info("[SSM Plugin] Received: " + json);
        System.out.println("[SSM Plugin] Received: ");

        String dotData = replaceProjectName(json, project);
        Path outputDir = resolveProjectRoot(project).resolve(OUTPUT_DIR_NAME);
        String baseFileName = LocalDateTime.now().format(FILE_NAME_FORMATTER);

        try {
            Files.createDirectories(outputDir);
            Path outputFile = resolveUniqueOutputFile(outputDir, baseFileName);
            Files.writeString(outputFile, dotData, StandardCharsets.UTF_8);
            LOG.info("[SSM Plugin] DOT file saved to: " + outputFile.toAbsolutePath());
            System.out.println("[SSM Plugin] DOT file saved to: " + outputFile.toAbsolutePath());
            openGeneratedFile(project, outputFile);
        } catch (Exception e) {
            LOG.warn("[SSM Plugin] Failed to save DOT file in directory: " + outputDir.toAbsolutePath(), e);
        }
    }

    private Path resolveUniqueOutputFile(Path outputDir, String baseFileName) {
        Path outputFile = outputDir.resolve(baseFileName + ".dot");
        int suffix = 1;

        while (Files.exists(outputFile)) {
            outputFile = outputDir.resolve(baseFileName + "_" + suffix + ".dot");
            suffix++;
        }

        return outputFile;
    }

    private String replaceProjectName(String dotData, Project project) {
        if (dotData == null || dotData.isBlank()) {
            return dotData;
        }
        return dotData.replaceFirst("StateMachine", CaseUtils.toCamelCase(project.getName().trim(), false, '-'));
    }

    private Path resolveProjectRoot(Project project) {
        if (project != null && project.getBasePath() != null) {
            return Path.of(project.getBasePath());
        }

        return Path.of("").toAbsolutePath();
    }

    private void openGeneratedFile(Project project, Path outputFile) {
        if (project == null || project.isDisposed()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(outputFile);
            if (file == null) {
                LOG.warn("[SSM Plugin] Failed to open DOT file in editor: " + outputFile.toAbsolutePath());
                return;
            }

            FileEditorManager.getInstance(project).openFile(file, true);
        });
    }

    private synchronized void stopServer() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception ignored) {
        }
    }
}