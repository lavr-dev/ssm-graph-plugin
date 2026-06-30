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
        try {
            //            System.out.println("[SSM Agent] Captured StateMachine: " + graphData);
            System.out.println("[SSM Agent] Captured StateMachine: ");
            Thread.sleep(100);
            StateMachineDataSender.send(graphData);
        } catch (Exception e) {
            System.err.println("[SSM Agent] Error capturing state machine: " + e);
        }
    }
}