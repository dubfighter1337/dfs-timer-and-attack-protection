package com.dfsready;

import java.io.BufferedInputStream;
import java.util.concurrent.ScheduledExecutorService;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import net.runelite.client.audio.AudioPlayer;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DfsReadyChimeTest
{
    @Test public void zeroVolumeDoesNotOpenAudioDevice()
    {
        AudioPlayer player = mock(AudioPlayer.class);
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        DfsReadyChime chime = new DfsReadyChime(player, executor);
        chime.play(0);
        chime.play(-10);
        verifyNoInteractions(player, executor);
    }

    @Test public void playsBundledChimeAtRequestedGainOffClientThread() throws Exception
    {
        AudioPlayer player = mock(AudioPlayer.class);
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        new DfsReadyChime(player, executor).play(35);
        org.mockito.ArgumentCaptor<Runnable> task = org.mockito.ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(task.capture());
        verifyNoInteractions(player);
        task.getValue().run();
        verify(player).play(eq(DfsReadyChime.class), eq("/com/dfsready/soft-chime.wav"),
            eq((float) (20 * Math.log10(0.35))));
    }

    @Test public void missingAudioDeviceDoesNotBreakAlert() throws Exception
    {
        AudioPlayer player = mock(AudioPlayer.class);
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        doAnswer(invocation -> { ((Runnable) invocation.getArgument(0)).run(); return null; })
            .when(executor).execute(any(Runnable.class));
        doThrow(new LineUnavailableException("No audio device"))
            .when(player).play(any(Class.class), anyString(), anyFloat());
        new DfsReadyChime(player, executor).play(35);
        verify(player).play(any(Class.class), anyString(), anyFloat());
    }

    @Test public void bundledWaveIsShortQuietAndValidPcm() throws Exception
    {
        try (BufferedInputStream input = new BufferedInputStream(
            DfsReadyChime.class.getResourceAsStream("/com/dfsready/soft-chime.wav"));
             AudioInputStream audio = AudioSystem.getAudioInputStream(input))
        {
            assertEquals(44100, audio.getFormat().getSampleRate(), 0);
            assertEquals(16, audio.getFormat().getSampleSizeInBits());
            assertEquals(1, audio.getFormat().getChannels());
            assertEquals(0.65, audio.getFrameLength() / audio.getFormat().getSampleRate(), 0.001);
            byte[] samples = audio.readAllBytes();
            int peak = 0;
            for (int i = 0; i < samples.length; i += 2)
            {
                int value = (short) ((samples[i] & 255) | (samples[i + 1] << 8));
                peak = Math.max(peak, Math.abs(value));
            }
            assertTrue(peak > 1000 && peak < 11000);
            assertEquals(0, samples[0]);
            assertEquals(0, samples[1]);
            assertEquals(0, samples[samples.length - 1]);
            assertEquals(0, samples[samples.length - 2]);
        }
    }
}
