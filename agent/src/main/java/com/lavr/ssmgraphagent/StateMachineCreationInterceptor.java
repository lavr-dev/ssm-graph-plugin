package com.lavr.ssmgraphagent;

import net.bytebuddy.asm.Advice;
import org.springframework.statemachine.StateMachine;

public class StateMachineCreationInterceptor {

    @Advice.OnMethodExit
    public static void onConstruct(@Advice.This StateMachine<?, ?> stateMachine) {
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