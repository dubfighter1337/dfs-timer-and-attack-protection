package com.dfsready;

import org.junit.Test;
import static org.junit.Assert.*;

public class CooldownTrackerTest
{
    @Test public void everyOffsetFromZeroToTenFiresOnceInEachOfThreeCycles()
    {
        for (int offset = 0; offset <= 10; offset++)
        {
            CooldownTracker tracker = new CooldownTracker();
            tracker.sample(0, true, offset);
            for (int cycle = 0; cycle < 3; cycle++)
            {
                int alerts = 0;
                for (int tick = 0; tick <= 192; tick++)
                {
                    int value = tick == 192 ? 0 : 24 - tick / 8;
                    if (tracker.sample(value, true, offset))
                    {
                        alerts++;
                        int lead = (192 - tick) * 600;
                        assertTrue("Too early at offset " + offset, lead <= offset * 1000);
                        assertTrue("Too late at offset " + offset, offset * 1000 - lead < 600);
                    }
                }
                assertEquals("Offset " + offset, 1, alerts);
                assertFalse(tracker.sample(0, true, offset));
            }
        }
    }

    @Test public void earlyWarningDoesNotRepeatAfterHopOrOffsetChange()
    {
        CooldownTracker tracker = new CooldownTracker();
        tracker.sample(24, true, 10);
        assertTrue(tracker.sample(2, true, 10));
        tracker.suspend();
        assertFalse(tracker.sample(1, true, 0));
        assertFalse(tracker.sample(0, true, 0));
    }

    @Test public void resumingInsideWarningWindowDoesNotCatchUp()
    {
        CooldownTracker tracker = new CooldownTracker();
        assertFalse(tracker.sample(1, true, 5));
        assertFalse(tracker.sample(1, true, 5));
        assertFalse(tracker.sample(0, true, 5));
    }

    @Test public void staleSampleCannotCountDownBeyondItsEightTickBlock()
    {
        CooldownTracker tracker = new CooldownTracker();
        tracker.sample(0, true, 10);
        tracker.sample(24, true, 10);
        for (int i = 0; i < 300; i++) { assertFalse(tracker.sample(24, true, 10)); }
    }

    @Test public void zeroOffsetAlwaysRequiresServerZero()
    {
        CooldownTracker tracker = new CooldownTracker();
        tracker.sample(1, true, 0);
        for (int i = 0; i < 50; i++) { assertFalse(tracker.sample(1, true, 0)); }
        assertTrue(tracker.sample(0, true, 0));
    }

    @Test public void threeFullCyclesWithRepeatedSamplesAlertExactlyOnceEach()
    {
        CooldownTracker tracker = new CooldownTracker();
        assertFalse(tracker.sample(0, true));
        int alerts = 0;
        for (int cycle = 0; cycle < 3; cycle++)
        {
            for (int value = 24; value >= 0; value--)
            {
                for (int tick = 0; tick < 8; tick++)
                {
                    if (tracker.sample(value, true)) { alerts++; }
                }
            }
            assertEquals(cycle + 1, alerts);
        }
    }

    @Test public void idleAndFailedActivationNeverAlert()
    {
        CooldownTracker tracker = new CooldownTracker();
        for (int i = 0; i < 400; i++) { assertFalse(tracker.sample(0, true)); }
    }

    @Test public void resumesExistingCooldownWithoutImmediateAlert()
    {
        CooldownTracker tracker = new CooldownTracker();
        assertFalse(tracker.sample(8, true));
        assertFalse(tracker.sample(1, true));
        assertTrue(tracker.sample(0, true));
        assertFalse(tracker.sample(0, true));
    }

    @Test public void otherShieldsDoNotArm()
    {
        CooldownTracker tracker = new CooldownTracker();
        assertFalse(tracker.sample(0, false));
        assertFalse(tracker.sample(24, false));
        assertFalse(tracker.sample(12, true));
        assertFalse(tracker.sample(0, true));
    }

    @Test public void unequippingDoesNotLoseTrackedCycle()
    {
        CooldownTracker tracker = new CooldownTracker();
        tracker.sample(24, true);
        tracker.sample(1, false);
        assertTrue(tracker.sample(0, false));
    }

    @Test public void hopPreservesActiveTrackingEvenWhenUnequipped()
    {
        CooldownTracker tracker = new CooldownTracker();
        tracker.sample(24, true);
        tracker.suspend();
        assertFalse(tracker.sample(12, false));
        assertTrue(tracker.sample(0, false));
    }

    @Test public void zeroBaselineAfterHopDoesNotFlash()
    {
        CooldownTracker tracker = new CooldownTracker();
        tracker.sample(1, true);
        tracker.suspend();
        assertFalse(tracker.sample(0, true));
        assertFalse(tracker.sample(0, true));
    }

    @Test public void logoutOrDisableClearsAttribution()
    {
        CooldownTracker tracker = new CooldownTracker();
        tracker.sample(24, true);
        tracker.reset();
        assertFalse(tracker.sample(0, true));
        tracker.sample(12, false);
        assertFalse(tracker.sample(0, false));
    }

    @Test public void newNonDfsCycleClearsAttribution()
    {
        CooldownTracker tracker = new CooldownTracker();
        tracker.sample(12, true);
        tracker.sample(24, false);
        assertFalse(tracker.sample(0, true));
    }

    @Test public void invalidSampleCannotTriggerReadiness()
    {
        CooldownTracker tracker = new CooldownTracker();
        tracker.sample(1, true);
        assertFalse(tracker.sample(-1, true));
        assertFalse(tracker.sample(0, true));
    }
}
