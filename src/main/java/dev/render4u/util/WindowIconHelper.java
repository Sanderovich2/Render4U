package dev.render4u.util;

import dev.render4u.Constants;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class WindowIconHelper {
    public static final String AVATAR_PATH = "/assets/" + Constants.getModId() + "/textures/avatar.png";
    public static final String[] ICON_PATHS = {
            "/assets/" + Constants.getModId() + "/textures/icons/icon_16.png",
            "/assets/" + Constants.getModId() + "/textures/icons/icon_32.png",
            "/assets/" + Constants.getModId() + "/textures/icons/icon_48.png",
            "/assets/" + Constants.getModId() + "/textures/icons/icon_64.png",
            "/assets/" + Constants.getModId() + "/textures/icons/icon_128.png",
            "/assets/" + Constants.getModId() + "/textures/icons/icon_256.png"
    };

    private WindowIconHelper() {
    }

    public static List<IoSupplier<InputStream>> getCustomIconSuppliers() {
        List<IoSupplier<InputStream>> suppliers = new ArrayList<>(ICON_PATHS.length);
        for (String path : ICON_PATHS) {
            suppliers.add(() -> {
                InputStream is = WindowIconHelper.class.getResourceAsStream(path);
                return is != null ? is : WindowIconHelper.class.getResourceAsStream(AVATAR_PATH);
            });
        }
        return suppliers;
    }
}
