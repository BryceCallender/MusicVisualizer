package Music;

import javafx.scene.media.*;

public class MusicController
{
    private MediaPlayer player;
    private AudioSpectrumListener audioSpectrumListener;
    private float[] values;

    public MusicController(MediaPlayer player)
    {
        this.player = player;
        values = new float[128];
        audioSpectrumListener = new AudioSpectrumListener()
        {
            @Override
            public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases)
            {
                //Go through the magnitudes of the frequencies and add the default back since its -60db up and
                //we want > 0 values for height
                for(int i = magnitudes.length - 1; i >= 0; i--)
                {
                    values[i] = magnitudes[i] + 60;
                }
            }
        };

        player.setAudioSpectrumListener(audioSpectrumListener);
    }

    public void play()
    {
        player.play();
    }

    public void pause()
    {
        player.pause();
    }

    public float[] getValues()
    {
        return values;
    }

}
