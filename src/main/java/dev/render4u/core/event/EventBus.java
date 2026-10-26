package dev.render4u.core.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger("Render4U/EventBus");

    @FunctionalInterface
    public interface EventInvoker {
        void invoke(Event event) throws Throwable;
    }

    private record Data(Object source, EventInvoker invoker, byte priority) {
    }

    private final Map<Class<? extends Event>, List<Data>> registry = new ConcurrentHashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public void register(Object listener) {
        Set<String> seen = new HashSet<>();
        for (Class<?> type = listener.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(EventTarget.class) || method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> paramType = method.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(paramType)) {
                    continue;
                }
                if (!seen.add(method.getName() + ':' + paramType.getName())) {
                    continue;
                }
                method.setAccessible(true);
                try {
                    MethodHandle handle = lookup.unreflect(method);
                    EventInvoker invoker = event -> handle.invoke(listener, event);
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) paramType;
                    registry.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                            .add(new Data(listener, invoker, method.getAnnotation(EventTarget.class).value()));
                    sort(eventClass);
                } catch (IllegalAccessException e) {
                    LOGGER.error("Failed to register handler {}#{}", type.getName(), method.getName(), e);
                }
            }
        }
    }

    public void unregister(Object listener) {
        for (List<Data> list : registry.values()) {
            list.removeIf(data -> data.source.equals(listener));
        }
    }

    public <T extends Event> T post(T event) {
        List<Data> list = registry.get(event.getClass());
        if (list == null) {
            return event;
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            try {
                list.get(i).invoker.invoke(event);
            } catch (Throwable t) {
                LOGGER.error("Error dispatching {}", event.getClass().getSimpleName(), t);
            }
        }
        return event;
    }

    private void sort(Class<? extends Event> eventClass) {
        List<Data> list = registry.get(eventClass);
        if (list != null) {
            list.sort(Comparator.comparingInt(d -> d.priority));
        }
    }
}
