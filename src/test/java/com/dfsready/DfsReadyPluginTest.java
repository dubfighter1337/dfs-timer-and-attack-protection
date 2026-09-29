package com.dfsready;

import java.lang.reflect.Field;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class DfsReadyPluginTest
{
    private final Client client = mock(Client.class);
    private final DfsReadyConfig config = mock(DfsReadyConfig.class);
    private final OverlayManager manager = mock(OverlayManager.class);
    private final DfsReadyOverlay overlay = mock(DfsReadyOverlay.class);
    private final DfsReadyChime chime = mock(DfsReadyChime.class);
    private final ItemContainer equipment = mock(ItemContainer.class);
    private DfsReadyPlugin plugin;

    @Before public void setup() throws Exception
    {
        plugin = new DfsReadyPlugin();
        inject("client", client);
        inject("config", config);
        inject("overlayManager", manager);
        inject("overlay", overlay);
        inject("chime", chime);
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        when(client.getItemContainer(InventoryID.WORN)).thenReturn(equipment);
        when(config.attackWeapon()).thenReturn("Bronze crossbow");
        when(config.attackShield()).thenReturn("Dragonfire shield");
        itemName(net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BRONZE, "Bronze crossbow");
        itemName(net.runelite.api.gameval.ItemID.DRAGONFIRE_SHIELD, "Dragonfire shield");
        itemName(net.runelite.api.gameval.ItemID.DRAGONFIRE_SHIELD_UNCHARGED, "Dragonfire shield");
        equipDfs();
        plugin.startUp();
    }

    private void inject(String name, Object value) throws Exception
    {
        Field field = DfsReadyPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(plugin, value);
    }

    private void itemName(int id, String name)
    {
        ItemComposition definition = mock(ItemComposition.class);
        when(definition.getName()).thenReturn(name);
        when(client.getItemDefinition(id)).thenReturn(definition);
    }

    @Test public void shieldOnlyAllowsAnyWeaponOrNoWeapon()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        when(config.attackWeapon()).thenReturn(" ");
        plugin.onPostMenuSort(new PostMenuSort());
        when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(new Item(4151, 1));
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, never()).getMenuEntries();
    }

    @Test public void weaponOnlyAllowsAnyShieldOrNoShield()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        when(config.attackShield()).thenReturn("");
        bronzeCrossbow();
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx())).thenReturn(null);
        plugin.onPostMenuSort(new PostMenuSort());
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx())).thenReturn(new Item(1540, 1));
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, never()).getMenuEntries();
    }

    @Test public void bothBlankDisableRestrictionEvenWithMissingEquipment()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        when(config.attackWeapon()).thenReturn("");
        when(config.attackShield()).thenReturn(" ");
        when(client.getItemContainer(InventoryID.WORN)).thenReturn(null);
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, never()).getMenuEntries();
    }

    @Test public void customNamesAreCaseInsensitiveAndTrimmed()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        when(config.attackWeapon()).thenReturn("  ABYSSAL whip ");
        when(config.attackShield()).thenReturn("");
        itemName(4151, "Abyssal whip");
        when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(new Item(4151, 1));
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, never()).getMenuEntries();
    }

    @Test public void exactIdsCanSelectSpecificVariants()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        when(config.attackWeapon()).thenReturn("9174");
        when(config.attackShield()).thenReturn("11283");
        bronzeCrossbow();
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, never()).getMenuEntries();
    }

    @Test public void wrongShieldInShieldOnlyModeStillHidesAttack()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        when(config.attackWeapon()).thenReturn("");
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx())).thenReturn(null);
        doReturn(new MenuEntry[] {menuEntry("Attack", MenuAction.NPC_SECOND_OPTION)}).when(client).getMenuEntries();
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client).setMenuEntries(new MenuEntry[0]);
    }

    @Test public void earlyWarningUsesEarlyTextAndChimeOnlyOnce()
    {
        when(config.warningOffset()).thenReturn(5);
        when(config.playChime()).thenReturn(true);
        when(config.chimeVolume()).thenReturn(35);
        tick(0);
        tick(24);
        tick(2);
        tick(1);
        tick(1);
        tick(0);
        verify(overlay).flashEarly();
        verify(overlay, never()).flash();
        verify(chime).play(35);
    }

    private void equipDfs()
    {
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx()))
            .thenReturn(new Item(net.runelite.api.gameval.ItemID.DRAGONFIRE_SHIELD, 1));
    }

    private void tick(int value)
    {
        when(client.getVarbitValue(VarbitID.DRAGONFIRE_SHIELD_RECHARGE)).thenReturn(value);
        plugin.onGameTick(new GameTick());
    }

    private void state(GameState state)
    {
        when(client.getGameState()).thenReturn(state);
        GameStateChanged event = new GameStateChanged();
        event.setGameState(state);
        plugin.onGameStateChanged(event);
    }

    private MenuEntry menuEntry(String option, MenuAction type)
    {
        MenuEntry entry = mock(MenuEntry.class);
        when(entry.getOption()).thenReturn(option);
        when(entry.getType()).thenReturn(type);
        return entry;
    }

    @Test public void chimeDefaultsOff()
    {
        assertFalse(new DfsReadyConfig() {}.playChime());
        tick(24);
        tick(0);
        verify(overlay).flash();
        verifyNoInteractions(chime);
    }

    @Test public void chimeFiresWithFlashOncePerCycle()
    {
        when(config.playChime()).thenReturn(true);
        when(config.chimeVolume()).thenReturn(35);
        tick(0);
        for (int cycle = 0; cycle < 3; cycle++)
        {
            tick(24);
            tick(1);
            tick(0);
            tick(0);
        }
        org.mockito.InOrder alerts = inOrder(overlay, chime);
        for (int cycle = 0; cycle < 3; cycle++)
        {
            alerts.verify(overlay).flash();
            alerts.verify(chime).play(35);
        }
        verify(chime, times(3)).play(35);
        verify(overlay, times(3)).flash();
    }

    @Test public void suppressedAlertAlsoSuppressesChime()
    {
        when(config.playChime()).thenReturn(true);
        when(config.equippedOnly()).thenReturn(true);
        tick(24);
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx())).thenReturn(null);
        tick(0);
        verifyNoInteractions(chime);
        verify(overlay, never()).flash();
    }

    private void bronzeCrossbow()
    {
        when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()))
            .thenReturn(new Item(net.runelite.api.gameval.ItemID.XBOWS_CROSSBOW_BRONZE, 1));
    }

    @Test public void attackFilterDefaultsOffAndLeavesMenusUntouched()
    {
        assertFalse(new DfsReadyConfig() {}.hideAttackWithoutLoadout());
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, never()).setMenuEntries(any());
        verify(client, never()).getMenuEntries();
    }

    @Test public void wrongWeaponHidesOnlyNpcAndPlayerAttacks()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(new Item(4151, 1));
        MenuEntry walk = menuEntry("Walk here", MenuAction.WALK);
        MenuEntry npc = menuEntry("<col=ff0000>Attack</col>", MenuAction.NPC_SECOND_OPTION);
        MenuEntry player = menuEntry("Attack", MenuAction.PLAYER_FIRST_OPTION);
        MenuEntry activate = menuEntry("Activate", MenuAction.CC_OP);
        MenuEntry examine = menuEntry("Examine", MenuAction.EXAMINE_NPC);
        MenuEntry widget = menuEntry("Attack", MenuAction.CC_OP);
        when(client.getMenuEntries()).thenReturn(new MenuEntry[] {walk, npc, player, activate, examine, widget});
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client).setMenuEntries(new MenuEntry[] {walk, activate, examine, widget});
    }

    @Test public void bronzeCrossbowAndEitherDfsVariantAllowAttacks()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        bronzeCrossbow();
        plugin.onPostMenuSort(new PostMenuSort());
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx()))
            .thenReturn(new Item(net.runelite.api.gameval.ItemID.DRAGONFIRE_SHIELD_UNCHARGED, 1));
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, never()).getMenuEntries();
        verify(client, never()).setMenuEntries(any());
    }

    @Test public void crossbowWithoutDfsHidesAttacks()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        bronzeCrossbow();
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx())).thenReturn(null);
        doReturn(new MenuEntry[] {menuEntry("Attack", MenuAction.NPC_SECOND_OPTION)}).when(client).getMenuEntries();
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client).setMenuEntries(new MenuEntry[0]);
    }

    @Test public void unarmedOrMissingEquipmentHidesAttacks()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        doReturn(new MenuEntry[] {menuEntry("Attack", MenuAction.NPC_SECOND_OPTION)}).when(client).getMenuEntries();
        plugin.onPostMenuSort(new PostMenuSort());
        when(client.getItemContainer(InventoryID.WORN)).thenReturn(null);
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, times(2)).setMenuEntries(new MenuEntry[0]);
    }

    @Test public void openMenuAndLoggedOutStateAreNotRewritten()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        when(client.isMenuOpen()).thenReturn(true);
        plugin.onPostMenuSort(new PostMenuSort());
        when(client.isMenuOpen()).thenReturn(false);
        when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, never()).getMenuEntries();
    }

    @Test public void switchingEquipmentOrDisablingOptionRestoresNormalNextMenu()
    {
        when(config.hideAttackWithoutLoadout()).thenReturn(true);
        doReturn(new MenuEntry[] {menuEntry("Attack", MenuAction.NPC_SECOND_OPTION)}).when(client).getMenuEntries();
        plugin.onPostMenuSort(new PostMenuSort());
        bronzeCrossbow();
        plugin.onPostMenuSort(new PostMenuSort());
        when(equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx())).thenReturn(null);
        when(config.hideAttackWithoutLoadout()).thenReturn(false);
        plugin.onPostMenuSort(new PostMenuSort());
        verify(client, times(1)).setMenuEntries(new MenuEntry[0]);
    }

    @Test public void threeCyclesCallFlashThreeTimes()
    {
        tick(0);
        for (int cycle = 0; cycle < 3; cycle++)
        {
            for (int value = 24; value >= 0; value--)
            {
                for (int repeated = 0; repeated < 8; repeated++) { tick(value); }
            }
        }
        verify(overlay, times(3)).flash();
    }

    @Test public void flashStillFiresAfterUnequipping()
    {
        tick(24);
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx())).thenReturn(null);
        tick(0);
        verify(overlay).flash();
    }

    @Test public void equippedOnlySuppressesWithoutDeferringAlert()
    {
        when(config.equippedOnly()).thenReturn(true);
        tick(24);
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx())).thenReturn(null);
        tick(0);
        equipDfs();
        tick(0);
        verify(overlay, never()).flash();
    }

    @Test public void loginAndHopZeroesNeverFlash()
    {
        tick(24);
        state(GameState.HOPPING);
        tick(0);
        state(GameState.LOGGED_IN);
        tick(0);
        tick(12);
        state(GameState.LOGIN_SCREEN);
        state(GameState.LOGGED_IN);
        tick(0);
        verify(overlay, never()).flash();
    }

    @Test public void activeCooldownSurvivesHop()
    {
        tick(24);
        state(GameState.HOPPING);
        state(GameState.LOGGED_IN);
        tick(12);
        tick(0);
        verify(overlay).flash();
    }

    @Test public void missingEquipmentCannotArm()
    {
        when(client.getItemContainer(InventoryID.WORN)).thenReturn(null);
        tick(24);
        tick(0);
        verify(overlay, never()).flash();
    }

    @Test public void unchargedShieldCannotArm()
    {
        when(equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx()))
            .thenReturn(new Item(net.runelite.api.gameval.ItemID.DRAGONFIRE_SHIELD_UNCHARGED, 1));
        tick(24);
        tick(0);
        verify(overlay, never()).flash();
    }

    @Test public void disableAndReenableCleansUp()
    {
        tick(24);
        plugin.shutDown();
        verify(manager).remove(overlay);
        plugin.startUp();
        tick(0);
        verify(manager, times(2)).add(overlay);
        verify(overlay, times(3)).clear();
        verify(overlay, never()).flash();
    }
}
