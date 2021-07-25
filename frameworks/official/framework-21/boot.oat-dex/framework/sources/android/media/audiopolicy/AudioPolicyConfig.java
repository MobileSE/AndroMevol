package android.media.audiopolicy;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.audiopolicy.AudioMix;
import android.media.audiopolicy.AudioMixingRule;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

public class AudioPolicyConfig implements Parcelable {
    public static final Parcelable.Creator<AudioPolicyConfig> CREATOR = new Parcelable.Creator<AudioPolicyConfig>() {
        /* class android.media.audiopolicy.AudioPolicyConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AudioPolicyConfig createFromParcel(Parcel p) {
            return new AudioPolicyConfig(p);
        }

        @Override // android.os.Parcelable.Creator
        public AudioPolicyConfig[] newArray(int size) {
            return new AudioPolicyConfig[size];
        }
    };
    private static final String TAG = "AudioPolicyConfig";
    ArrayList<AudioMix> mMixes;

    AudioPolicyConfig(ArrayList<AudioMix> mixes) {
        this.mMixes = mixes;
    }

    public void addMix(AudioMix mix) throws IllegalArgumentException {
        if (mix == null) {
            throw new IllegalArgumentException("Illegal null AudioMix argument");
        }
        this.mMixes.add(mix);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMixes.size());
        Iterator<AudioMix> it = this.mMixes.iterator();
        while (it.hasNext()) {
            AudioMix mix = it.next();
            dest.writeInt(mix.getRouteFlags());
            dest.writeInt(mix.getFormat().getSampleRate());
            dest.writeInt(mix.getFormat().getEncoding());
            dest.writeInt(mix.getFormat().getChannelMask());
            ArrayList<AudioMixingRule.AttributeMatchCriterion> criteria = mix.getRule().getCriteria();
            dest.writeInt(criteria.size());
            Iterator i$ = criteria.iterator();
            while (i$.hasNext()) {
                AudioMixingRule.AttributeMatchCriterion criterion = i$.next();
                dest.writeInt(criterion.mRule);
                dest.writeInt(criterion.mAttr.getUsage());
            }
        }
    }

    private AudioPolicyConfig(Parcel in) {
        this.mMixes = new ArrayList<>();
        int nbMixes = in.readInt();
        for (int i = 0; i < nbMixes; i++) {
            AudioMix.Builder mixBuilder = new AudioMix.Builder();
            mixBuilder.setRouteFlags(in.readInt());
            int sampleRate = in.readInt();
            mixBuilder.setFormat(new AudioFormat.Builder().setSampleRate(sampleRate).setChannelMask(in.readInt()).setEncoding(in.readInt()).build());
            int nbRules = in.readInt();
            AudioMixingRule.Builder ruleBuilder = new AudioMixingRule.Builder();
            for (int j = 0; j < nbRules; j++) {
                int matchRule = in.readInt();
                if (matchRule == 2 || matchRule == 1) {
                    ruleBuilder.addRule(new AudioAttributes.Builder().setUsage(in.readInt()).build(), matchRule);
                } else {
                    Log.w(TAG, "Encountered unsupported rule, skipping");
                    in.readInt();
                }
            }
            mixBuilder.setMixingRule(ruleBuilder.build());
            this.mMixes.add(mixBuilder.build());
        }
    }

    public String toString() {
        String textDump;
        String textDump2 = new String("android.media.audiopolicy.AudioPolicyConfig:\n") + this.mMixes.size() + " AudioMix:\n";
        Iterator<AudioMix> it = this.mMixes.iterator();
        while (it.hasNext()) {
            AudioMix mix = it.next();
            textDump2 = ((((textDump2 + "* route flags=0x" + Integer.toHexString(mix.getRouteFlags()) + "\n") + "  rate=" + mix.getFormat().getSampleRate() + "Hz\n") + "  encoding=" + mix.getFormat().getEncoding() + "\n") + "  channels=0x") + Integer.toHexString(mix.getFormat().getChannelMask()).toUpperCase() + "\n";
            Iterator i$ = mix.getRule().getCriteria().iterator();
            while (i$.hasNext()) {
                AudioMixingRule.AttributeMatchCriterion criterion = i$.next();
                switch (criterion.mRule) {
                    case 1:
                        textDump = (textDump2 + "  match usage ") + criterion.mAttr.usageToString();
                        break;
                    case 2:
                        textDump = (textDump2 + "  exclude usage ") + criterion.mAttr.usageToString();
                        break;
                    default:
                        textDump = textDump2 + "invalid rule!";
                        break;
                }
                textDump2 = textDump + "\n";
            }
        }
        return textDump2;
    }
}
