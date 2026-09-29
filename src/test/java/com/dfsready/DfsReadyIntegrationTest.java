package com.dfsready;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.awt.Graphics2D;
import java.awt.Canvas;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Real Guice injection, RuneLite event dispatch, tracker and overlay in one path. */
public class DfsReadyIntegrationTest
{
    @Test public void injectedPluginRendersAfterEventBusCooldownCompletion() throws Exception
    {
        Client client = mock(Client.class);
        OverlayManager manager = mock(OverlayManager.class);
        ItemContainer equipment = mock(ItemContainer.class);
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        when(client.getItemContainer(InventoryID.WORN)).thenReturn(equipment);
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx()))
            .thenReturn(new Item(ItemID.DRAGONFIRE_SHIELD, 1));
        Canvas canvas = new Canvas();
        canvas.setSize(765, 503);
        when(client.getCanvas()).thenReturn(canvas);
        Injector injector = Guice.createInjector(new AbstractModule()
        {
            @Override protected void configure()
            {
                bind(Client.class).toInstance(client);
                bind(DfsReadyConfig.class).toInstance(new DfsReadyConfig() {});
                bind(OverlayManager.class).toInstance(manager);
                bind(net.runelite.client.audio.AudioPlayer.class).toInstance(mock(net.runelite.client.audio.AudioPlayer.class));
                bind(java.util.concurrent.ScheduledExecutorService.class)
                    .toInstance(mock(java.util.concurrent.ScheduledExecutorService.class));
                bind(DfsReadyOverlay.class).asEagerSingleton();
            }
        });
        DfsReadyPlugin plugin = injector.getInstance(DfsReadyPlugin.class);
        DfsReadyOverlay overlay = injector.getInstance(DfsReadyOverlay.class);
        EventBus bus = new EventBus();
        bus.register(plugin);
        plugin.startUp();
        assertEquals(0, pixel(overlay));
        when(client.getVarbitValue(VarbitID.DRAGONFIRE_SHIELD_RECHARGE)).thenReturn(24);
        bus.post(new GameTick());
        assertEquals(0, pixel(overlay));
        when(client.getVarbitValue(VarbitID.DRAGONFIRE_SHIELD_RECHARGE)).thenReturn(0);
        bus.post(new GameTick());
        assertTrue(pixel(overlay) >>> 24 > 0);
        plugin.shutDown();
        bus.unregister(plugin);
        assertEquals(0, pixel(overlay));
        verify(manager).add(overlay);
        verify(manager).remove(overlay);
    }

    private int pixel(DfsReadyOverlay overlay)
    {
        BufferedImage image = new BufferedImage(765, 503, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        overlay.render(graphics);
        graphics.dispose();
        return image.getRGB(5, 5);
    }
}
