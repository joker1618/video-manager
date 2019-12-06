package xxx.joker.libs.core.scanner;

import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.object.JkRange;
import xxx.joker.libs.core.test.JkTests;
import xxx.joker.libs.core.util.JkStrings;

import java.util.*;

import static xxx.joker.libs.core.util.JkStrings.strf;

class JkTagImpl implements JkTag {

    // The root tag has null parent, but has the html set
    // All other tags:
    // - have a tag parent, but not html string
    // - to retrieve the html, they ask to parent
    private JkTagImpl parent;
    private String html;

    private String tagName;
    private int startPos;
    private int endPos;
    private boolean autoClosed;
    private Map<String, String> attributes;
    private List<JkTag> children;


    public JkTagImpl() {
        this(null);
    }
    public JkTagImpl(String tagName) {
        this.tagName = tagName;
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.startPos = -1;
        this.endPos = -1;
    }

    @Override
    public Map<String, String> getAllAttributes() {
        return new HashMap<>(attributes);
    }
   
    @Override
    public String getAttribute(String attrName) {
        return attributes.get(attrName);
    }

    @Override
    public boolean hasAttribute(String attrName) {
        return attributes.get(attrName) != null;
    }

    @Override
    public boolean matchAttribute(String attrName, String attrValue) {
        return hasAttribute(attrName) && attrValue.equals(getAttribute(attrName));
    }

    @Override
    public boolean matchAttributes(String... attribs) {
        for (String attrib : attribs) {
            String[] split = JkStrings.splitArr(attrib, "=", true);
            if(!matchAttribute(split[0], split[1])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasChildren(String childName, String... attributes) {
        List<JkTag> chlist = getChildren(childName);
        chlist.removeIf(ch -> !ch.matchAttributes(attributes));
        return !chlist.isEmpty();
    }

    @Override
    public boolean isAutoClosed() {
        return autoClosed;
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public JkTag getChild(int childNum, int... subNums) {
        JkTag source = childNum < children.size() ? children.get(childNum) : null;
        for(int i = 0; source != null && i < subNums.length; i++) {
            source = source.getChild(subNums[i]);
        }
        return source;
    }

    @Override
    public JkTag getChild(String childName, String... attributes) {
        List<JkTag> children = getChildren(childName);
        children.removeIf(ch -> !ch.matchAttributes(attributes));
        return children.isEmpty() ? null : children.get(0);
    }

    @Override
    public List<JkTag> getChildren() {
        return children;
    }

    @Override
    public List<JkTag> getChildren(String... childNames) {
        return JkStreams.filter(children, ch -> JkTests.containsIgnoreCase(childNames, ch.getTagName()));
    }

    @Override
    public JkTag walkFirstChild(String... tagsPaths) {
        List<JkTag> res = walkChildren(tagsPaths);
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public List<JkTag> walkChildren(String... tagsPaths) {
        for (String tagsPath : tagsPaths) {
            List<JkTag> childs = walkTagChilds(tagsPath);
            if(!childs.isEmpty())   return childs;
        }
        return Collections.emptyList();
    }

    @Override
    public JkTag findFirstTag(String tagName, String... attributes) {
        List<JkTag> res = findFirstTags(tagName, attributes);
        return res.isEmpty() ? null : res.get(0);
    }

    @Override
    public List<JkTag> findFirstTags(String tagName, String... attributes) {
        List<JkTag> res = getChildren(tagName);
        if(attributes.length > 0) {
            res.removeIf(t -> !t.matchAttributes(attributes));
        }
        if(res.isEmpty()) {
            for (JkTag child : children) {
                res = child.findFirstTags(tagName, attributes);
                if(!res.isEmpty()) {
                    return res;
                }
            }
        }
        return res;
    }

    @Override
    public List<JkTag> findAllTags(String tagName, String... attributes) {
        List<JkTag> found = new ArrayList<>();
        JkTag tag = this;
        for (JkTag child : tag.getChildren()) {
            if(child.getTagName().equalsIgnoreCase(tagName) && child.matchAttributes(attributes)) {
                found.add(child);
            }
            found.addAll(child.findAllTags(tagName, attributes));
        }
        return found;
    }

    private List<JkTag> walkTagChilds(String tagsPath) {
        JkTag t = this;
        int pos = 0;
        String[] tagsName = JkStrings.splitArr(tagsPath, " ", true);
        for(; pos < tagsName.length - 1; pos++) {
            String tn = tagsName[pos];
            t = t.getChild(tn);
            if(t == null) {
                return Collections.emptyList();
            }
        }
        return t.getChildren(tagsName[pos]);
    }

    @Override
    public JkRange getRange() {
        return JkRange.ofBounds(startPos, endPos);
    }

    @Override
    public String getHtmlTag() {
        return getFullHtml().substring(startPos, endPos);
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        if(!children.isEmpty()) {
            List<JkRange> chRanges = JkStreams.map(children, ch -> ch.getRange().shiftStart(-1 * startPos));
            String htag = getHtmlTag();
            int start = 0;
            for (JkRange r : chRanges) {
                sb.append(htag, start, r.getStart());
                start = r.getEnd();
            }
            sb.append(htag.substring(start));
        } else {
            sb.append(getHtmlTag());
        }

        String str = sb.toString().replaceAll("^<[^<]*?>", "").replaceAll("</[^<]*?>$", "");
        return JkHtmlChars.fixDirtyChars(str).trim();
    }

    @Override
    public String getTextFlat() {
        return getHtmlTag().replaceAll("<[^<]*?>", "").trim();
    }

    @Override
    public JkTag cloneTag() {
        return JkScanners.parseHtmlTag(getHtmlTag(), getTagName());
    }

    @Override
    public String toString() {
        String str = tagName;
        if(!attributes.isEmpty()) {
            String join = JkStreams.join(attributes.entrySet(), ", ", a -> a.getKey() + "=" + a.getValue());
            str = strf("{}  [{}]", str, join);
        }
        return str;
    }

    private String getFullHtml() {
        return html == null ? parent.getFullHtml() : html;
    }

    public JkTag getParent() {
        return parent;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    protected void setTagName(String tagName) {
        this.tagName = tagName;
    }

    protected void setAutoClosed(boolean autoClosed) {
        this.autoClosed = autoClosed;
    }

    protected void setParent(JkTagImpl parent) {
        this.parent = parent;
    }

    protected void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    protected void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    protected Map<String, String> getAttributes() {
        return attributes;
    }

    protected void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    protected void setHtml(String html) {
        this.html = html;
    }
}