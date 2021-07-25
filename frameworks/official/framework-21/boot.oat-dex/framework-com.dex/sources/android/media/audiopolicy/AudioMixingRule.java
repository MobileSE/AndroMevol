package android.media.audiopolicy;

import android.media.AudioAttributes;
import java.util.ArrayList;
import java.util.Iterator;

public class AudioMixingRule {
    public static final int RULE_EXCLUDE_ATTRIBUTE_USAGE = 2;
    public static final int RULE_MATCH_ATTRIBUTE_USAGE = 1;
    private ArrayList<AttributeMatchCriterion> mCriteria;

    private AudioMixingRule(ArrayList<AttributeMatchCriterion> criteria) {
        this.mCriteria = criteria;
    }

    /* access modifiers changed from: package-private */
    public static final class AttributeMatchCriterion {
        AudioAttributes mAttr;
        int mRule;

        AttributeMatchCriterion(AudioAttributes attributes, int rule) {
            this.mAttr = attributes;
            this.mRule = rule;
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<AttributeMatchCriterion> getCriteria() {
        return this.mCriteria;
    }

    public static class Builder {
        private ArrayList<AttributeMatchCriterion> mCriteria = new ArrayList<>();

        public Builder addRule(AudioAttributes attrToMatch, int rule) throws IllegalArgumentException {
            if (attrToMatch == null) {
                throw new IllegalArgumentException("Illegal null AudioAttributes argument");
            } else if (rule == 1 || rule == 2) {
                synchronized (this.mCriteria) {
                    Iterator<AttributeMatchCriterion> crIterator = this.mCriteria.iterator();
                    while (true) {
                        if (!crIterator.hasNext()) {
                            this.mCriteria.add(new AttributeMatchCriterion(attrToMatch, rule));
                            break;
                        }
                        AttributeMatchCriterion criterion = crIterator.next();
                        if ((rule == 1 || rule == 2) && criterion.mAttr.getUsage() == attrToMatch.getUsage()) {
                            if (criterion.mRule != rule) {
                                throw new IllegalArgumentException("Contradictory rule exists for " + attrToMatch);
                            }
                        }
                    }
                }
                return this;
            } else {
                throw new IllegalArgumentException("Illegal rule value " + rule);
            }
        }

        public AudioMixingRule build() {
            return new AudioMixingRule(this.mCriteria);
        }
    }
}
