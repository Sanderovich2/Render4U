package dev.render4u.module;

import dev.render4u.module.impl.movement.AutoSprint;
import dev.render4u.module.impl.render.ClickGuiModule;
import dev.render4u.module.impl.render.HUDModule;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ModuleManager {
    private static final List<Module> MODULES = new CopyOnWriteArrayList<>();
    private static final Map<Class<? extends Module>, Module> MODULE_MAP = new HashMap<>();

    public void init() {
        MODULES.clear();
        MODULE_MAP.clear();

        register(new AutoSprint());
        register(new ClickGuiModule());
        register(new HUDModule());
    }

    private void register(Module module) {
        MODULES.add(module);
        MODULE_MAP.put(module.getClass(), module);
    }

    public static List<Module> getModules() {
        return Collections.unmodifiableList(MODULES);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T getModule(Class<T> clazz) {
        return (T) MODULE_MAP.get(clazz);
    }

    public static Module getByName(String name) {
        for (Module module : MODULES) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public static List<Module> getByCategory(Category category) {
        List<Module> list = new ArrayList<>();
        for (Module module : MODULES) {
            if (module.getCategory() == category) {
                list.add(module);
            }
        }
        return list;
    }
}
