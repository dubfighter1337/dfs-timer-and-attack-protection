package com.dfsready;

import com.google.inject.Provides;
import java.awt.TrayIcon;
import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.Notifier;
import net.runelite.client.config.Notification;
import net.runelite.client.config.NotificationSound;
import net.runelite.client.config.RequestFocusType;
import net.runelite.client.config.FlashNotification;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(name = "DFS Timer and Attack Protection", description = "Dragonfire shield cooldown alerts and optional equipment-based Attack menu protection", tags = {"dragonfire", "shield", "cooldown", "flash"})
public class DfsReadyPlugin extends Plugin
{
    private static final Notification DESKTOP_ALERT = new Notification()
        .withEnabled(true).withInitialized(true).withOverride(true)
        .withTray(true).withTrayIconType(TrayIcon.MessageType.INFO)
        .withRequestFocus(RequestFocusType.OFF).withSound(NotificationSound.OFF)
        .withFlash(FlashNotification.DISABLED).withGameMessage(false)
        .withSendWhenFocused(true).withTimeout(5000);

    @Inject private Client client;
    @Inject private DfsReadyConfig config;
    @Inject private OverlayManager overlayManager;
    @Inject private DfsReadyOverlay overlay;
    @Inject private DfsReadyChime chime;
    @Inject private Notifier notifier;
    private final CooldownTracker tracker = new CooldownTracker();

    @Provides
    DfsReadyConfig provideConfig(ConfigManager manager)
    {
        return manager.getConfig(DfsReadyConfig.class);
    }

    @Override
    protected void startUp()
    {
        tracker.reset();
        overlay.clear();
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        overlay.clear();
        tracker.reset();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        GameState state = event.getGameState();
        if (state == GameState.LOGGED_IN) { return; }
        overlay.clear();
        if (state == GameState.LOGIN_SCREEN || state == GameState.LOGIN_SCREEN_AUTHENTICATOR
            || state == GameState.LOGGING_IN || state == GameState.STARTING)
        {
            tracker.reset();
        }
        else
        {
            tracker.suspend();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (client.getGameState() != GameState.LOGGED_IN) { return; }
        boolean equipped = dfsEquipped();
        int cooldown = client.getVarbitValue(VarbitID.DRAGONFIRE_SHIELD_RECHARGE);
        boolean ready = tracker.sample(cooldown, equipped, config.warningOffset());
        if (ready && (!config.equippedOnly() || equipped))
        {
            if (cooldown > 0) { overlay.flashEarly(); }
            else { overlay.flash(); }
            if (config.playChime())
            {
                chime.play(config.chimeVolume());
            }
            if (config.desktopNotification())
            {
                notifier.notify(DESKTOP_ALERT, cooldown > 0
                    ? "DFS cooldown ending soon."
                    : "DFS cooldown finished.");
            }
        }
    }

    private boolean dfsEquipped()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
        Item shield = equipment == null ? null : equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx());
        return shield != null && shield.getId() == ItemID.DRAGONFIRE_SHIELD;
    }

    @Subscribe
    public void onPostMenuSort(PostMenuSort event)
    {
        if (!config.hideAttackWithoutLoadout() || client.getGameState() != GameState.LOGGED_IN
            || client.isMenuOpen() || attackLoadoutEquipped())
        {
            return;
        }
        MenuEntry[] entries = client.getMenuEntries();
        MenuEntry[] filtered = Arrays.stream(entries)
            .filter(entry -> !isAttackEntry(entry))
            .toArray(MenuEntry[]::new);
        if (filtered.length != entries.length)
        {
            client.setMenuEntries(filtered);
        }
    }

    private boolean attackLoadoutEquipped()
    {
        String requiredWeapon = config.attackWeapon().trim();
        String requiredShield = config.attackShield().trim();
        if (requiredWeapon.isEmpty() && requiredShield.isEmpty()) { return true; }
        ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
        if (equipment == null) { return false; }
        Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        Item shield = equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx());
        return (requiredWeapon.isEmpty() || matchesItem(weapon, requiredWeapon))
            && (requiredShield.isEmpty() || matchesItem(shield, requiredShield));
    }

    private boolean matchesItem(Item item, String required)
    {
        if (item == null) { return false; }
        String value = required.trim();
        if (value.matches("[0-9]+"))
        {
            return Integer.toString(item.getId()).equals(value);
        }
        ItemComposition definition = client.getItemDefinition(item.getId());
        return definition != null && value.equalsIgnoreCase(Text.removeTags(definition.getName()));
    }

    private static boolean isAttackEntry(MenuEntry entry)
    {
        if (!"Attack".equalsIgnoreCase(Text.removeTags(entry.getOption()))) { return false; }
        MenuAction type = entry.getType();
        return type == MenuAction.NPC_FIRST_OPTION || type == MenuAction.NPC_SECOND_OPTION
            || type == MenuAction.NPC_THIRD_OPTION || type == MenuAction.NPC_FOURTH_OPTION
            || type == MenuAction.NPC_FIFTH_OPTION || type == MenuAction.PLAYER_FIRST_OPTION
            || type == MenuAction.PLAYER_SECOND_OPTION || type == MenuAction.PLAYER_THIRD_OPTION
            || type == MenuAction.PLAYER_FOURTH_OPTION || type == MenuAction.PLAYER_FIFTH_OPTION
            || type == MenuAction.PLAYER_SIXTH_OPTION || type == MenuAction.PLAYER_SEVENTH_OPTION
            || type == MenuAction.PLAYER_EIGHTH_OPTION;
    }
}
