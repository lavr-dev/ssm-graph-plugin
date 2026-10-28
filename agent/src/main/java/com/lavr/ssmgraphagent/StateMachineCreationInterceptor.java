package com.lavr.ssmgraphagent;

import net.bytebuddy.asm.Advice;
import org.springframework.statemachine.StateMachine;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class StateMachineCreationInterceptor {

    private static final Set<StateMachine<?, ?>> CAPTURED = Collections.newSetFromMap(new WeakHashMap<>());

    @Advice.OnMethodExit(inline = false)
    public static void onConstruct(@Advice.This StateMachine<?, ?> stateMachine) {
        synchronized (CAPTURED) {
            if (!CAPTURED.add(stateMachine)) {
                return;
            }
        }

        String graphData = StateMachineConverter.generate(stateMachine);
        System.out.println("[SSM Graph Agent] Captured StateMachine: " + stateMachine.getClass().getName());
        StateMachineDataSender.send(graphData);
    }
}