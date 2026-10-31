package dev.render4u;

import dev.render4u.util.WindowIconHelper;
import net.minecraft.server.packs.resources.IoSupplier;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WindowIconTest {

    @Test
    void testAvatarResourceExists() throws Exception {
        try (InputStream stream = WindowIconHelper.class.getResourceAsStream(WindowIconHelper.AVATAR_PATH)) {
            assertNotNull(stream, "Avatar resource should exist at " + WindowIconHelper.AVATAR_PATH);
            assertTrue(stream.available() > 0, "Avatar stream should not be empty");
        }
    }

    @Test
    void testLogoResourceExists() throws Exception {
        try (InputStream stream = WindowIconHelper.class.getResourceAsStream("/assets/render4u/textures/logo.png")) {
            assertNotNull(stream, "Logo resource should exist at /assets/render4u/textures/logo.png");
            assertTrue(stream.available() > 0, "Logo stream should not be empty");
        }
    }

    @Test
    void testLogoTtfAndMsdfResourcesExist() throws Exception {
        try (InputStream stream = WindowIconTest.class.getResourceAsStream("/assets/render4u/fonts/ttf/render4u-logo.ttf")) {
            assertNotNull(stream, "render4u-logo.ttf should exist");
            assertTrue(stream.available() > 0);
        }
        try (InputStream stream = WindowIconTest.class.getResourceAsStream("/assets/render4u/fonts/msdf/logo.json")) {
            assertNotNull(stream, "logo.json MSDF descriptor should exist");
            assertTrue(stream.available() > 0);
        }
        try (InputStream stream = WindowIconTest.class.getResourceAsStream("/assets/render4u/fonts/msdf/logo.png")) {
            assertNotNull(stream, "logo.png MSDF atlas should exist");
            assertTrue(stream.available() > 0);
        }
    }

    @Test
    void testCustomIconSuppliers() throws Exception {
        List<IoSupplier<InputStream>> suppliers = WindowIconHelper.getCustomIconSuppliers();
        assertNotNull(suppliers);
        assertFalse(suppliers.isEmpty(), "Suppliers list should not be empty");
        assertEquals(WindowIconHelper.ICON_PATHS.length, suppliers.size());

        for (IoSupplier<InputStream> supplier : suppliers) {
            try (InputStream is = supplier.get()) {
                assertNotNull(is, "Icon supplier should return non-null stream");
                assertTrue(is.available() > 0, "Icon stream should contain bytes");
            }
        }
    }
}
