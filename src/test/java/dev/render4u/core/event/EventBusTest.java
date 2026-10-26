package dev.render4u.core.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventBusTest {

    private static class TestEvent extends Event {
    }

    private static class Listener {
        final List<String> calls = new ArrayList<>();

        @EventTarget
        public void onEvent(TestEvent event) {
            calls.add("listener");
        }
    }

    private static class BaseListener {
        final List<String> calls = new ArrayList<>();

        @EventTarget
        public void onEvent(TestEvent event) {
            calls.add("base");
        }
    }

    private static class OverridingListener extends BaseListener {
        @Override
        @EventTarget
        public void onEvent(TestEvent event) {
            calls.add("child");
        }
    }

    private static class InheritingListener extends BaseListener {
    }

    private static class HighPriorityListener {
        private final List<String> order;

        HighPriorityListener(List<String> order) {
            this.order = order;
        }

        @EventTarget(Priority.HIGH)
        public void onEvent(TestEvent event) {
            order.add("high");
        }
    }

    private static class LowPriorityListener {
        private final List<String> order;

        LowPriorityListener(List<String> order) {
            this.order = order;
        }

        @EventTarget(Priority.LOW)
        public void onEvent(TestEvent event) {
            order.add("low");
        }
    }

    private static class BrokenListener {
        @EventTarget
        public void onEvent(TestEvent event) {
            throw new IllegalStateException("boom");
        }
    }

    private static class InvalidListener {
        final List<String> calls = new ArrayList<>();

        @EventTarget
        public void noArgs() {
            calls.add("noArgs");
        }

        @EventTarget
        public void notAnEvent(String text) {
            calls.add("notAnEvent");
        }
    }

    @Test
    void dispatchesToRegisteredListener() {
        EventBus bus = new EventBus();
        Listener listener = new Listener();
        bus.register(listener);

        TestEvent event = bus.post(new TestEvent());

        assertEquals(List.of("listener"), listener.calls);
        assertNotNull(event);
    }

    @Test
    void postReturnsSameEventInstance() {
        EventBus bus = new EventBus();
        TestEvent event = new TestEvent();
        assertEquals(event, bus.post(event));
    }

    @Test
    void unregisterStopsDispatch() {
        EventBus bus = new EventBus();
        Listener listener = new Listener();
        bus.register(listener);
        bus.unregister(listener);

        bus.post(new TestEvent());

        assertEquals(List.of(), listener.calls);
    }

    @Test
    void overriddenHandlerFiresOnce() {
        EventBus bus = new EventBus();
        OverridingListener listener = new OverridingListener();
        bus.register(listener);
        bus.post(new TestEvent());

        assertEquals(List.of("child"), listener.calls);
    }

    @Test
    void inheritedHandlerFromSuperclassIsFound() {
        EventBus bus = new EventBus();
        InheritingListener listener = new InheritingListener();
        bus.register(listener);
        bus.post(new TestEvent());

        assertEquals(List.of("base"), listener.calls);
    }

    @Test
    void higherPriorityRunsFirst() {
        EventBus bus = new EventBus();
        List<String> order = new ArrayList<>();
        bus.register(new LowPriorityListener(order));
        bus.register(new HighPriorityListener(order));
        bus.post(new TestEvent());

        assertEquals(List.of("high", "low"), order);
    }

    @Test
    void throwingHandlerDoesNotBreakOthers() {
        EventBus bus = new EventBus();
        Listener listener = new Listener();
        bus.register(new BrokenListener());
        bus.register(listener);
        bus.post(new TestEvent());

        assertEquals(List.of("listener"), listener.calls);
    }

    @Test
    void methodsWithoutSingleEventParameterAreIgnored() {
        EventBus bus = new EventBus();
        InvalidListener listener = new InvalidListener();
        bus.register(listener);
        bus.post(new TestEvent());

        assertEquals(List.of(), listener.calls);
    }
}
