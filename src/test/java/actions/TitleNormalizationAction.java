package actions;

import org.junit.Test;
import xxx.joker.apps.video.manager.action.AdjustVideoTitleAction;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.libs.core.format.JkOutput;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static xxx.joker.libs.core.lambda.JkStreams.*;
import static xxx.joker.libs.core.util.JkConsole.display;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class TitleNormalizationAction {

    @Test
    public void doTitleNormalization() {
        VideoRepo repo = VideoRepo.getRepo();
        Map<Video, String> map = toMapSingle(repo.getVideos(), Function.identity(), Video::getTitle);
        AdjustVideoTitleAction.adjustTitles(repo.getVideos());

        List<Video> diffs = filterSort(map.keySet(), v -> !v.getTitle().equals(map.get(v)), Video.titleComparator());
        List<String> lines = map(diffs, v -> strf("{}|{}", map.get(v), v.getTitle()));
        display(JkOutput.columnsView(lines));
        display("{} titles modified", diffs.size());

//        repo.commit();

        display("END");
    }

}
