package dev.render4u.feature.module;

import dev.render4u.feature.module.impl.DebugHudModule;
import dev.render4u.feature.module.impl.RenderSelfTestModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        register(new RenderSelfTestModule());
        register(new DebugHudModule());
    }

    public void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (m.getClass() == clazz) {
                return (T) m;
            }
        }
        return null;
    }
}
