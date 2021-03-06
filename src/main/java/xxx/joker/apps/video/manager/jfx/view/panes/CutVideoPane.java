package xxx.joker.apps.video.manager.jfx.view.panes;

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
import xxx.joker.apps.video.manager.jfx.model.FxModel;
import xxx.joker.apps.video.manager.jfx.model.FxVideo;
import xxx.joker.apps.video.manager.jfx.view.PanesSelector;
import xxx.joker.apps.video.manager.jfx.view.gridpane.GridPaneBuilder;
import xxx.joker.apps.video.manager.jfx.view.provider.IconProvider;
import xxx.joker.apps.video.manager.jfx.view.videoplayer.JfxVideoBuilder;
import xxx.joker.apps.video.manager.jfx.view.videoplayer.JfxVideoPlayer;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkConvert;
import xxx.joker.libs.core.util.JkStrings;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static xxx.joker.libs.javafx.util.JfxControls.*;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class CutVideoPane extends BorderPane implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(CutVideoPane.class);

    private final FxModel model = FxModel.getModel();

    private JfxVideoPlayer videoPlayer;

    public CutVideoPane(Video video) {
        FxVideo fxVideo = model.toFxVideo(video);

        setCenter(createCenterPane(fxVideo));
        setRight(createRightPane());

        getStylesheets().add(getClass().getResource("/css/CutVideoPane.css").toExternalForm());
    }

    private Pane createCenterPane(FxVideo fxVideo) {
        JfxVideoBuilder vbuilder = new JfxVideoBuilder();
        vbuilder.setShowBorder(true);
        vbuilder.setShowClose(false);
        vbuilder.setBackward5Milli(3 * 1000L);
        vbuilder.setBackward10Milli(10 * 1000L);
        vbuilder.setForward5Milli(3 * 1000L);
        vbuilder.setForward10Milli(10 * 1000L);
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

        Image imgDel = new IconProvider().getIconImage(IconProvider.DELETE_RED);

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
                gpBuilder.add(row, col++, JkDuration.of(sp).strElapsed());
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
        String origSplittedCategory = "splitted";
        String defaultCustomCategory = "reduced";
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

                    new Alert(Alert.AlertType.CONFIRMATION);

                    List<Video> addedList = cutVideo(btnStartEnd.isDisable(), seekPoints, cats);

                    Category customCat = JkStreams.findUnique(model.getCategories(), cat -> cat.getName().equals(origSplittedCategory));
                    if (customCat == null) {
                        customCat = new Category(origSplittedCategory);
                        model.getCategories().add(customCat);
                    }
                    videoPlayer.getFxVideo().getVideo().getCategories().add(customCat);
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

    private List<Video> cutVideo(boolean splitByPoints, Collection<Duration> seekPoints, Set<Category> customCats) {
        List<Video> addedList = new ArrayList<>();

        FxVideo fxVideo = videoPlayer.getFxVideo();
        Path sourcePath = JkFiles.copyInFolder(fxVideo.getPath(), Config.FOLDER_TEMP_CUT);
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
            FxVideo added = model.addVideoFile(finalPath, false);
            if(added != null) {
                added.getVideo().setTitle(fxVideo.getVideo().getTitle()+"_cut");
                added.getVideo().getCategories().addAll(cats);
                addedList.add(added.getVideo());
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
                FxVideo added = model.addVideoFile(fp, false);
                if(added != null) {
                    added.getVideo().setTitle(strf("%s.%02d_cut", fxVideo.getVideo().getTitle(), index.getAndIncrement()));
                    added.getVideo().getCategories().addAll(cats);
                    addedList.add(added.getVideo());
                }
            });
        }

        return addedList;
    }

    @Override
    public void closePane() {
        videoPlayer.closePlayer();
    }
}
