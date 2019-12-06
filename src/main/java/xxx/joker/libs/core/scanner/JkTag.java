package xxx.joker.libs.core.scanner;

import xxx.joker.libs.core.object.JkRange;

import java.util.List;
import java.util.Map;

public interface JkTag {

    Map<String, String> getAllAttributes();
    String getAttribute(String attrName);

    boolean hasAttribute(String attrName);
    boolean matchAttribute(String attrName, String attrValue);
    boolean matchAttributes(String... attribs);

    boolean hasChildren(String childName, String... attributes);

    boolean isAutoClosed();

    String getTagName();

    JkTag getChild(int childNum, int... subNums);
    JkTag getChild(String childName, String... attributes);
    List<JkTag> getChildren();
    List<JkTag> getChildren(String... childNames);

    JkTag walkFirstChild(String... tagsPaths);
    List<JkTag> walkChildren(String... tagsPaths);

    JkTag findFirstTag(String tagName, String... attributes);
    List<JkTag> findFirstTags(String tagName, String... attributes);

    List<JkTag> findAllTags(String tagName, String... attributes);

    JkRange getRange();

    String getHtmlTag();
    String getText();
    String getTextFlat();

    JkTag cloneTag();
}
