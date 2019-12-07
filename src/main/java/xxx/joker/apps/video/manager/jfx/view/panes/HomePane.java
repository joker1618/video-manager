package xxx.joker.apps.video.manager.jfx.view.panes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.model.FxModel;
import xxx.joker.apps.video.manager.jfx.model.FxSnapshot;
import xxx.joker.apps.video.manager.jfx.model.FxVideo;
import xxx.joker.apps.video.manager.jfx.view.PanesSelector;
import xxx.joker.apps.video.manager.jfx.view.bindings.SortFilter;
import xxx.joker.apps.video.manager.jfx.view.gridpane.GridPaneBuilder;
import xxx.joker.apps.video.manager.jfx.view.managers.SnapshotManager;
import xxx.joker.apps.video.manager.jfx.view.provider.IconProvider;
import xxx.joker.apps.video.manager.jfx.view.table.JfxTable;
import xxx.joker.apps.video.manager.jfx.view.table.JfxTableCol;
import xxx.joker.apps.video.manager.jfx.view.videoplayer.JfxVideoBuilder;
import xxx.joker.apps.video.manager.jfx.view.videoplayer.JfxVideoStage;
import xxx.joker.apps.video.manager.jfx.view.provider.StagePosProvider;
import xxx.joker.apps.video.manager.jfx.view.provider.VideoStagesPosition;
import xxx.joker.libs.core.datetime.JkDateTime;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.format.JkOutput;
import xxx.joker.libs.core.javafx.JfxUtil;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.runtime.JkEnvironment;
import xxx.joker.libs.core.util.JkStruct;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static xxx.joker.libs.core.javafx.JfxControls.*;
import static xxx.joker.libs.core.lambda.JkStreams.*;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class HomePane extends BorderPane implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(HomePane.class);

    private final FxModel model = FxModel.getModel();

    private SortFilter sortFilter;
    private GridPane gridPaneFilterCat;
    private Map<String,Pair<Label,ToggleGroup>> toggleMap = new TreeMap<>(String::compareToIgnoreCase);
    private JfxTable<Video> tableVideos;

    private ObservableSet<Video> holdVideos = FXCollections.observableSet();

    private SimpleBooleanProperty showCategories;
    private GridPane gpCategories;
    private SimpleBooleanProperty showSnapshots;
    private ScrollPane scrollPaneSnaps;
    private List<JfxVideoStage> showedVideoStages = new ArrayList<>();

    private final Image imgDelete;

    public HomePane() {
        this.imgDelete = new IconProvider().getIconImage(IconProvider.DELETE_RED);

        // left -> sort & filter
        setLeft(createLeftPane());

        // center -> table videos & videos stats
        setCenter(createCenterPane());

        // right --> buttons
        setRight(createRightPane());

        getStylesheets().add(getClass().getResource("/css/HomePane.css").toExternalForm());
    }

    private Pane createLeftPane() {
        sortFilter = new SortFilter(holdVideos);

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
        scrollPane.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> 15 + 10 + 2 + gridPaneFilterCat.getWidth(), gridPaneFilterCat.widthProperty()));
        scrollPane.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> 15 + 5 + 2 + gridPaneFilterCat.getHeight(), gridPaneFilterCat.heightProperty()));

        nameFilter.prefWidthProperty().bind(scrollPane.widthProperty());

        Button btnClearSearch = new Button("CLEAR");
        btnClearSearch.setOnAction(e -> {
            nameFilter.setText("");
            toggleMap.values().forEach(
                    tg -> tg.getValue().selectToggle(JkStruct.getLastElem(tg.getValue().getToggles()))
            );
        });
        Button btnTriggerSearch = new Button("TRIGGER");
        btnTriggerSearch.setOnAction(e -> sortFilter.triggerSort());
        box.getChildren().add(createHBox("centered boxSearchBtn", btnTriggerSearch, btnClearSearch));

        BorderPane bp = new BorderPane();
        bp.getStyleClass().add("leftPane");
        bp.setCenter(box);

        int rowNum = 0;
        GridPaneBuilder gbDet = new GridPaneBuilder();

        Label fixedNum = new Label("");
        fixedNum.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(model.getVideos().size()), model.getVideos()));
        Label selNum = new Label("");
        selNum.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(model.getSelectedVideos().size()), model.getSelectedVideos()));
        addDetailsLine(gbDet, rowNum, "Num videos", fixedNum, selNum);
        rowNum++;

        Label fixedLength = new Label("");
        fixedLength.textProperty().bind(Bindings.createStringBinding(() -> JkDuration.of(model.getVideos().stream().mapToLong(v -> v.getLength() == null ? 0L : v.getLength().toMillis()).sum()).strElapsed(false), model.getVideos()));
        Label selLength = new Label("");
        selLength.textProperty().bind(Bindings.createStringBinding(() -> JkDuration.of(model.getSelectedVideos().stream().filter(Objects::nonNull).mapToLong(v -> v.getLength() == null ? 0L : v.getLength().toMillis()).sum()).strElapsed(false), model.getSelectedVideos()));
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

    private Pane createCenterPane() {
        tableVideos = new JfxTable<>();
        tableVideos.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableVideos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        JfxTableCol<Video, String> tcolTitle = JfxTableCol.createCol("VIDEO", "title");
        tableVideos.getColumns().add(tcolTitle);
        tcolTitle.setFixedPrefWidth(500);

//        JfxTableCol<Video, Long> tcolSize = JfxTableCol.createCol("SIZE", "size", l -> JkOutput.humanSize(l, JkSizeUnit.MB, false));
//        tableVideos.getColumns().add(tcolSize);
//        tcolSize.setFixedPrefWidth(80);
//
//        JfxTableCol<Video,String> tcolDim = JfxTableCol.createCol("WxH", v -> v.getWidth() + v.getHeight() == 0d ? "" : strf("{}x{}", v.getWidth(), v.getHeight()));
//        tableVideos.getColumns().add(tcolDim);
//        tcolDim.setFixedPrefWidth(80);
//
//        JfxTableCol<Video,Double> tcolResolution = JfxTableCol.createCol("W/H", v -> v.getHeight() == 0d ? 0d : (double)v.getWidth()/v.getHeight(), d -> strf("%.2f", d));
//        tableVideos.getColumns().add(tcolResolution);
//        tcolResolution.setFixedPrefWidth(65);

        JfxTableCol<Video, JkDuration> tcolLength = JfxTableCol.createCol("LENGTH", "length", d -> d == null ? "" : d.strElapsed(false));
        tableVideos.getColumns().add(tcolLength);
        tcolLength.setFixedPrefWidth(75);

        JfxTableCol<Video,Integer> tcolNumSnapshots = JfxTableCol.createCol("SNAP", v -> v.getSnapTimes().size());
        tableVideos.getColumns().add(tcolNumSnapshots);
        tcolNumSnapshots.setFixedPrefWidth(65);

        JfxTableCol<Video, JkDateTime> tcolCreationTm = JfxTableCol.createCol("CREATION", "creationTm", null, jdt -> jdt == null ? "" : jdt.format("dd/MM/yyyy   HH:mm:ss"));
        tableVideos.getColumns().add(tcolCreationTm);
        tcolCreationTm.setFixedPrefWidth(160);

        // Center all columns but the first one
        tableVideos.getColumns().subList(1, tableVideos.getColumns().size()).forEach(col -> col.getStyleClass().add("centered"));

        FilteredList<Video> filteredList = new FilteredList<>(model.getVideos());
        filteredList.predicateProperty().bind(sortFilter);

        SortedList<Video> tableItems = new SortedList<>(filteredList);
        tableItems.comparatorProperty().bind(tableVideos.comparatorProperty());
        tableVideos.setItems(tableItems);
        tableVideos.resizeWidth();
        model.getVideos().addListener((ListChangeListener<Video>)c -> tableVideos.refresh());

        // On row double click, open a new stage player
        tableVideos.setRowFactory( tv -> {
            TableRow<Video> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ObservableList<Video> selItems = tableVideos.getSelectionModel().getSelectedItems();
                    if(selItems.size() == 1) {
                        Video rowData = row.getItem();
                        if(rowData.equals(selItems.get(0))) {
                            JfxVideoBuilder videoBuilder = new JfxVideoBuilder();
                            videoBuilder.setShowClose(false);
                            videoBuilder.setVisibleBtnCamera(true);
                            videoBuilder.setVisibleBtnMark(true);
                            videoBuilder.setDecoratedStage(true);
                            JfxVideoStage vstage = videoBuilder.createStage();
                            vstage.setWidth(800);
                            vstage.setHeight(600);
                            vstage.getPlayerConfig().setCloseRunnable(() -> showedVideoStages.remove(vstage));
                            vstage.playVideo(model.toFxVideo(rowData));
                            showedVideoStages.add(vstage);
                        }
                    }
                }
            });
            return row ;
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

        BorderPane bpBottom = createCenterBottomButtonsPane();
        bpBottom.getStyleClass().addAll("bottomPane", "buttonsPane");
        vbox.getChildren().add(bpBottom);

        BorderPane bpTop = new BorderPane();
        bpTop.getStyleClass().addAll("topPane", "buttonsPane", "smallBtn");
        vbox.getChildren().add(0, bpTop);

        Button btnManageVideos = new Button("MANAGE");
        btnManageVideos.setOnAction(e -> PanesSelector.getInstance().displayManagementPane());
        Button btnCutVideo = new Button("CUT VIDEO");
        btnCutVideo.setOnAction(e -> PanesSelector.getInstance().displayCutVideoPane(model.getSelectedVideos().get(0)));
        btnCutVideo.disableProperty().bind(Bindings.createBooleanBinding(() -> model.getSelectedVideos().size() != 1, model.getSelectedVideos()));
        Button btnAddVideos = new Button("ADD VIDEOS");
        btnAddVideos.setOnAction(this::actionAddVideos);
        bpTop.setLeft(createHBox("spacing10", btnManageVideos, btnCutVideo, btnAddVideos));

        String lblHold = "HOLD";
        String lblUnhold = "UNHOLD";
        Button btnHold = new Button(lblHold);
        btnHold.setOnAction(e -> {
            synchronized (model.getSelectedVideos()) {
                if(!model.getSelectedVideos().isEmpty()) {
                    if(lblHold.equals(btnHold.getText())) {
                        holdVideos.addAll(model.getSelectedVideos());
                        btnHold.setText(lblUnhold);
                    } else {
                        holdVideos.removeAll(model.getSelectedVideos());
                        btnHold.setText(lblHold);
                    }
                    sortFilter.triggerSort();
                }
            }
        });
        String lblMark = "MARK";
        String lblUnmark = "UNMARK";
        Button btnMark = new Button(lblMark);
        btnMark.setOnAction(e -> {
            synchronized (model.getSelectedVideos()) {
                if(!model.getSelectedVideos().isEmpty()) {
                    if(lblMark.equals(btnMark.getText())) {
                        model.getSelectedVideos().forEach(v -> v.setMarked(true));
                        btnMark.setText(lblUnmark);
                    } else {
                        model.getSelectedVideos().forEach(v -> v.setMarked(false));
                        btnMark.setText(lblMark);
                    }
                    sortFilter.triggerSort();
                }
            }
        });
        model.getSelectedVideos().addListener((ListChangeListener<Video>)c -> {
            int numUnmarked = JkStreams.count(model.getSelectedVideos(), v -> !v.isMarked());
            btnMark.setText(numUnmarked == 0 ? lblUnmark : lblMark);
            int numUnhold = JkStreams.count(model.getSelectedVideos(), v -> !holdVideos.contains(v));
            btnHold.setText(numUnhold == 0 ? lblUnhold : lblHold);
        });
        Button btnAutoSnap = new Button("AUTOSNAP");
        btnAutoSnap.setOnAction(e -> {
            SimpleBooleanProperty finished = new SnapshotManager().runAutoSnap(model.getSelectedVideos());
            if(finished != null) {
                finished.addListener((obs,o,n) -> {
                    if(finished.get()){
                        tableVideos.refresh();
                    }
                });
            }
        });
        Button btnClearSnaps = new Button("DEL SNAPS");
        btnClearSnaps.setOnAction(e -> {
            model.getSelectedVideos().forEach(v -> {
                model.removeSnapshots(v);
                v.getSnapTimes().clear();
            });
            tableVideos.refresh();
            updateRightPane();
        });
        bpTop.setCenter(createHBox("spacing10 centered", btnHold, btnMark, btnAutoSnap, btnClearSnaps));

        Button btnDelete = new Button("DELETE");
        btnDelete.setOnAction(e -> {
            manageDelete(model.getSelectedVideos());
            updateRightPane();
        });
        bpTop.setRight(createHBox("centered", btnDelete));

        return vbox;
    }
    private void manageDelete(List<Video> videos) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(strf("Delete {} videos?", videos.size()));
        Optional<ButtonType> res = alert.showAndWait();
        if(res != null && res.isPresent() && res.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            Alert dlgWait = new Alert(Alert.AlertType.INFORMATION);
            dlgWait.getDialogPane().getButtonTypes().clear();
            dlgWait.setHeaderText(strf("Deleting {} videos", videos.size()));
            dlgWait.show();
            model.getVideos().removeAll(model.getSelectedVideos());
            dlgWait.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dlgWait.close();
            JfxUtil.alertInfo("Deleted {} videos", videos.size());
        }
    }

    private BorderPane createCenterBottomButtonsPane() {
        BorderPane bp = new BorderPane();

        // VBox PLAY
//        JfxVideoBuilder videoBuilder = new JfxVideoBuilder();
//        videoBuilder.setShowClose(false);
//        videoBuilder.setVisibleBtnCamera(true);
//        videoBuilder.setVisibleBtnMark(true);
//        videoBuilder.setDecoratedStage(true);
//        Button btnPlay = new Button("PLAY");
//        btnPlay.disableProperty().bind(Bindings.createBooleanBinding(() -> model.getSelectedVideos().size() != 1, model.getSelectedVideos()));
//        btnPlay.setOnAction(e -> {
//            JfxVideoStage vstage = videoBuilder.createStage();
//            vstage.setWidth(800);
//            vstage.setHeight(600);
//            vstage.getPlayerConfig().setCloseRunnable(() -> showedVideoStages.remove(vstage));
//            vstage.playVideo(model.toFxVideo(model.getSelectedVideos().get(0)));
//            showedVideoStages.add(vstage);
//        });
        ComboBox<VideoStagesPosition> combo = new ComboBox<>();
        combo.getItems().setAll(StagePosProvider.getVideoPosList());
        combo.setConverter(new StringConverter<VideoStagesPosition>() {
            @Override
            public String toString(VideoStagesPosition object) {
                return object.getName();
            }
            @Override
            public VideoStagesPosition fromString(String string) {
                return StagePosProvider.getStagesPosition(string);
            }
        });
        combo.getSelectionModel().selectFirst();
        Button btnDisplay = new Button("DISPLAY");
        btnDisplay.setOnAction(e -> {
            List<FxVideo> fxVideos = model.toFxVideos(model.getSelectedVideos());
            PanesSelector.getInstance().displayMultiVideoPane(combo.getValue(), fxVideos);
        });
        HBox hboxDisplay = createHBox("centered spacing10", combo, btnDisplay);

//        bp.setLeft(createHBox("", btnPlay));
        bp.setCenter(hboxDisplay);

        return bp;
    }

    private Pane createRightPane() {
        VBox box = createVBox("rightBox");

        // VBox CATEGORIES
        showCategories = new SimpleBooleanProperty(false);
        Button btnShowCats = new Button("SHOW CATEGORIES");
        btnShowCats.setStyle(strf("{}; -fx-padding: 0 10 0 10", btnShowCats.getStyle()));
        btnShowCats.setOnAction(e -> showCategories.set(!showCategories.get()));
        HBox hboxBtnShowCats = createHBox("centered", btnShowCats);
        gpCategories = new GridPane();
        VBox vboxCats = createVBox("subVBox", hboxBtnShowCats);
        box.getChildren().add(vboxCats);
        showCategories.addListener((obs, o, n) -> {
            ObservableList<Node> children = vboxCats.getChildren();
            if(n) {
                btnShowCats.setText("HIDE CATEGORIES");
                children.add(gpCategories);
                updateRightPane();
            } else {
                btnShowCats.setText("SHOW CATEGORIES");
                children.remove(gpCategories);
            }
        });

        // VBox SNAPSHOTS
        showSnapshots = new SimpleBooleanProperty(false);
        Button btnShowSnaps = new Button("SHOW SNAPS");
        btnShowSnaps.setOnAction(e -> showSnapshots.set(!showSnapshots.get()));
        HBox hboxBtnShoSnap = createHBox("centered", btnShowSnaps);
        scrollPaneSnaps = new ScrollPane();
        VBox vboxSnaps = createVBox("subVBox", hboxBtnShoSnap);
        box.getChildren().add(vboxSnaps);
        showSnapshots.addListener((obs, o, n) -> {
            ObservableList<Node> children = vboxSnaps.getChildren();
            if(n) {
                btnShowSnaps.setText("HIDE SNAPS");
                children.add(scrollPaneSnaps);
                updateRightPane();
            } else {
                btnShowSnaps.setText("SHOW SNAPS");
                children.remove(scrollPaneSnaps);
            }
        });

        model.getSelectedVideos().addListener((ListChangeListener<Video>)c -> updateRightPane());

        return box;
    }
    private void updateRightPane() {
        if(showSnapshots.get()) {
            scrollPaneSnaps.setContent(createSnapsPane(model.getSelectedVideos()));
        }
        if(showCategories.get()) {
            model.getSelectedVideos().stream().flatMap(v -> v.getCategories().stream());
            Set<Category> catSet = new TreeSet<>(JkStreams.flatMap(model.getSelectedVideos(), Video::getCategories));

            GridPaneBuilder builder = new GridPaneBuilder();
            int row = 0;
            for (Category category : catSet) {
                builder.add(row++, 0, category.getName());
            }
            builder.createGridPane(gpCategories);
        }
    }
    private Pane createSnapsPane(List<Video> videos) {
        List<FxSnapshot> snapshots = new ArrayList<>();

        final double totWidth = 400d;
        final int ncols = 3;

        if(videos.size() == 1) {
            Video video = videos.get(0);
            snapshots.addAll(model.getSnapshots(video));
        } else if(videos.size() <= 20){
            videos.forEach(v -> {
                List<FxSnapshot> stList = model.getSnapshots(v, ncols);
                snapshots.addAll(stList);
                int rem = ncols - stList.size();
                for(int i = 0; i < rem; i++)    snapshots.add(null);
            });
        }

        double ivWidth = totWidth / ncols;
        double ivHeight = ivWidth / 1.33;
        List<HBox> ivBoxList = map(snapshots, sn -> {
            if(sn == null)  return null;
            ImageView iv = createImageView(sn.getImage());
            HBox ivbox = new HBox(iv);
            ivbox.getStyleClass().addAll("bgBlack", "centered");
            ivbox.setPrefWidth(ivWidth);
            ivbox.setPrefHeight(ivHeight);
            iv.setFitWidth(ivbox.getPrefWidth());
            iv.setFitHeight(ivbox.getPrefHeight());
            return ivbox;
        });

        GridPaneBuilder gpBuilder = new GridPaneBuilder();
        for(int row = 0, index = 0; index < ivBoxList.size(); row++) {
            for(int col = 0; col < ncols && index < ivBoxList.size(); col++, index++) {
                if(ivBoxList.get(index) != null) {
                    gpBuilder.add(row, col, ivBoxList.get(index));
                }
            }
        }

        GridPane gp = gpBuilder.createGridPane();
        return createVBox("vboxSnapshots", gp);
    }

    private void fillGpCategFilter() {
        GridPaneBuilder gb = new GridPaneBuilder();
        List<Category> cats = JkStreams.sorted(model.getCategories());
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
            RadioButton radioSkip = new RadioButton("-");

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
            btnDelCat.setGraphic(createImageView(imgDelete, 20d, 20d));
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
        sortFilter.triggerSort();
    }

    private File lastAddFolder;
    private void actionAddVideos(ActionEvent event) {
        FileChooser fc = new FileChooser();
        File initial = lastAddFolder != null && lastAddFolder.isDirectory() ? lastAddFolder : JkEnvironment.getHomeFolder().resolve("Desktop").toFile();
        fc.setInitialDirectory(initial);
        fc.setTitle("Select videos");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP4", "*.mp4"));
        List<File> files = fc.showOpenMultipleDialog(JfxUtil.getWindow(event));
        if(files != null && !files.isEmpty()) {
            List<Path> paths = map(files, File::toPath);
            lastAddFolder = JkFiles.getParent(paths.get(0)).toFile();
            addNewVideos(paths);
        }
    }

    private void addNewVideos(List<Path> pathList) {
        Dialog<ButtonType> dlgForceAdd = new Dialog<>();
        dlgForceAdd.getDialogPane().getButtonTypes().add(ButtonType.YES);
        dlgForceAdd.getDialogPane().getButtonTypes().add(ButtonType.NO);
        dlgForceAdd.setHeaderText("Add files previously deleted?");
        Optional<ButtonType> bt = dlgForceAdd.showAndWait();
        boolean forceAdd = bt.isPresent() && bt.get() == ButtonType.YES;

//        Dialog dlgWait = new Dialog();
//        dlgWait.getDialogPane().getButtonTypes().clear();
//        dlgWait.setTitle(null);
//        dlgWait.setHeaderText(null);
//        dlgWait.setContentText(strf("Analyzing {} files", pathList.size()));
//        dlgWait.show();
//
//        pathList.forEach(p -> model.addVideoFile(p, !forceAdd));
//
//        dlgWait.getDialogPane().getButtonTypes().add(ButtonType.OK);
//        dlgWait.close();

        Alert dlgWait = new Alert(Alert.AlertType.INFORMATION);
        dlgWait.getDialogPane().getButtonTypes().clear();
        dlgWait.setHeaderText(strf("Analyzing {} files", pathList.size()));
        dlgWait.show();

//        pathList.forEach(p -> model.addVideoFile(p, !forceAdd));
        List<FxVideo> addedList = map(pathList, p -> model.addVideoFile(p, !forceAdd));
        addedList.removeIf(Objects::isNull);

        dlgWait.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dlgWait.close();

        JfxUtil.alertInfo("Added {}/{} videos", addedList.size(), pathList.size());
    }

    @Override
    public void closePane() {
        new ArrayList<>(showedVideoStages).forEach(JfxVideoStage::close);
        showedVideoStages.clear();
    }
}
