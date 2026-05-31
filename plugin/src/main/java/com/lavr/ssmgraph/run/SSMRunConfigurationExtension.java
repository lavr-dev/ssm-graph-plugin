package com.lavr.ssmgraph.run;

import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.lavr.ssmgraph.service.CheckBoxService;
import com.lavr.ssmgraph.service.SocketServerService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SSMRunConfigurationExtension extends RunConfigurationExtension {

    private static final String PLUGIN_ID = "com.lavr.ssm-graph-plugin";
    private static final String AGENT_JAR_RELATIVE_PATH = "lib/agent.jar";

    @Override
    public <T extends RunConfigurationBase<?>> void updateJavaParameters(
            @NotNull T configuration,
            @NotNull JavaParameters params,
            @Nullable RunnerSettings runnerSettings) {
        try {
            Project project = configuration.getProject();
            CheckBoxService checkBoxService = configuration.getProject().getService(CheckBoxService.class);
            if (checkBoxService == null || !checkBoxService.isCatchSsmLaunchEnabled()) {
                return;
            }

            var pluginDescriptor = PluginManager.getInstance().findEnabledPlugin(PluginId.getId(PLUGIN_ID));
            if (pluginDescriptor == null) {
                System.err.println("[SSM Plugin] Plugin descriptor not found for id: " + PLUGIN_ID);
                return;
            }

            SocketServerService server = project.getService(SocketServerService.class);
            server.restartServer();

            String pluginPath = pluginDescriptor
                    .getPluginPath()
                    .resolve(AGENT_JAR_RELATIVE_PATH)
                    .toAbsolutePath()
                    .toString();

            String javaAgentParameter = "-javaagent:" + pluginPath;
            if (!params.getVMParametersList().hasParameter(javaAgentParameter)) {
                params.getVMParametersList().add(javaAgentParameter);
            }

            checkBoxService.setCatchSsmLaunchEnabled(false);

            System.out.println("[SSM Plugin] Added javaagent for run config '" + configuration.getName() + "': " + pluginPath);
        } catch (Exception e) {
            System.err.println("[SSM Plugin] Failed to add agent for run config '" + configuration.getName() + "': " + e.getMessage());
        }
    }

    public boolean isApplicableFor(@NotNull RunConfigurationBase<?> config) {
        CheckBoxService checkBoxService = config.getProject().getService(CheckBoxService.class);
        if (checkBoxService == null || !checkBoxService.isCatchSsmLaunchEnabled()) {
            return false;
        }
        String name = config.getType().getDisplayName().toLowerCase();
        return name.contains("spring") ||
                name.contains("application") ||
                name.contains("junit") ||
                name.contains("gradle");
    }
}