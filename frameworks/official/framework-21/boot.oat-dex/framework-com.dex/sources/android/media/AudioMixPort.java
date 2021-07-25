package android.media;

public class AudioMixPort extends AudioPort {
    AudioMixPort(AudioHandle handle, int role, int[] samplingRates, int[] channelMasks, int[] formats, AudioGain[] gains) {
        super(handle, role, samplingRates, channelMasks, formats, gains);
    }

    @Override // android.media.AudioPort
    public AudioMixPortConfig buildConfig(int samplingRate, int channelMask, int format, AudioGainConfig gain) {
        return new AudioMixPortConfig(this, samplingRate, channelMask, format, gain);
    }

    @Override // android.media.AudioPort
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AudioMixPort)) {
            return false;
        }
        return super.equals(o);
    }
}
