package xxx.joker.apps.video.manager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.scenicview.ScenicView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.action.AdjustVideoTitleAction;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.model.FxModel;
import xxx.joker.apps.video.manager.jfx.view.PanesSelector;
import xxx.joker.libs.core.debug.JkDebug;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.format.JkOutput;
import xxx.joker.libs.core.util.JkConsole;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static xxx.joker.libs.core.lambda.JkStreams.*;
import static xxx.joker.libs.core.lambda.JkStreams.map;
import static xxx.joker.libs.core.util.JkConsole.display;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class NewLauncher extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(NewLauncher.class);

    public static boolean scenicView;

    private PanesSelector panesSel = PanesSelector.getInstance();

    @Override
    public void init() throws Exception {
        JkDebug.start("main");
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JOKER VIDEO MANAGER");

        // Create scene
        Group root = new Group();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/common.css").toExternalForm());

        panesSel.setScene(scene);
        panesSel.displayHomePane();

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        if (scenicView) {
            ScenicView.show(scene);
        }

        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(e -> {
            if (panesSel.isHomeShowed()) {
                Platform.exit();
            } else {
                e.consume();
                panesSel.displayHomePane();
            }
        });
    }

        // Show stage
    @Override
    public void stop() throws Exception {
        panesSel.getHomePane().closePane();
        JkFiles.delete(Config.FOLDER_TEMP_SNAPS);
        JkFiles.delete(Config.FOLDER_TEMP_CUT);
        FxModel.getModel().persistData();
        LOG.info("Closing app");
        JkDebug.stop("main");
//        JkDebug.displayTimes();
    }

    public static void main(String[] args) {
        boolean cleanRepo = args.length == 1 && "--clean".equals(args[0]);
        boolean fixTitles = args.length == 1 && "--fixTitles".equals(args[0]);
        if(cleanRepo) {
            VideoRepo.getRepo().cleanResources();
            Platform.exit();
        } else if(fixTitles) {
            manageFixTitles();
            Platform.exit();
        } else {
            scenicView = args.length == 1 && "-sv".equals(args[0]);
            launch(args);
        }
    }

    private static void manageFixTitles() {
        VideoRepo repo = VideoRepo.getRepo();

            Map<Video, String> map = toMapSingle(repo.getVideos(), Function.identity(), Video::getTitle);
        AdjustVideoTitleAction.adjustTitles(repo.getVideos());

        List<Video> diffs = filterSort(map.keySet(), v -> !v.getTitle().equals(map.get(v)), Video.titleComparator());
        List<Video> equals = filterSort(map.keySet(), v -> !diffs.contains(v), Video.titleComparator());

        List<String> lines1 = map(equals, v -> strf("{}|eq|{}", map.get(v), v.getTitle()));
        display(JkOutput.columnsView(lines1));
        display("{} titles unchanged\n\n", equals.size());

        List<String> lines = map(diffs, v -> strf("{}|diff|{}", map.get(v), v.getTitle()));
        display(JkOutput.columnsView(lines));
        display("{} titles modified", diffs.size());

        if(!diffs.isEmpty()) {
            String resp = JkConsole.readUserInput("Change titles?", s -> StringUtils.equalsAnyIgnoreCase(s, "y", "n"));
            if ("y".equalsIgnoreCase(resp)) {
                repo.commit();
                LOG.info("Changed {} video titles", diffs.size());
            }
        }
    }

}