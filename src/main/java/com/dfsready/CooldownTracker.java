package com.dfsready;

/** Tracks server cooldown samples and estimates early warnings in game ticks. */
final class CooldownTracker
{
    private int previous = -1;
    private boolean tracking;
    private boolean warned;
    private int ticksSinceChange;

    boolean sample(int cooldown, boolean dfsEquipped)
    {
        return sample(cooldown, dfsEquipped, 0);
    }

    boolean sample(int cooldown, boolean dfsEquipped, int offsetSeconds)
    {
        int leadMillis = Math.max(0, Math.min(10, offsetSeconds)) * 1000;
        if (cooldown < 0)
        {
            suspend();
            return false;
        }
        if (previous < 0)
        {
            // Establish a baseline after login/hop/reconnect. Never alert on it.
            tracking = cooldown > 0 && (tracking || dfsEquipped);
            ticksSinceChange = 0;
            // Do not issue a catch-up warning when resuming inside the early window.
            warned = cooldown > 0 && (warned || cooldown * 4800 <= leadMillis);
            previous = cooldown;
            return false;
        }
        if (cooldown > previous)
        {
            // Shared draconic cooldown: only attribute a fresh cycle to an equipped DFS.
            tracking = dfsEquipped;
            warned = false;
        }
        ticksSinceChange = cooldown == previous ? Math.min(7, ticksSinceChange + 1) : 0;
        // The server value is quantized in eight-tick units. Never infer actual
        // readiness before it reaches zero, even if a sample is held unusually long.
        int remainingTicks = cooldown == 0 ? 0 : Math.max(1, cooldown * 8 - ticksSinceChange);
        boolean ready = tracking && !warned && previous > 0
            && (cooldown == 0 || (leadMillis > 0 && remainingTicks * 600 <= leadMillis));
        if (ready) { warned = true; }
        if (cooldown == 0)
        {
            tracking = false;
        }
        previous = cooldown;
        return ready;
    }

    void suspend()
    {
        previous = -1;
    }

    void reset()
    {
        previous = -1;
        tracking = false;
        warned = false;
        ticksSinceChange = 0;
    }
}
