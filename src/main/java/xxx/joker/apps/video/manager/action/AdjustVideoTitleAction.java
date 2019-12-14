package xxx.joker.apps.video.manager.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.test.JkTests;
import xxx.joker.libs.core.util.JkStrings;

import java.util.*;
import java.util.function.Function;

import static xxx.joker.libs.core.lambda.JkStreams.*;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class AdjustVideoTitleAction {

    public static void adjustTitles(Collection<Video> videos) {
        List<String[]> regexps = readRexExpPairsFromFile();
        List<Video> sorted = sorted(videos, Video.titleComparator());
        Map<Video, String> changesMap = toMapSingle(sorted, Function.identity(), v -> applyRegExps(v.getTitle(), regexps));
        List<Video> unchanged = filter(sorted, v -> v.getTitle().equals(changesMap.get(v)));
        List<String> newTitles = map(unchanged, Video::getTitle);
        List<Video> videosToChange = filter(sorted, v -> !unchanged.contains(v));
        for (Video video : videosToChange) {
            String changedTitle = changesMap.get(video);
            String finalTitle = StringUtils.capitalize(changedTitle.toLowerCase());
            int counter = 1;
            while (JkTests.containsIgnoreCase(newTitles, finalTitle)) {
                finalTitle = strf("{}.{}", changedTitle, counter++);
            }
            newTitles.add(finalTitle);
            video.setTitle(finalTitle);
        }
    }

    private static List<String[]> readRexExpPairsFromFile() {
        List<String> lines = JkFiles.readLines(AdjustVideoTitleAction.class.getResourceAsStream(Config.REGEXP_TITLE_FILEPATH));
        List<String[]> rows = map(lines, l -> JkStrings.splitArr(l, "|"));
        if(!rows.isEmpty()) {
            String[] header = rows.remove(0);
            rows.removeIf(arr -> arr.length != header.length);
        }
        return rows;
    }

    private static String applyRegExps(String title, List<String[]> regexps) {
        String toRet = title;
        for (String[] regexp : regexps) {
            toRet = toRet.replaceAll(regexp[0], regexp[1]);
        }
        return StringUtils.capitalize(toRet.toLowerCase());
    }

}
