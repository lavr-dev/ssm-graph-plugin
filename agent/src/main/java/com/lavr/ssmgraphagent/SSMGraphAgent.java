package com.lavr.ssmgraphagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.statemachine.StateMachine;

import java.lang.instrument.Instrumentation;

public class SSMGraphAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[SSM Graph Agent] Starting...");

        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(ElementMatchers.isSubTypeOf(StateMachine.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder
                                .constructor(ElementMatchers.any())
                                .intercept(Advice.to(StateMachineCreationInterceptor.class))
                )
                .installOn(inst);

        System.out.println("[SSM Graph Agent] Installed interceptor on StateMachine.start()");
    }
}