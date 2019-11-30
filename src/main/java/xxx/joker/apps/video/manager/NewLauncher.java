package xxx.joker.apps.video.manager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.scenicview.ScenicView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.VideoTracingAdded;
import xxx.joker.apps.video.manager.jfx.model.FxModel;
import xxx.joker.apps.video.manager.jfx.view.PanesSelector;
import xxx.joker.libs.core.file.JkEncryption;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.util.JkConvert;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static xxx.joker.libs.core.lambda.JkStreams.*;

public class NewLauncher extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(NewLauncher.class);

    public static boolean scenicView;

    private PanesSelector panesSel = PanesSelector.getInstance();

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
    }

    public static void main(String[] args) {
        boolean cleanRepo = args.length == 1 && "--clean".equals(args[0]);
        boolean moveAddedVideos = args.length == 2 && "--moveAdded".equals(args[0]);
        if(cleanRepo) {
            VideoRepo.getRepo().cleanRepo();
            Platform.exit();
        } else if(moveAddedVideos) {
            VideoRepo repo = VideoRepo.getRepo();
            Path folder = Paths.get(JkConvert.unixToWinPath(args[1]));
            List<Path> files = JkFiles.findFiles(folder, false);
            Map<String, Path> md5Map = toMapSingle(files, JkEncryption::getMD5);
            List<String> md5List = map(repo.getAddedVideoHistory(), VideoTracingAdded::getMd5);
            List<Path> pathList = filterMap(md5List, md5Map::containsKey, md5Map::get);
            Path subf = folder.resolve("alreadyAdded");
            pathList.forEach(f -> JkFiles.moveInFolder(f, subf));
            LOG.info("Moved {} files in {}", files.size(), subf);
            Platform.exit();
        } else {
            scenicView = args.length == 1 && "-sv".equals(args[0]);
            launch(args);
        }
    }

}