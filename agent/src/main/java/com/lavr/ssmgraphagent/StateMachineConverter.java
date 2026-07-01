package com.lavr.ssmgraphagent;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.ChoicePseudoState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.trigger.Trigger;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StateMachineConverter {

    public static <S, E> String generate(StateMachine<S, E> stateMachine) {
        StringBuilder gvBuilder = new StringBuilder();
        gvBuilder.append("digraph StateMachine {\n");
        gvBuilder.append(" rankdir=TB;\n");
        generateStates(stateMachine, gvBuilder);
        generateTransitions(stateMachine, gvBuilder);
        generateChoiceTransitions(stateMachine, gvBuilder);
        gvBuilder.append("}\n");
        return gvBuilder.toString();
    }

    private static <S, E> void generateStates(StateMachine<S, E> stateMachine, StringBuilder gvBuilder) {
        Set<State<S, E>> states = new HashSet<>();
        Set<String> stateIds = new HashSet<>();
        stateMachine.getStates()
                .stream()
                .filter(state -> !states.contains(state))
                .filter(state -> state.getId() != null)
                .filter(state -> stateIds.add(state.getId().toString()))
                .forEach(state -> {
                    states.add(state);
                    String stateId = state.getId().toString();
                    boolean isChoice = state.getPseudoState() != null && state.getPseudoState().getKind() == PseudoStateKind.CHOICE;
                    String shape = isChoice ? "diamond" : "ellipse";
                    gvBuilder.append(String.format(" %s [shape=%s];\n", stateId, shape));
                });
    }

    private static <S, E> void generateTransitions(StateMachine<S, E> stateMachine, StringBuilder gvBuilder) {
        stateMachine.getTransitions().forEach(transition -> {
            String event = Optional.ofNullable(transition.getTrigger())
                    .map(Trigger::getEvent)
                    .map(Object::toString)
                    .orElse(null);
            String guard = Optional.ofNullable(transition.getGuard())
                    .map(StateMachineConverter::getClassName)
                    .orElse(null);
            String actions = Optional.ofNullable(transition.getActions())
                    .filter(acts -> !acts.isEmpty())
                    .map(StateMachineConverter::getActionClassNames)
                    .orElse(null);
            buildString(gvBuilder, transition.getSource().getId().toString(), transition.getTarget().getId().toString(), event, guard, actions);
        });
    }

    private static void buildString(StringBuilder gvBuilder, String source, String target, String event, String guard, String actions) {
        gvBuilder.append(String.format(" %s -> %s", source, target));
        if (event != null || guard != null || actions != null) {
            gvBuilder.append(" [label=\"");
            gvBuilder.append(event == null ? "" : String.format("%s\\n", event));
            gvBuilder.append(guard == null ? "" : String.format("%s\\n", guard));
            gvBuilder.append(actions == null ? "" : String.format("%s\\n", actions));
            gvBuilder.append("\"];");
        }
        gvBuilder.append("\n");
    }

    private static <S, E> void generateChoiceTransitions(StateMachine<S, E> stateMachine, StringBuilder gvBuilder) {
        Set<String> sourceTargetSet = new HashSet<>();
        stateMachine.getStates().stream()
                .filter(s -> s.getPseudoState() != null && s.getPseudoState().getKind() == PseudoStateKind.CHOICE)
                .forEach(choiceState -> {
                    PseudoState<S, E> choicePseudoState = choiceState.getPseudoState();
                    List<ChoicePseudoState.ChoiceStateData<S, E>> choices;
                    try {
                        Field choicesField = ChoicePseudoState.class.getDeclaredField("choices");
                        choicesField.setAccessible(true);
                        choices = (List<ChoicePseudoState.ChoiceStateData<S, E>>) choicesField.get(choicePseudoState);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    choices.forEach(choiceData -> {
                        State<S, E> targetState = choiceData.getState();
                        if (targetState != null) {
                            if (!sourceTargetSet.contains(choiceState.getId().toString() + targetState.getId().toString())) {
                                sourceTargetSet.add(choiceState.getId().toString() + targetState.getId().toString());
                                S target = targetState.getId();
                                String guard = Optional.ofNullable(choiceData.getGuard())
                                        .map(grd -> cleanClassName(grd.getClass().getSimpleName()))
                                        .orElse(null);
                                String actions = Optional.ofNullable(choiceData.getActions())
                                        .filter(acts -> !acts.isEmpty())
                                        .map(StateMachineConverter::getActionClassNames)
                                        .orElse(null);
                                buildString(gvBuilder, choiceState.getId().toString(), target.toString(), null, guard, actions);
                            }
                        }
                    });
                });
    }

    private static <S, E> String getActionClassNames(Collection<Function<StateContext<S, E>, Mono<Void>>> actions) {
        return actions.stream()
                .map(StateMachineConverter::getClassName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

    private static String getClassName(Object function) {
        if (function == null) {
            return null;
        }
        Class<?> functionClass = function.getClass();

        Field[] fields = functionClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object inner;
            try {
                inner = field.get(function);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (inner != null && !inner.getClass().isPrimitive()
                    && !inner.getClass().getName().startsWith("java.")) {
                return cleanClassName(inner.getClass().getSimpleName());
            }
        }

        return null;
    }

    private static String cleanClassName(String className) {
        if (className.contains("$$Lambda$")) {
            return null;
//            int lambdaIndex = className.indexOf("$$Lambda$");
//            return className.substring(0, lambdaIndex + "$$Lambda$".length());
        }
        if (className.contains("$MockitoMock$")) {
            return className.substring(0, className.indexOf("$MockitoMock$"));
        }
        return className;
    }
}