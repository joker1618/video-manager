package xxx.joker.apps.video.manager.jfx.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.scenicview.ScenicView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.jfx.model.FxModel;
import xxx.joker.libs.core.file.JkFiles;

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
        scenicView = args.length == 1 && "-sv".equals(args[0]);
//		scenicView = true;
        launch(args);
    }

}