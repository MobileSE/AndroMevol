package android.sax;

import android.net.ProxyInfo;
import java.util.ArrayList;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class Element {
    Children children;
    final int depth;
    EndElementListener endElementListener;
    EndTextElementListener endTextElementListener;
    final String localName;
    final Element parent;
    ArrayList<Element> requiredChilden;
    StartElementListener startElementListener;
    final String uri;
    boolean visited;

    Element(Element parent2, String uri2, String localName2, int depth2) {
        this.parent = parent2;
        this.uri = uri2;
        this.localName = localName2;
        this.depth = depth2;
    }

    public Element getChild(String localName2) {
        return getChild(ProxyInfo.LOCAL_EXCL_LIST, localName2);
    }

    public Element getChild(String uri2, String localName2) {
        if (this.endTextElementListener != null) {
            throw new IllegalStateException("This element already has an end text element listener. It cannot have children.");
        }
        if (this.children == null) {
            this.children = new Children();
        }
        return this.children.getOrCreate(this, uri2, localName2);
    }

    public Element requireChild(String localName2) {
        return requireChild(ProxyInfo.LOCAL_EXCL_LIST, localName2);
    }

    public Element requireChild(String uri2, String localName2) {
        Element child = getChild(uri2, localName2);
        if (this.requiredChilden == null) {
            this.requiredChilden = new ArrayList<>();
            this.requiredChilden.add(child);
        } else if (!this.requiredChilden.contains(child)) {
            this.requiredChilden.add(child);
        }
        return child;
    }

    public void setElementListener(ElementListener elementListener) {
        setStartElementListener(elementListener);
        setEndElementListener(elementListener);
    }

    public void setTextElementListener(TextElementListener elementListener) {
        setStartElementListener(elementListener);
        setEndTextElementListener(elementListener);
    }

    public void setStartElementListener(StartElementListener startElementListener2) {
        if (this.startElementListener != null) {
            throw new IllegalStateException("Start element listener has already been set.");
        }
        this.startElementListener = startElementListener2;
    }

    public void setEndElementListener(EndElementListener endElementListener2) {
        if (this.endElementListener != null) {
            throw new IllegalStateException("End element listener has already been set.");
        }
        this.endElementListener = endElementListener2;
    }

    public void setEndTextElementListener(EndTextElementListener endTextElementListener2) {
        if (this.endTextElementListener != null) {
            throw new IllegalStateException("End text element listener has already been set.");
        } else if (this.children != null) {
            throw new IllegalStateException("This element already has children. It cannot have an end text element listener.");
        } else {
            this.endTextElementListener = endTextElementListener2;
        }
    }

    public String toString() {
        return toString(this.uri, this.localName);
    }

    static String toString(String uri2, String localName2) {
        StringBuilder append = new StringBuilder().append("'");
        if (!uri2.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
            localName2 = uri2 + ":" + localName2;
        }
        return append.append(localName2).append("'").toString();
    }

    /* access modifiers changed from: package-private */
    public void resetRequiredChildren() {
        ArrayList<Element> requiredChildren = this.requiredChilden;
        if (requiredChildren != null) {
            for (int i = requiredChildren.size() - 1; i >= 0; i--) {
                requiredChildren.get(i).visited = false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void checkRequiredChildren(Locator locator) throws SAXParseException {
        ArrayList<Element> requiredChildren = this.requiredChilden;
        if (requiredChildren != null) {
            for (int i = requiredChildren.size() - 1; i >= 0; i--) {
                Element child = requiredChildren.get(i);
                if (!child.visited) {
                    throw new BadXmlException("Element named " + this + " is missing required" + " child element named " + child + ".", locator);
                }
            }
        }
    }
}
