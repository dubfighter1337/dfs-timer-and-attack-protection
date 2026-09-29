package com.dfsready;

import java.awt.Color;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.function.LongSupplier;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class DfsReadyOverlay extends Overlay
{
    private final Client client;
    private final DfsReadyConfig config;
    private final LongSupplier clock;
    private volatile long start;
    private volatile long duration;
    private volatile String message = "DFS cooldown finished";

    @Inject
    DfsReadyOverlay(Client client, DfsReadyConfig config)
    {
        this(client, config, System::nanoTime);
    }

    DfsReadyOverlay(Client client, DfsReadyConfig config, LongSupplier clock)
    {
        this.client = client;
        this.config = config;
        this.clock = clock;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(PRIORITY_HIGH);
    }

    void flash()
    {
        message = "DFS cooldown finished";
        startFlash();
    }

    void flashEarly()
    {
        message = "DFS cooldown ending soon";
        startFlash();
    }

    private void startFlash()
    {
        start = clock.getAsLong();
        duration = Math.max(200, Math.min(30000, config.duration())) * 1_000_000L;
    }

    void clear() { duration = 0; }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        long length = duration;
        long elapsed = clock.getAsLong() - start;
        if (length == 0 || elapsed < 0 || elapsed >= length || client.getGameState() != GameState.LOGGED_IN)
        {
            return null;
        }
        // Match RuneLite's notification cadence without invoking Notifier, which can
        // also send desktop notifications, play sounds, or request window focus.
        if ((elapsed / 400_000_000L) % 2 != 0) { return null; }
        Canvas canvas = client.getCanvas();
        if (canvas == null) { return null; }
        int x = 0;
        int y = 0;
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        if (width <= 0 || height <= 0) { return null; }

        Graphics2D g = (Graphics2D) graphics.create();
        try
        {
            g.clipRect(x, y, width, height);
            int alpha = (int) Math.round(255 * Math.max(5, Math.min(60, config.opacity())) / 100.0);
            Color color = config.flashColor();
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g.fillRect(x, y, width, height);
            if (config.showText())
            {
                FontMetrics metrics = g.getFontMetrics();
                int textX = x + (width - metrics.stringWidth(message)) / 2;
                int textY = y + height / 3;
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRoundRect(textX - 12, textY - metrics.getAscent() - 8,
                    metrics.stringWidth(message) + 24, metrics.getHeight() + 16, 12, 12);
                g.setColor(Color.WHITE);
                g.drawString(message, textX, textY);
            }
        }
        finally { g.dispose(); }
        return null;
    }
}
