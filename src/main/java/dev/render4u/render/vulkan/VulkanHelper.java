package dev.render4u.render.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VulkanHelper {
    public static final String VULKAN_BACKEND = "Vulkan";
    private static final Logger LOGGER = LoggerFactory.getLogger("Render4UVulkan");

    private static volatile Boolean vulkanActive;
    private static volatile String cachedBackendName;
    private static volatile String cachedDeviceInfo;

    private VulkanHelper() {
    }

    public static boolean isVulkan() {
        Boolean active = vulkanActive;
        if (active != null) {
            return active;
        }
        try {
            GpuDevice device = RenderSystem.getDevice();
            boolean res = isVulkan(device);
            vulkanActive = res;
            return res;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isVulkan(GpuDevice device) {
        if (device == null || device.getDeviceInfo() == null) {
            return false;
        }
        return VULKAN_BACKEND.equalsIgnoreCase(device.getDeviceInfo().backendName());
    }

    public static String getBackendName() {
        String name = cachedBackendName;
        if (name != null) {
            return name;
        }
        try {
            GpuDevice device = RenderSystem.getDevice();
            if (device != null && device.getDeviceInfo() != null) {
                name = device.getDeviceInfo().backendName();
                cachedBackendName = name;
                return name;
            }
        } catch (Throwable ignored) {
        }
        return "Unknown";
    }

    public static String getDeviceInfo() {
        String info = cachedDeviceInfo;
        if (info != null) {
            return info;
        }
        try {
            GpuDevice device = RenderSystem.getDevice();
            if (device != null && device.getDeviceInfo() != null) {
                info = device.getDeviceInfo().name() + " (" + getBackendName() + ")";
                cachedDeviceInfo = info;
                return info;
            }
        } catch (Throwable ignored) {
        }
        return "Unknown Device";
    }

    public static void submitIfVulkan(GpuDevice device) {
        if (isVulkan(device)) {
            device.createCommandEncoder().submit();
        }
    }
}

