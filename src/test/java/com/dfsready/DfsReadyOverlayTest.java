package com.dfsready;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DfsReadyOverlayTest
{
    private final Client client = mock(Client.class);
    private final AtomicLong clock = new AtomicLong(1_000_000_000L);
    private final DfsReadyConfig config = new DfsReadyConfig() {};
    private DfsReadyOverlay overlay;
    private final Canvas canvas = new Canvas();

    @Before public void setup()
    {
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        canvas.setSize(600, 400);
        when(client.getCanvas()).thenReturn(canvas);
        overlay = new DfsReadyOverlay(client, config, clock::get);
    }

    private BufferedImage render()
    {
        BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        overlay.render(graphics);
        graphics.dispose();
        return image;
    }

    @Test public void flashCoversCanvasAndIsTranslucentRed()
    {
        overlay.flash();
        BufferedImage image = render();
        assertEquals(64, image.getRGB(0, 0) >>> 24);
        assertEquals(64, image.getRGB(599, 399) >>> 24);
        assertEquals(0, image.getRGB(600, 400));
        assertEquals(64, image.getRGB(25, 45) >>> 24);
        assertEquals(255, new Color(image.getRGB(25, 45), true).getRed());
        assertEquals(0, new Color(image.getRGB(25, 45), true).getGreen());
    }

    @Test public void flashBlinksEvery400MillisecondsAndExpires()
    {
        overlay.flash();
        for (int phase = 0; phase < 5; phase++)
        {
            assertEquals(phase % 2 == 0 ? 64 : 0, render().getRGB(25, 45) >>> 24);
            clock.addAndGet(400_000_000L);
        }
        assertEquals(0, render().getRGB(25, 45));
    }

    @Test public void fiveSecondDurationIsNotClampedToThreeSeconds()
    {
        overlay = new DfsReadyOverlay(client, new DfsReadyConfig()
        {
            @Override public int duration() { return 5000; }
        }, clock::get);
        overlay.flash();
        clock.addAndGet(4_800_000_000L);
        assertEquals(64, render().getRGB(25, 45) >>> 24);
        clock.addAndGet(200_000_000L);
        assertEquals(0, render().getRGB(25, 45));
    }

    @Test public void inactiveClearedAndLoggedOutNeverDraw()
    {
        assertEquals(0, render().getRGB(25, 45));
        overlay.flash();
        overlay.clear();
        assertEquals(0, render().getRGB(25, 45));
        overlay.flash();
        when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
        assertEquals(0, render().getRGB(25, 45));
    }

    @Test public void renderingPreservesGraphicsState()
    {
        Graphics2D graphics = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB).createGraphics();
        graphics.setColor(Color.MAGENTA);
        graphics.setClip(0, 0, 600, 400);
        Shape clip = graphics.getClip();
        overlay.flash();
        overlay.render(graphics);
        assertEquals(Color.MAGENTA, graphics.getColor());
        assertEquals(clip.getBounds(), graphics.getClip().getBounds());
        graphics.dispose();
    }

    @Test public void resizeUsesCurrentCanvas()
    {
        overlay.flash();
        canvas.setSize(640, 480);
        assertTrue(render().getRGB(639, 479) >>> 24 > 0);
        canvas.setSize(0, 0);
        assertEquals(0, render().getRGB(25, 45));
    }

    @Test public void customColourOpacityDurationAndNoText()
    {
        DfsReadyConfig custom = new DfsReadyConfig()
        {
            @Override public Color flashColor() { return Color.RED; }
            @Override public int opacity() { return 40; }
            @Override public int duration() { return 200; }
            @Override public boolean showText() { return false; }
        };
        overlay = new DfsReadyOverlay(client, custom, clock::get);
        overlay.flash();
        Color pixel = new Color(render().getRGB(25, 45), true);
        assertEquals(102, pixel.getAlpha());
        assertEquals(255, pixel.getRed());
        assertEquals(0, pixel.getGreen());
        clock.addAndGet(200_000_000L);
        assertEquals(0, render().getRGB(25, 45));
    }

    @Test public void generateVisualEvidence() throws Exception
    {
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(30, 34, 39));
        graphics.fillRect(0, 0, 800, 600);
        graphics.setColor(new Color(70, 77, 66));
        graphics.fillRect(20, 40, 500, 320);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawString("Synthetic game canvas — actual overlay renderer", 20, 25);
        graphics.drawString("Outside game canvas: no flash", 20, 440);
        overlay.flash();
        overlay.render(graphics);
        graphics.dispose();
        File output = new File("build/verification/flash-preview.png");
        output.getParentFile().mkdirs();
        ImageIO.write(image, "png", output);
    }
}
