package xxx.joker.apps.video.manager.fxlayer.fxview.panes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxModel;
import xxx.joker.apps.video.manager.fxlayer.fxview.PanesSelector;
import xxx.joker.apps.video.manager.fxlayer.fxview.bindings.SortFilter;
import xxx.joker.apps.video.manager.fxlayer.fxview.builders.SnapshotManager;
import xxx.joker.apps.video.manager.fxlayer.fxview.builders.GridPaneBuilder;
import xxx.joker.apps.video.manager.fxlayer.fxview.controls.JfxTable;
import xxx.joker.apps.video.manager.fxlayer.fxview.controls.JfxTableCol;
import xxx.joker.apps.video.manager.provider.StagePosProvider;
import xxx.joker.apps.video.manager.provider.VideoStagesPosition;
import xxx.joker.libs.core.datetime.JkDateTime;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.enums.JkSizeUnit;
import xxx.joker.libs.core.files.JkEncryption;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.format.JkOutput;
import xxx.joker.libs.core.javafx.JfxUtil;
import xxx.joker.libs.core.lambdas.JkStreams;
import xxx.joker.libs.core.utils.JkStruct;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static xxx.joker.libs.core.javafx.JfxControls.*;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class HomePane extends BorderPane {

    private static final Logger logger = LoggerFactory.getLogger(HomePane.class);

    private final FxModel model = FxModel.getModel();

    private SortFilter sortFilter;
    private GridPane gridPaneFilterCat;
    private JfxTable<Video> tableVideos;

    private Map<String,Pair<Label,ToggleGroup>> toggleMap = new TreeMap<>(String::compareToIgnoreCase);

    private final Image imgDelete;
    private Set<Video> markedVideos = Collections.synchronizedSet(new HashSet<>());

    public HomePane() {
        this.imgDelete = new Image(getClass().getResource("/icons/delete.png").toExternalForm());

        // left -> sort & filter
        setLeft(createLeftPane());

        // center -> table videos & videos stats
        setCenter(createCenterPane());

        // right --> buttons
        setRight(createRightPane());

        getStylesheets().add(getClass().getResource("/css/HomePane.css").toExternalForm());
    }

    private Pane createLeftPane() {
        sortFilter = new SortFilter(markedVideos);

        VBox box = new VBox();
        box.getStyleClass().add("filterBox");

        box.getChildren().add(createHBox("boxCaption", new Label("FILTER")));

        TextField nameFilter = new TextField("");
        sortFilter.videoNameProperty().bind(nameFilter.textProperty());
        box.getChildren().add(createHBox("centered", nameFilter));

        GridPaneBuilder gb = new GridPaneBuilder();
        addRadioLine(gb, "Search type:", 0, sortFilter::setUseAndOperator, false, false, false);
        ObservableList<Toggle> toggles = toggleMap.get("Search type:").getValue().getToggles();
        ((RadioButton)toggles.get(0)).setText("AND");
        ((RadioButton)toggles.get(1)).setText("OR");
        addRadioLine(gb, "Cataloged:", 1, sortFilter::setCataloged, true, false, true);
        addRadioLine(gb, "Marked:", 2, sortFilter::setMarked, true, false, true);
        box.getChildren().add(gb.createGridPane());

        gridPaneFilterCat = new GridPane();
        fillGpCategFilter();
        model.getCategories().addListener((ListChangeListener<Category>)c -> fillGpCategFilter());
        ScrollPane scrollPane = new ScrollPane(gridPaneFilterCat);
        scrollPane.getStyleClass().add("scrollPaneCateg");
        box.getChildren().add(scrollPane);

        nameFilter.prefWidthProperty().bind(scrollPane.widthProperty());

        Button button = new Button("CLEAR");
        button.setOnAction(e -> {
            nameFilter.setText("");
            toggleMap.values().forEach(
                    tg -> tg.getValue().selectToggle(JkStruct.getLastElem(tg.getValue().getToggles()))
            );
        });
        box.getChildren().add(createHBox("centered boxClearOpt", button));

        BorderPane bp = new BorderPane();
        bp.getStyleClass().add("leftPane");
        bp.setCenter(box);
//        bp.getStyleClass().add("leftBox");

        int rowNum = 0;
        GridPaneBuilder gbDet = new GridPaneBuilder();

        Label fixedNum = new Label("");
        fixedNum.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(model.getVideos().size()), model.getVideos()));
        Label selNum = new Label("");
        selNum.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(model.getSelectedVideos().size()), model.getSelectedVideos()));
        addDetailsLine(gbDet, rowNum, "Num videos", fixedNum, selNum);
        rowNum++;

        Label fixedLength = new Label("");
        fixedLength.textProperty().bind(Bindings.createStringBinding(() -> JkDuration.of(model.getVideos().stream().mapToLong(v -> v.getLength() == null ? 0L : v.getLength().toMillis()).sum()).toStringElapsed(false), model.getVideos()));
        Label selLength = new Label("");
        selLength.textProperty().bind(Bindings.createStringBinding(() -> JkDuration.of(model.getSelectedVideos().stream().filter(Objects::nonNull).mapToLong(v -> v.getLength() == null ? 0L : v.getLength().toMillis()).sum()).toStringElapsed(false), model.getSelectedVideos()));
        addDetailsLine(gbDet, rowNum, "Total length:", fixedLength, selLength);
        rowNum++;

        Label fixedSize = new Label("");
        fixedSize.textProperty().bind(Bindings.createStringBinding(() -> JkOutput.humanSize(model.getVideos().stream().mapToLong(Video::getSize).sum()), model.getVideos()));
        Label selSize = new Label("");
        selSize.textProperty().bind(Bindings.createStringBinding(() -> JkOutput.humanSize(model.getSelectedVideos().stream().filter(Objects::nonNull).mapToLong(Video::getSize).sum()), model.getSelectedVideos()));
        addDetailsLine(gbDet, rowNum, "Total size:", fixedSize, selSize);
        rowNum++;

        bp.setBottom(createHBox("paneDetails", gbDet.createGridPane()));

        return bp;
    }

    private void fillGpCategFilter() {
        GridPaneBuilder gb = new GridPaneBuilder();
        ObservableList<Category> cats = model.getCategories();
        for(int i = 0; i < cats.size(); i++) {
            addRadioLine(gb, cats.get(i), i);
        }
        gb.createGridPane(gridPaneFilterCat);
    }

    private void addRadioLine(GridPaneBuilder gpBuilder, Category cat, int row) {
        addRadioLine(gpBuilder, cat.getName(), row, b -> sortFilter.setCategory(cat, b), true, true, true);
    }
    private void addRadioLine(GridPaneBuilder gpBuilder, String catName, int row, Consumer<Boolean> setter, boolean showRadioSkip, boolean showDelBtn, boolean setStyle) {
        Pair<Label, ToggleGroup> pair = toggleMap.get(catName);

        if(pair == null) {
            Label lbl = new Label(catName);

            RadioButton radioYes = new RadioButton("Y");
            RadioButton radioNo = new RadioButton("N");
            RadioButton radioSkip = new RadioButton("Skip");

            ToggleGroup tg = new ToggleGroup();
            tg.getToggles().addAll(radioYes, radioNo);
            if(showRadioSkip) {
                tg.getToggles().add(radioSkip);
            }
            tg.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                if(setStyle)    lbl.getStyleClass().removeAll("tfBlack", "tfRed", "bold");
                if (newValue == radioYes) {
                    setter.accept(true);
                    if(setStyle)    lbl.getStyleClass().addAll("tfBlack", "bold");
                } else if (newValue == radioNo) {
                    setter.accept(false);
                    if(setStyle)    lbl.getStyleClass().addAll("tfRed", "bold");
                } else if (newValue == radioSkip) {
                    setter.accept(null);
                    if(setStyle)    lbl.getStyleClass().addAll("tfBlack");
                }
            });

            tg.selectToggle(showRadioSkip ? radioSkip : radioYes);
            pair = Pair.of(lbl, tg);
            toggleMap.put(catName, pair);
        }

        int col = 0;
        gpBuilder.add(row, col++, pair.getKey());
        for (Toggle toggle : pair.getValue().getToggles()) {
            gpBuilder.add(row, col++, (RadioButton)toggle);
        }
        if(showDelBtn) {
            Button btnDelCat = new Button();
            btnDelCat.setGraphic(createImageView(imgDelete, 25d, 25d));
            gpBuilder.add(row, col++, btnDelCat);
            btnDelCat.setOnAction(e -> {
                model.getVideos().forEach(v -> v.getCategories().removeIf(c -> c.getName().equals(catName)));
                model.getCategories().removeIf(c -> c.getName().equals(catName));
                tableVideos.refresh();
                ToggleGroup tg = toggleMap.get(catName).getRight();
                tg.selectToggle(JkStruct.getLastElem(tg.getToggles()));
                sortFilter.triggerSort();
            });
        }
    }
    private void addDetailsLine(GridPaneBuilder gpBuilder, int row, String label, Label fixedLabel, Label selLabel) {
        gpBuilder.add(row, 0, new Label(label));
        gpBuilder.add(row, 1, fixedLabel);
        gpBuilder.add(row, 2, selLabel);
    }

    public void refreshView() {
        fillGpCategFilter();
        tableVideos.refresh();
    }

    private Pane createCenterPane() {
        tableVideos = new JfxTable<>();
        tableVideos.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableVideos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        JfxTableCol<Video, String> tcolTitle = JfxTableCol.createCol("VIDEO", "title");
        tableVideos.getColumns().add(tcolTitle);
        tcolTitle.setFixedPrefWidth(500);

        JfxTableCol<Video, Long> tcolSize = JfxTableCol.createCol("SIZE", "size", l -> JkOutput.humanSize(l, JkSizeUnit.MB, false));
        tableVideos.getColumns().add(tcolSize);
        tcolSize.setFixedPrefWidth(100);

        JfxTableCol<Video,String> tcolDim = JfxTableCol.createCol("WxH", v -> v.getWidth() + v.getHeight() == 0d ? "" : strf("{}x{}", v.getWidth(), v.getHeight()));
        tableVideos.getColumns().add(tcolDim);
        tcolDim.setFixedPrefWidth(100);

        JfxTableCol<Video,Double> tcolResolution = JfxTableCol.createCol("W/H", v -> v.getHeight() == 0d ? 0d : (double)v.getWidth()/v.getHeight(), d -> strf("%.2f", d));
        tableVideos.getColumns().add(tcolResolution);
        tcolResolution.setFixedPrefWidth(75);

        JfxTableCol<Video, JkDuration> tcolLength = JfxTableCol.createCol("LENGTH", "length", d -> d == null ? "" : d.toStringElapsed(false));
        tableVideos.getColumns().add(tcolLength);
        tcolLength.setFixedPrefWidth(75);

        JfxTableCol<Video,Integer> tcolPlayTimes = JfxTableCol.createCol("N.PLAY", "playTimes");
        tableVideos.getColumns().add(tcolPlayTimes);
        tcolPlayTimes.setFixedPrefWidth(75);

        JfxTableCol<Video,Integer> tcolNumSnapshots = JfxTableCol.createCol("SNAP", v -> v.getSnapTimes().size());
        tableVideos.getColumns().add(tcolNumSnapshots);
        tcolNumSnapshots.setFixedPrefWidth(75);

        JfxTableCol<Video, JkDateTime> tcolCreationTm = JfxTableCol.createCol("CREATION", "creationTm", null, jdt -> jdt == null ? "" : jdt.format("dd/MM/yyyy   HH:mm:ss"));
        tableVideos.getColumns().add(tcolCreationTm);
        tcolCreationTm.setFixedPrefWidth(170);

//        tableVideos.setMaxWidth(tableVideos.getColumns().stream().mapToDouble(TableColumn::getMinWidth).sum() + 22 + 2);

        // Center all columns but the first one
        tableVideos.getColumns().subList(1, tableVideos.getColumns().size()).forEach(col -> col.getStyleClass().add("centered"));

        FilteredList<Video> filteredList = new FilteredList<>(model.getVideos());
        filteredList.predicateProperty().bind(sortFilter);

        SortedList<Video> tableItems = new SortedList<>(filteredList);
        tableItems.comparatorProperty().bind(tableVideos.comparatorProperty());
        tableVideos.setItems(tableItems);
        tableVideos.resizeWidth();
        model.getVideos().addListener((ListChangeListener<Video>)c -> {
            tableVideos.refresh();
            markedVideos.removeIf(v -> !model.getVideos().contains(v));
        });

        VBox vbox = new VBox(tableVideos);
        vbox.getStyleClass().add("centerBox");
        VBox.setVgrow(tableVideos, Priority.ALWAYS);

        Consumer<Object> selEvent = obj -> {
            ObservableList<Video> sitems = tableVideos.getSelectionModel().getSelectedItems();
            model.getSelectedVideos().setAll(sitems.isEmpty() ? tableItems : sitems);
        };
        tableVideos.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super Video>) selEvent::accept);
        tableItems.addListener((ListChangeListener<? super Video>) selEvent::accept);

        model.getSelectedVideos().setAll(tableItems);

        BorderPane bpTop = new BorderPane();
        bpTop.getStyleClass().add("topPane");
        vbox.getChildren().add(0, bpTop);

        Button btnAddVideos = new Button("ADD VIDEOS");
        btnAddVideos.setOnAction(this::actionAddVideos);
        Button btnManageVideos = new Button("MANAGE");
        btnManageVideos.setOnAction(e -> PanesSelector.getInstance().displayManagementPane());
        bpTop.setLeft(createHBox("spacing20", btnManageVideos, btnAddVideos));

        Button btnMark = new Button("MARK");
        btnMark.setOnAction(e -> markedVideos.addAll(model.getSelectedVideos()));
        Button btnUnmark = new Button("UNMARK");
        btnUnmark.setOnAction(e -> markedVideos.removeAll(model.getSelectedVideos()));
        Button btnAutoSnap = new Button("AUTOSNAP");
        btnAutoSnap.setOnAction(e -> {
            SimpleBooleanProperty finished = new SnapshotManager().runAutoSnap(model.getSelectedVideos());
            if(finished != null) {
                finished.addListener((obs,o,n) -> { if(finished.get()) tableVideos.refresh(); });
            }
        });
        bpTop.setCenter(createHBox("spacing20 centered", btnMark, btnUnmark, btnAutoSnap));

        Button btnDelete = new Button("DELETE");
        btnDelete.setOnAction(e -> model.getVideos().removeAll(model.getSelectedVideos()));
        bpTop.setRight(createHBox("centered", btnDelete));

        return vbox;
    }

    private void actionAddVideos(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select videos");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP4", "*.mp4"));
        List<File> files = fc.showOpenMultipleDialog(JfxUtil.getWindow(event));
        if(files != null && !files.isEmpty()) {
            addNewVideos(JkStreams.map(files, File::toPath));
        }
    }

    private void addNewVideos(List<Path> pathList) {
        Dialog dlg = new Dialog();
        dlg.getDialogPane().getButtonTypes().clear();
        dlg.setTitle(null);
        dlg.setHeaderText(null);
        dlg.setContentText(strf("Analyzing {} files", pathList.size()));
        dlg.show();

        for(Path p : pathList) {
            Video v = createFromPath(p);
            boolean addVideo = !model.getVideos().contains(v);
            if(addVideo) {
                model.addVideoFile(v, p);
                readVideoLengthWidthHeight(v);
                model.getVideos().add(v);
                logger.info("New video added {}", p);
            } else {
                logger.info("Skip add for video {}: already exists", p);
            }
        }

        dlg.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dlg.close();
    }

    private Video createFromPath(Path path)  {
        Video video = new Video();
        video.setMd5(JkEncryption.getMD5(path));
        video.setTitle(JkFiles.getFileName(path));
        video.setSize(JkFiles.safeSize(path));
        return video;
    }

    private void readVideoLengthWidthHeight(Video video) {
        MediaView mv = new MediaView();
        Path videoPath = model.getVideoFile(video);
        Media media = new Media(JkFiles.toURL(videoPath));
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(false);
        mediaPlayer.setVolume(0d);
        mv.setMediaPlayer(mediaPlayer);
        AtomicInteger aint = new AtomicInteger(0);
        SimpleIntegerProperty iprop = new SimpleIntegerProperty(0);
        mediaPlayer.totalDurationProperty().addListener((obs,o,n) -> { video.setLength(JkDuration.of(n)); iprop.set(aint.incrementAndGet());});
        media.widthProperty().addListener((obs,o,n) -> { video.setWidth(n.intValue()); iprop.set(aint.incrementAndGet());});
        media.heightProperty().addListener((obs,o,n) -> { video.setHeight(n.intValue()); iprop.set(aint.incrementAndGet());});
        iprop.addListener((obs,o,n) -> { if(n.intValue() == 3) { mediaPlayer.stop(); mediaPlayer.dispose(); tableVideos.refresh(); }});
        mediaPlayer.play();
    }

    private Pane createRightPane() {
        VBox box = createVBox("rightBox");

        VBox vbox = new VBox();
        box.getChildren().add(vbox);
        vbox.getStyleClass().addAll("boxPlayVideos");

        ComboBox<VideoStagesPosition> combo = new ComboBox<>();
        combo.getItems().setAll(StagePosProvider.getVideoPosList());
        combo.setConverter(new StringConverter<VideoStagesPosition>() {
            @Override
            public String toString(VideoStagesPosition object) {
                return object.getName().replace("_", " ");
            }
            @Override
            public VideoStagesPosition fromString(String string) {
                return StagePosProvider.getStagesPosition(string);
            }
        });
        combo.getSelectionModel().selectFirst();
        Button btnPlay = new Button("PLAY");
        vbox.getChildren().addAll(combo, btnPlay);

        return box;
    }
}
