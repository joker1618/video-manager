package xxx.joker.apps.video.manager.fxlayer.fxview.panes;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.ffmpeg.FFMPEGAdapter;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxModel;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxVideo;
import xxx.joker.apps.video.manager.fxlayer.fxview.PanesSelector;
import xxx.joker.apps.video.manager.fxlayer.fxview.builders.GridPaneBuilder;
import xxx.joker.apps.video.manager.fxlayer.fxview.provider.IconProvider;
import xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer.JfxVideoBuilder;
import xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer.JfxVideoPlayer;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.lambdas.JkStreams;
import xxx.joker.libs.core.utils.JkConvert;
import xxx.joker.libs.core.utils.JkStrings;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static xxx.joker.libs.core.javafx.JfxControls.*;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class CutVideoPane extends BorderPane implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(CutVideoPane.class);

    private final FxModel model = FxModel.getModel();

    private JfxVideoPlayer videoPlayer;

    public CutVideoPane(Video video) {
        FxVideo fxVideo = model.getFxVideo(video);

        setCenter(createCenterPane(fxVideo));
        setRight(createRightPane());

        getStylesheets().add(getClass().getResource("/css/CutVideoPane.css").toExternalForm());
    }

    private Pane createCenterPane(FxVideo fxVideo) {
        JfxVideoBuilder vbuilder = new JfxVideoBuilder();
        vbuilder.setShowBorder(true);
        vbuilder.setShowClose(false);
        vbuilder.setBackward5Milli(1000L);
        vbuilder.setBackward10Milli(5 * 1000L);
        vbuilder.setForward5Milli(1000L);
        vbuilder.setForward10Milli(5 * 1000L);
        videoPlayer = vbuilder.createPane(fxVideo);
//        return createVBox("centerBox", videoPlayer);
        return videoPlayer;
    }

    private Pane createRightPane() {
        VBox box = createVBox("rightBox");

        ObservableSet<Duration> seekPoints = FXCollections.observableSet(new TreeSet<>());
        Button btnStartEnd = new Button("SET START");
        Button btnCutPoint = new Button("SET POINT");
        Button btnSplit = new Button("SPLIT");

        HBox btnBox = createHBox("buttonsBox", btnStartEnd, btnCutPoint, btnSplit);
        HBox spBox = createHBox("seekPointsBox");
        box.getChildren().addAll(btnBox, spBox);

        Image imgDel = new IconProvider().getIcon(IconProvider.DELETE_RED).getImage();

        seekPoints.addListener((SetChangeListener<Duration>) c -> {
            if(seekPoints.isEmpty()) {
                btnStartEnd.setDisable(false);
                btnCutPoint.setDisable(false);
            }
            if(!btnStartEnd.isDisable()) {
                btnStartEnd.setText("SET " + (seekPoints.size()%2==0 ? "START" : "END"));
            }
            MediaPlayer mp = videoPlayer.getMediaView().getMediaPlayer();
            GridPaneBuilder gpBuilder = new GridPaneBuilder();
            int row = 0;
            for (Duration sp : seekPoints) {
                if(!btnStartEnd.isDisable()) {
                    gpBuilder.add(row, 0, row%2==0 ? "S" : "E");
                }
                int col = !btnStartEnd.isDisable() ? 1 : 0;
                gpBuilder.add(row, col++, JkDuration.of(sp).toStringElapsed());
                Button btnSeek = new Button("SEEK");
                gpBuilder.add(row, col++, btnSeek);
                btnSeek.setOnAction(e -> mp.seek(sp));
                Button btnDel = new Button();
                btnDel.setGraphic(createImageView(imgDel, 30, 30));
                btnDel.setOnAction(e -> seekPoints.remove(sp));
                gpBuilder.add(row, col++, btnDel);
                row++;
            }
            spBox.getChildren().setAll(gpBuilder.createGridPane());
        });

        btnStartEnd.setOnAction(e -> {
            btnCutPoint.setDisable(true);
            seekPoints.add(videoPlayer.getMediaView().getMediaPlayer().getCurrentTime());
        });

        btnCutPoint.setOnAction(e -> {
            btnStartEnd.setDisable(true);
            seekPoints.add(videoPlayer.getMediaView().getMediaPlayer().getCurrentTime());
        });

        btnSplit.disableProperty().bind(Bindings.createBooleanBinding(() -> seekPoints.isEmpty() || (seekPoints.size() % 2 != 0 && !btnStartEnd.isDisable()), seekPoints));
        String defaultCustomCategory = "splitted";
        btnSplit.setOnAction(e -> {
            try {
                if (!seekPoints.isEmpty() && !(seekPoints.size() % 2 != 0 && !btnStartEnd.isDisable())) {
                    TextInputDialog dialog = new TextInputDialog(defaultCustomCategory);
                    dialog.setHeaderText("Set custom category");
                    dialog.setContentText("Categories:");

                    Optional<String> result = dialog.showAndWait();
                    List<String> customCatNames = JkStrings.splitList(result.isPresent() && !result.get().trim().isEmpty() ? result.get() : defaultCustomCategory, " ", true);
                    customCatNames.removeIf(StringUtils::isBlank);

                    Set<Category> cats = new TreeSet<>();
                    customCatNames.forEach(cn -> {
                        Category customCat = JkStreams.findUnique(model.getCategories(), cat -> cat.getName().equalsIgnoreCase(cn));
                        if (customCat == null) {
                            customCat = new Category(cn);
                            model.getCategories().add(customCat);
                        }
                        cats.add(customCat);
                    });

                    cutVideo(btnStartEnd.isDisable(), seekPoints, cats);
                }

            } catch (Throwable t) {
                LOG.error("Error during split", t);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error during split");
                alert.setContentText(JkStreams.joinLines(Arrays.asList(t.getStackTrace()), StackTraceElement::toString));
                alert.showAndWait();
            }
            PanesSelector.getInstance().displayHomePane();
        });

        return box;
    }

    private void cutVideo(boolean splitByPoints, Collection<Duration> seekPoints, Set<Category> customCats) {
        FxVideo fxVideo = videoPlayer.getFxVideo();
        Path sourcePath = JkFiles.copyInFolder(fxVideo.getPath(), Config.TEMP_FOLDER);
        Video video = fxVideo.getVideo();

        Set<Category> cats = new TreeSet<>(customCats);
        cats.addAll(video.getCategories());

        List<Duration> spList = JkConvert.toList(seekPoints);
        if(!splitByPoints) {
            List<Path> cutList = new ArrayList<>();
            for(int i = 0; i < spList.size(); i += 2) {
                double startMs = spList.get(i).toMillis();
                double endMs = spList.get(i + 1).toMillis();
                Path cut = FFMPEGAdapter.cutVideo(sourcePath, startMs, endMs - startMs);
                cutList.add(cut);
            }
            Path finalPath;
            if(cutList.size() > 1) {
                finalPath = FFMPEGAdapter.concat(cutList);
            } else {
                finalPath = cutList.get(0);
            }
            FxVideo added = model.addVideoFile(finalPath);
            if(added != null) {
                added.getVideo().setTitle(fxVideo.getVideo().getTitle()+"_cut");
                added.getVideo().getCategories().addAll(cats);
            }
        } else {
            List<Path> finalPaths = new ArrayList<>();
            for(int i = 0; i < spList.size(); i++) {
                long msDur = (long) spList.get(i).toMillis();
                if(i == 0 && msDur > 0) {
                    finalPaths.add(FFMPEGAdapter.cutVideo(sourcePath, 0, msDur));
                }
                if(i == spList.size() - 1) {
                    if(msDur < fxVideo.getVideo().getLength().toMillis()) {
                        finalPaths.add(FFMPEGAdapter.cutVideo(sourcePath, msDur, (fxVideo.getVideo().getLength().toMillis()) - msDur));
                    }
                } else {
                    finalPaths.add(FFMPEGAdapter.cutVideo(sourcePath, msDur, ((long) spList.get(i + 1).toMillis()) - msDur));
                }
            }
            AtomicInteger index = new AtomicInteger(0);
            finalPaths.forEach(fp -> {
                FxVideo added = model.addVideoFile(fp);
                if(added != null) {
                    added.getVideo().setTitle(strf("%s.%02d_cut", fxVideo.getVideo().getTitle(), index.getAndIncrement()));
                    added.getVideo().getCategories().addAll(cats);
                }
            });
        }

        JkFiles.delete(Config.TEMP_FOLDER);
    }

    @Override
    public void closePane() {
        videoPlayer.closePlayer();
    }
}
