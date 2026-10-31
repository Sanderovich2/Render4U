package dev.render4u.render.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Render3DCoordinateTest {

    @Test
    public void testScreenBox() {
        Render3D.ScreenBox box = new Render3D.ScreenBox(10.0F, 20.0F, 50.0F, 80.0F);
        assertEquals(40.0F, box.width());
        assertEquals(60.0F, box.height());
    }

    @Test
    public void testProjectBoxCalculationsWithoutMinecraft() {
        assertNull(Render3D.project(100.0, 50.0, 100.0));
        assertNull(Render3D.projectBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    }

    @Test
    public void testDecoupledRender3DDoesNotReferenceEntityClasses() {
        for (java.lang.reflect.Method m : Render3D.class.getDeclaredMethods()) {
            for (Class<?> param : m.getParameterTypes()) {
                assertNotEquals("net.minecraft.world.entity.Entity", param.getName(),
                        "Render3D should not depend on Entity: " + m.getName());
                assertNotEquals("net.minecraft.core.BlockPos", param.getName(),
                        "Render3D should not depend on BlockPos: " + m.getName());
            }
        }
    }

    @Test
    public void testEntityRenderUtilExists() {
        assertNotNull(EntityRenderUtil.class);
    }
}
