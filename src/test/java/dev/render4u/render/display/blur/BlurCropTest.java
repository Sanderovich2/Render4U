package dev.render4u.render.display.blur;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlurCropTest {

    @Test
    void regionMergesRectangles() {
        Blur.Region region = new Blur.Region();
        region.merge(10, 10, 20, 20);
        region.merge(30, 5, 50, 25);
        assertEquals(10, region.minX);
        assertEquals(5, region.minY);
        assertEquals(50, region.maxX);
        assertEquals(25, region.maxY);
        assertFalse(region.isEmpty());
    }

    @Test
    void emptyRegionHasNoCrop() {
        assertFalse(Blur.crop(new Blur.Region(), 1, 960, 540));
    }

    @Test
    void cropScalesRegionToLevelAndFlipsY() {
        Blur.Region region = new Blur.Region();
        region.merge(10, 10, 20, 20);

        assertTrue(Blur.crop(region, 1, 960, 540));

        assertEquals(0, Blur.SCISSOR[0]);
        assertEquals(508, Blur.SCISSOR[1]);
        assertEquals(32, Blur.SCISSOR[2]);
        assertEquals(32, Blur.SCISSOR[3]);
    }

    @Test
    void cropAtLevelZeroMatchesScreenRectWithMargin() {
        Blur.Region region = new Blur.Region();
        region.merge(0, 0, 50, 40);

        assertTrue(Blur.crop(region, 0, 1920, 1080));

        assertEquals(0, Blur.SCISSOR[0]);
        assertEquals(988, Blur.SCISSOR[1]);
        assertEquals(112, Blur.SCISSOR[2]);
        assertEquals(92, Blur.SCISSOR[3]);
    }

    @Test
    void cropClampsToTarget() {
        Blur.Region region = new Blur.Region();
        region.merge(5000, 5000, 5010, 5010);

        assertFalse(Blur.crop(region, 1, 960, 540));
    }
}
