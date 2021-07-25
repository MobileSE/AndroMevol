package android.graphics;

import android.media.TtmlUtils;
import android.security.KeyChain;
import android.speech.tts.TextToSpeech;
import android.util.Xml;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FontListParser {

    public static class Alias {
        public String name;
        public String toName;
        public int weight;
    }

    public static class Config {
        public List<Alias> aliases = new ArrayList();
        public List<Family> families = new ArrayList();

        Config() {
        }
    }

    public static class Font {
        public String fontName;
        public boolean isItalic;
        public int weight;

        Font(String fontName2, int weight2, boolean isItalic2) {
            this.fontName = fontName2;
            this.weight = weight2;
            this.isItalic = isItalic2;
        }
    }

    public static class Family {
        public List<Font> fonts;
        public String lang;
        public String name;
        public String variant;

        public Family(String name2, List<Font> fonts2, String lang2, String variant2) {
            this.name = name2;
            this.fonts = fonts2;
            this.lang = lang2;
            this.variant = variant2;
        }
    }

    public static Config parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, null);
            parser.nextTag();
            return readFamilies(parser);
        } finally {
            in.close();
        }
    }

    private static Config readFamilies(XmlPullParser parser) throws XmlPullParserException, IOException {
        Config config = new Config();
        parser.require(2, null, "familyset");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if (parser.getName().equals("family")) {
                    config.families.add(readFamily(parser));
                } else if (parser.getName().equals(KeyChain.EXTRA_ALIAS)) {
                    config.aliases.add(readAlias(parser));
                } else {
                    skip(parser);
                }
            }
        }
        return config;
    }

    private static Family readFamily(XmlPullParser parser) throws XmlPullParserException, IOException {
        String name = parser.getAttributeValue(null, "name");
        String lang = parser.getAttributeValue(null, "lang");
        String variant = parser.getAttributeValue(null, TextToSpeech.Engine.KEY_PARAM_VARIANT);
        List<Font> fonts = new ArrayList<>();
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if (parser.getName().equals("font")) {
                    String weightStr = parser.getAttributeValue(null, "weight");
                    fonts.add(new Font("/system/fonts/" + parser.nextText(), weightStr == null ? 400 : Integer.parseInt(weightStr), "italic".equals(parser.getAttributeValue(null, TtmlUtils.TAG_STYLE))));
                } else {
                    skip(parser);
                }
            }
        }
        return new Family(name, fonts, lang, variant);
    }

    private static Alias readAlias(XmlPullParser parser) throws XmlPullParserException, IOException {
        Alias alias = new Alias();
        alias.name = parser.getAttributeValue(null, "name");
        alias.toName = parser.getAttributeValue(null, "to");
        String weightStr = parser.getAttributeValue(null, "weight");
        if (weightStr == null) {
            alias.weight = 400;
        } else {
            alias.weight = Integer.parseInt(weightStr);
        }
        skip(parser);
        return alias;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        int depth = 1;
        while (depth > 0) {
            switch (parser.next()) {
                case 2:
                    depth++;
                    break;
                case 3:
                    depth--;
                    break;
            }
        }
    }
}
