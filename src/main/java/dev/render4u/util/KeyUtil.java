package dev.render4u.util;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.Locale;

public final class KeyUtil {

    private KeyUtil() {
    }

    public static String getKeyName(int key) {
        if (key == 0 || key == InputConstants.UNKNOWN.getValue()) {
            return "NONE";
        }

        try {
            InputConstants.Key k = InputConstants.Type.KEYBOARD.getOrCreate(key);
            String name = k.getName();
            if (name != null) {
                if (name.startsWith("key.keyboard.")) {
                    name = name.substring("key.keyboard.".length());
                }

                return switch (name) {
                    case "right.shift" -> "RSHIFT";
                    case "left.shift" -> "LSHIFT";
                    case "right.control" -> "RCTRL";
                    case "left.control" -> "LCTRL";
                    case "right.alt" -> "RALT";
                    case "left.alt" -> "LALT";
                    case "right.win", "right.gui" -> "RWIN";
                    case "left.win", "left.gui" -> "LWIN";
                    case "caps.lock" -> "CAPS";
                    case "scroll.lock" -> "SCRLK";
                    case "num.lock" -> "NUMLK";
                    case "print.screen" -> "PRTSC";
                    case "page.up" -> "PGUP";
                    case "page.down" -> "PGDN";
                    case "space" -> "SPACE";
                    case "enter" -> "ENTER";
                    case "escape" -> "ESC";
                    case "backspace" -> "BACK";
                    case "delete" -> "DEL";
                    case "insert" -> "INS";
                    case "home" -> "HOME";
                    case "end" -> "END";
                    case "tab" -> "TAB";
                    case "up" -> "UP";
                    case "down" -> "DOWN";
                    case "left" -> "LEFT";
                    case "right" -> "RIGHT";
                    default -> name.replace(".", "").toUpperCase(Locale.ROOT);
                };
            }
        } catch (Exception ignored) {
        }

        return "K" + key;
    }

    public static boolean isKeyMatch(int targetKey, int eventKey, int eventScancode) {
        if (targetKey <= 0 || targetKey == InputConstants.UNKNOWN.getValue()) return false;
        if (targetKey == eventKey || targetKey == eventScancode) return true;
        return (targetKey == 344 || targetKey == 229) && (eventKey == 344 || eventScancode == 229)
            || (targetKey == 340 || targetKey == 225) && (eventKey == 340 || eventScancode == 225);
    }
}
