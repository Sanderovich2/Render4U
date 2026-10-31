package dev.render4u;

import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.util.KeyUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyNameTest {
    @Test
    void testFormatKeys() {
        assertEquals("RSHIFT", KeyUtil.getKeyName(InputConstants.KEY_RSHIFT));
        assertEquals("LSHIFT", KeyUtil.getKeyName(InputConstants.KEY_LSHIFT));
        assertEquals("LCTRL", KeyUtil.getKeyName(InputConstants.KEY_LCONTROL));
        assertEquals("RCTRL", KeyUtil.getKeyName(InputConstants.KEY_RCONTROL));
        assertEquals("SPACE", KeyUtil.getKeyName(InputConstants.KEY_SPACE));
        assertEquals("ENTER", KeyUtil.getKeyName(InputConstants.KEY_RETURN));
        assertEquals("V", KeyUtil.getKeyName(InputConstants.KEY_V));
        assertEquals("UP", KeyUtil.getKeyName(InputConstants.KEY_UP));
        assertEquals("ESC", KeyUtil.getKeyName(InputConstants.KEY_ESCAPE));
        assertEquals("NONE", KeyUtil.getKeyName(InputConstants.UNKNOWN.getValue()));
        assertEquals("NONE", KeyUtil.getKeyName(0));
    }
}
