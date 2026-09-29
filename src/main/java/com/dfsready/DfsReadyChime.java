package com.dfsready;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.runelite.client.audio.AudioPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DfsReadyChime
{
    private static final Logger LOG = LoggerFactory.getLogger(DfsReadyChime.class);
    private final AudioPlayer audioPlayer;
    private final ScheduledExecutorService executor;

    @Inject
    DfsReadyChime(AudioPlayer audioPlayer, ScheduledExecutorService executor)
    {
        this.audioPlayer = audioPlayer;
        this.executor = executor;
    }

    void play(int volume)
    {
        int clampedVolume = Math.max(0, Math.min(100, volume));
        if (clampedVolume == 0) { return; }
        float gain = (float) (20 * Math.log10(clampedVolume / 100.0));
        // Opening an audio device must not hold up the client/game thread.
        executor.execute(() ->
        {
            try
            {
                audioPlayer.play(DfsReadyChime.class, "/com/dfsready/soft-chime.wav", gain);
            }
            catch (IOException | UnsupportedAudioFileException | LineUnavailableException | RuntimeException ex)
            {
                // The visual alert remains available even with no usable audio device.
                LOG.debug("Unable to play DFS Ready chime", ex);
            }
        });
    }
}
