package com.dfsready;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(DfsReadyConfig.GROUP)
public interface DfsReadyConfig extends Config
{
    String GROUP = "dfsready";

    @ConfigSection(name = "DFS Timer", description = "Cooldown warnings, screen flash and optional sound", position = 0)
    String TIMER_SECTION = "dfsTimer";

    @ConfigSection(name = "Attack Protection", description = "Optional equipment requirements for Attack menu entries", position = 1)
    String ATTACK_SECTION = "attackProtection";

    @ConfigItem(keyName = "flashColor", name = "Flash colour", description = "Colour of the cooldown-finished flash", position = 0, section = TIMER_SECTION)
    default Color flashColor() { return Color.RED; }

    @Range(min = 5, max = 60)
    @ConfigItem(keyName = "opacity", name = "Opacity (%)", description = "Maximum opacity of the translucent flash", position = 1, section = TIMER_SECTION)
    default int opacity() { return 25; }

    @Range(min = 200, max = 30000)
    @ConfigItem(keyName = "duration", name = "Duration (ms)", description = "Total flashing time in milliseconds: 2000 = 2 seconds, 5000 = 5 seconds. Maximum 30000 (30 seconds).", position = 2, section = TIMER_SECTION)
    default int duration() { return 2000; }

    @ConfigItem(keyName = "showText", name = "Show message", description = "Show cooldown finished or ending soon for early warnings; charges are not checked", position = 3, section = TIMER_SECTION)
    default boolean showText() { return false; }

    @ConfigItem(keyName = "equippedOnly", name = "Only alert while equipped", description = "Suppress the alert if the DFS is no longer equipped when the warning is due", position = 4, section = TIMER_SECTION)
    default boolean equippedOnly() { return false; }

    @ConfigItem(keyName = "hideAttackWithoutLoadout", name = "Enable attack protection", description = "Hide Attack unless all filled equipment fields match. Blank fields are ignored. Does not stop combat already in progress.", position = 5, section = ATTACK_SECTION)
    default boolean hideAttackWithoutLoadout() { return false; }

    @ConfigItem(keyName = "playChime", name = "Play soft chime", description = "Play one gentle chime when the red flash starts. No Windows notification sound.", position = 6, section = TIMER_SECTION)
    default boolean playChime() { return false; }

    @Range(min = 0, max = 100)
    @ConfigItem(keyName = "chimeVolume", name = "Chime volume (%)", description = "Volume of the soft alert chime; 0 mutes it", position = 7, section = TIMER_SECTION)
    default int chimeVolume() { return 35; }

    @ConfigItem(keyName = "attackWeapon", name = "Allowed weapon", description = "Exact item name or item ID. Blank ignores this slot; both slots blank disable the restriction.", position = 8, section = ATTACK_SECTION)
    default String attackWeapon() { return "Bronze crossbow"; }

    @ConfigItem(keyName = "attackShield", name = "Allowed shield", description = "Exact item name or item ID. Blank ignores this slot; both slots blank disable the restriction.", position = 9, section = ATTACK_SECTION)
    default String attackShield() { return "Dragonfire shield"; }

    @Range(min = 0, max = 10)
    @ConfigItem(keyName = "warningOffset", name = "Warn early (seconds)", description = "Warn approximately 0-10 seconds before cooldown ends. 0 waits for confirmed readiness. One alert per cooldown.", position = 10, section = TIMER_SECTION)
    default int warningOffset() { return 0; }

    @ConfigItem(keyName = "desktopNotification", name = "Windows / desktop notification", description = "Show one desktop notification when the cooldown alert fires. Off by default. Delivery depends on system notification settings.", position = 11, section = TIMER_SECTION)
    default boolean desktopNotification() { return false; }
}
