package dev.render4u.core.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {

    @FunctionalInterface
    public interface EventInvoker {
        void invoke(Event event) throws Throwable;
    }

    private record Data(Object source, EventInvoker invoker, byte priority) {
    }

    private final Map<Class<? extends Event>, List<Data>> registry = new ConcurrentHashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventTarget.class) && method.getParameterCount() == 1) {
                Class<?> paramType = method.getParameterTypes()[0];
                if (Event.class.isAssignableFrom(paramType)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) paramType;
                    EventTarget annotation = method.getAnnotation(EventTarget.class);
                    method.setAccessible(true);
                    try {
                        MethodHandle handle = lookup.unreflect(method);
                        EventInvoker invoker = event -> handle.invoke(listener, event);
                        registry.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                                .add(new Data(listener, invoker, annotation.value()));
                        sort(eventClass);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
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
        if (list != null && !list.isEmpty()) {
            for (int i = 0, size = list.size(); i < size; i++) {
                try {
                    list.get(i).invoker.invoke(event);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
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

