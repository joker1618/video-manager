package xxx.joker.libs.repo.design.entities;

import xxx.joker.libs.core.format.JkSortFormattable;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.test.JkTests;
import xxx.joker.libs.core.util.JkStrings;

import java.util.*;

public class RepoTags extends JkSortFormattable<RepoTags> {

    private static final String SEP = "-";

    private final TreeSet<String> tags = new TreeSet<>(Comparator.comparing(String::toLowerCase));

    public RepoTags() {

    }
    public RepoTags(String... tags) {
        this(JkStrings.splitFlat(tags));
    }
    public RepoTags(Collection<String> tags) {
        List<String> lowercase = JkStreams.map(tags, String::toLowerCase);
        this.tags.addAll(lowercase);
    }

    public static RepoTags of(String... tags) {
        return new RepoTags(tags);
    }

    public RepoTags addTags(String... tags) {
        RepoTags toRet = new RepoTags(tags);
        toRet.tags.addAll(JkStrings.splitFlat(tags));
        return toRet;
    }
    public boolean belongToGroup(RepoTags ot) {
        return JkStreams.filter(ot.tags, t -> !JkTests.containsIgnoreCase(tags, t)).isEmpty();
    }

    @Override
    public String format() {
        return JkStreams.join(tags, SEP, String::toLowerCase);
    }

    @Override
    public RepoTags parse(String str) {
        List<String> strList = JkStrings.splitList(str, SEP);
        strList = JkStreams.map(strList, String::toLowerCase);
        tags.clear();
        tags.addAll(strList);
        return this;
    }
}
