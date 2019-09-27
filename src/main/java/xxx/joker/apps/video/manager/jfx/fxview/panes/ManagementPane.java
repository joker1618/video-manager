package xxx.joker.apps.video.manager.jfx.fxview.panes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.fxmodel.FxModel;
import xxx.joker.apps.video.manager.jfx.fxmodel.FxSnapshot;
import xxx.joker.apps.video.manager.jfx.fxmodel.FxVideo;
import xxx.joker.apps.video.manager.jfx.fxview.gridpane.GridPaneBuilder;
import xxx.joker.apps.video.manager.jfx.fxview.managers.SnapshotManager;
import xxx.joker.apps.video.manager.jfx.fxview.provider.IconProvider;
import xxx.joker.apps.video.manager.jfx.fxview.videoplayer.JfxVideoBuilder;
import xxx.joker.apps.video.manager.jfx.fxview.videoplayer.JfxVideoPlayer;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.lambdas.JkStreams;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static xxx.joker.libs.core.javafx.JfxControls.*;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class ManagementPane extends BorderPane implements Closeable {

    private static Logger LOG = LoggerFactory.getLogger(ManagementPane.class);

    private FxModel model = FxModel.getModel();

    private ListView<Video> videoListView;
    private ObservableList<Video> videos;
    private SimpleIntegerProperty videoIndex;
    private SimpleObjectProperty<JfxVideoPlayer> showingPlayer;

    private VBox categoryBox;
    private VBox categoryBoxMulti;
    private Map<Category, CheckBox> checkBoxCategoryMap;
    private Map<Category, CheckBox> checkBoxCategoryMapMulti;
    private final JfxVideoBuilder vpBuilder;

    private ObservableList<JkDuration> obsSnapList = FXCollections.observableArrayList(new ArrayList<>());


    public ManagementPane(ObservableList<Video> videos) {
        this.videos = FXCollections.observableArrayList(videos);
        this.videoIndex = new SimpleIntegerProperty(-1);
        this.showingPlayer = new SimpleObjectProperty<>();

        this.vpBuilder = new JfxVideoBuilder();
        vpBuilder.setShowBorder(true);
        vpBuilder.setShowClose(false);
        vpBuilder.setVisibleBtnCamera(true);
        vpBuilder.setVisibleBtnMark(true);
        vpBuilder.setBtnCameraRunnable(this::updateSnapshotsPane);
        vpBuilder.setNextAction(e -> updateShowingVideo(videoIndex.get() + 1));
        vpBuilder.setPreviousAction(e -> updateShowingVideo(videoIndex.get() - 1));

        setLeft(createVideoListViewPane());
        setCenter(createPlayerPane());
        setRight(createSnapshotsPane());

        getStylesheets().add(getClass().getResource("/css/ManagementPane.css").toExternalForm());

    }

    private Pane createVideoListViewPane() {
        this.videoListView = new ListView<>();
        videoListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        videoListView.setCellFactory(param -> new ListCell<Video>() {
            @Override
            protected void updateItem(Video item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getTitle());
                String style = getStyle();
                if(item == null || item.getCategories().isEmpty()) {
                    style += "; -fx-text-fill: red";
                } else {
                    style += "; -fx-text-fill: black";
                }
                if(showingPlayer.get() != null && showingPlayer.get().getFxVideo().getVideo().equals(item)) {
                    style += "; -fx-font-weight: bold";
                } else {
                    style += "; -fx-font-weight: normal";
                }
                setStyle(style);
            }
        });

        videoListView.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                if(videoListView.getSelectionModel().getSelectedItems().size() == 1) {
                    int rowIdx = videoListView.getSelectionModel().getSelectedIndex();
                    videoListView.refresh();
                    updateShowingVideo(rowIdx);
                    updateSelectedCheckBoxes();
                    updateSelectedCheckBoxesMulti();
                }
            }
        });

        videoListView.setItems(videos);

        Pane optionFieldsPane = createOptionFieldsPane();

        return createVBox("leftBox", videoListView, optionFieldsPane);
    }
    private Pane createOptionFieldsPane() {
        VBox container = new VBox();
        container.getStyleClass().add("choosePane");

        // Categories check box
        categoryBox = new VBox();
        categoryBoxMulti = new VBox();
        checkBoxCategoryMap = new TreeMap<>();
        checkBoxCategoryMapMulti = new TreeMap<>();
        categoryBox.getStyleClass().addAll("boxCategories");
        categoryBoxMulti.getStyleClass().addAll("boxCategories");
        updateCategoriesCheckBoxes();
        updateCategoriesCheckBoxesMulti();

        Label lblSingle = new Label("SINGLE");
        lblSingle.getStyleClass().add("lblTitle");
        BorderPane bp1 = new BorderPane(categoryBox, lblSingle, null, null, null);
        Label lblMultiNumSel = new Label("MULTI (0)");
        lblMultiNumSel.getStyleClass().add("lblTitle");
        ObservableList<Video> selItems = videoListView.getSelectionModel().getSelectedItems();
        selItems.addListener(
                (ListChangeListener<? super Video>) c -> {
                    lblMultiNumSel.setText(strf("MULTI ({})", selItems.size()));
                    updateSelectedCheckBoxesMulti();
                });
        BorderPane bp2 = new BorderPane(categoryBoxMulti, lblMultiNumSel, null, null, null);
        Arrays.asList(bp1 ,bp2).forEach(bpx -> bpx.getStyleClass().add("bpCatCheckBoxes"));
        HBox hb = new HBox(bp1, bp2);
        hb.getStyleClass().addAll("catCont");

        ScrollPane scrollPane = new ScrollPane(hb);
        scrollPane.getStyleClass().add("scrollPaneCategories");
        container.getChildren().addAll(scrollPane);

        // Button add category
        Button btnAddCategory = new Button("ADD CATEGORY");
        btnAddCategory.setOnAction(e -> actionAddCategory());
        HBox btnBox = new HBox(btnAddCategory);
        btnBox.getStyleClass().add("boxButtons");
        container.getChildren().add(btnBox);

        model.getCategories().addListener((ListChangeListener<Category>)c -> {
            updateCategoriesCheckBoxes();
            updateCategoriesCheckBoxesMulti();
        });

        return container;
    }

    private Pane createPlayerPane() {
        TextField txtTitle = new TextField();
        txtTitle.setDisable(true);
        Button btnChangeTitle = new Button("CHANGE");
        btnChangeTitle.disableProperty().bind(Bindings.createBooleanBinding(
                () -> StringUtils.isBlank(txtTitle.getText()) || txtTitle.getText().trim().equals(showingPlayer.get() == null ? "" : showingPlayer.get().getFxVideo().getVideo().getTitle()),
                txtTitle.textProperty()
        ));
        btnChangeTitle.setOnAction(e -> changeVideoTitle(txtTitle.getText().trim()));
        HBox hboxChangeTitle = createHBox("subBox titleBox", new Label("Video title:"), txtTitle, btnChangeTitle);

        Button btnAutoSnap = new Button("AUTOSNAP");
        btnAutoSnap.setOnAction(e -> manageAutoSnap(Collections.singletonList(showingPlayer.get().getFxVideo().getVideo())));
        btnAutoSnap.disableProperty().bind(showingPlayer.isNull());

        Button btnClearSnaps = new Button("CLEAR SNAPS");
        btnClearSnaps.disableProperty().bind(showingPlayer.isNull());
        btnClearSnaps.setOnAction(e -> {
            Video v = showingPlayer.get().getFxVideo().getVideo();
            v.getSnapTimes().forEach(st -> model.removeSnapshot(v, st));
            v.getSnapTimes().clear();
            model.persistData();
            updateSnapshotsPane();
        });
        Button btnDelete = new Button("DELETE");
        btnDelete.disableProperty().bind(showingPlayer.isNull());
        btnDelete.setOnAction(e -> {
            Video video = showingPlayer.get().getFxVideo().getVideo();
            updateShowingVideo(videoIndex.get() + 1);
            model.getVideos().remove(video);
            obsSnapList.clear();
            videoListView.getItems().remove(video);
        });
        HBox snapSubBox = createHBox("subBox", btnAutoSnap, btnClearSnaps);

        BorderPane bpTop = new BorderPane();
        bpTop.getStyleClass().addAll("topBox");
        bpTop.setTop(hboxChangeTitle);
        bpTop.setLeft(snapSubBox);
        bpTop.setRight(btnDelete);

        BorderPane bp = new BorderPane();
        bp.getStyleClass().addAll("centerBox");
        bp.setTop(bpTop);
        showingPlayer.addListener((observable, oldValue, newValue) -> {
            bp.setCenter(newValue);
            txtTitle.setDisable(newValue == null);
            if(newValue != null) {
                updateSelectedCheckBoxes();
                updateSelectedCheckBoxesMulti();
                txtTitle.setText(newValue.getFxVideo().getVideo().getTitle());
            } else {
                txtTitle.setText("");
            }
        });

        return bp;
    }
    private void changeVideoTitle(String newVideoTitle) {
        JfxVideoPlayer vp = showingPlayer.get();
        String oldTitle = vp.getFxVideo().getVideo().getTitle();
        if(!newVideoTitle.equals(oldTitle)) {
            vp.getFxVideo().getVideo().setTitle(newVideoTitle);
            vp.setPlayerCaption(newVideoTitle);
            videoListView.refresh();
            LOG.info("Changed title from [{}] to [{}]", oldTitle, newVideoTitle);
        }
    }

    private void updateSnapshotsPane() {
        Set<JkDuration> snapTimes = new TreeSet<>();
        if(showingPlayer.get() != null) {
            snapTimes.addAll(showingPlayer.get().getFxVideo().getVideo().getSnapTimes());
        }
        obsSnapList.setAll(snapTimes);
    }
    private Node createSnapshotsPane() {
        BorderPane toRet = new BorderPane();
        toRet.getStyleClass().add("rightBox");

        Label lblTitle = new Label();
        toRet.setTop(createHBox("btop", lblTitle));

        Image imgDelete = new IconProvider().getIconImage(IconProvider.DELETE_RED);
        obsSnapList.addListener((ListChangeListener<JkDuration>)c -> {
            lblTitle.setText(strf("Snapshots ({})", obsSnapList.size()));

            Video video = showingPlayer.get().getFxVideo().getVideo();
            GridPaneBuilder gpBuilder = new GridPaneBuilder();
            int numCols = 3;
            int counter = 0;
            double width = 160;
            double height = width / 1.33;
            for(int row = 0; counter < obsSnapList.size(); row++) {
                for(int col = 0; col < numCols && counter < obsSnapList.size(); col++, counter++) {
                    int index = row * numCols + col;
                    JkDuration dur = obsSnapList.get(index);
                    FxSnapshot snap = model.getSnapshot(video, dur);
                    ImageView ivSnap = createImageView(snap.getImage(), width, height);
                    ivSnap.setOnMouseClicked(e -> showingPlayer.get().getMediaView().getMediaPlayer().seek(Duration.millis(dur.toMillis())));
                    HBox ivbox = new HBox(ivSnap);
                    ivbox.getStyleClass().addAll("bgBlack", "centered");
                    ivbox.setPrefWidth(width);
                    ivbox.setPrefHeight(height);
                    ivSnap.setFitWidth(ivbox.getPrefWidth());
                    ivSnap.setFitHeight(ivbox.getPrefHeight());
                    ImageView ivDel = createImageView(imgDelete, 20, 20);
                    Button btnDel = new Button(null, ivDel);
                    btnDel.setStyle("-fx-padding: 0; -fx-pref-width: " + width);
                    btnDel.setOnAction(e -> {
                        video.getSnapTimes().remove(dur);
                        model.removeSnapshot(video, dur);
                        updateSnapshotsPane();
                    });
                    gpBuilder.add(row, col, createVBox("centered", ivbox, btnDel));
                }
            }
            ScrollPane sp = new ScrollPane(gpBuilder.createGridPane());
            toRet.setCenter(sp);
        });
        showingPlayer.addListener((obs,o,n) -> { if(n != null) updateSnapshotsPane(); });

        return toRet;
    }

    private void actionAddCategory() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setContentText("New category name:");
        dlg.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<String> opt = dlg.showAndWait();
        if(opt.isPresent()) {
            String trimmed = StringUtils.trim(opt.get());
            if(StringUtils.isNotBlank(trimmed) && JkStreams.filter(model.getCategories(), cat -> cat.getName().equalsIgnoreCase(trimmed)).isEmpty()) {
                Category cat = new Category(trimmed);
                model.getCategories().add(cat);
                updateCategoriesCheckBoxes();
                updateCategoriesCheckBoxesMulti();
                if(showingPlayer.get() != null){
                    showingPlayer.getValue().getFxVideo().getVideo().getCategories().add(cat);
                    updateSelectedCheckBoxes();
                    updateSelectedCheckBoxesMulti();
                }
            }
        }
    }

    private void updateShowingVideo(int idx) {
        int newIndex = Math.max(Math.min(idx, videos.size()), -1);
        videoIndex.setValue(newIndex);
        JfxVideoPlayer videoPlayer = showingPlayer.getValue();
        if (videoPlayer != null) {
            videoPlayer.closePlayer();
        }
        if(newIndex >= 0 && newIndex < videos.size()) {
            Video video = JkStreams.filter(model.getVideos(), v -> videos.get(newIndex).getTitle().equals(v.getTitle())).get(0);
            FxVideo FxVideo = model.getFxVideo(video);
            JfxVideoPlayer vp = vpBuilder.createPane(FxVideo);
            vp.play();
            showingPlayer.setValue(vp);
        } else {
            showingPlayer.setValue(null);
        }
        videoListView.refresh();
    }
    
    private void updateCategoriesCheckBoxes() {
        checkBoxCategoryMap.entrySet().removeIf(e -> !model.getCategories().contains(e.getKey()));
        for(Category cat : model.getCategories()) {
            if(!checkBoxCategoryMap.containsKey(cat)) {
                CheckBox cb = new CheckBox(cat.getName());
                cb.setOnAction(e -> actionSetVideoCategory(e, cat));
                cb.disableProperty().bind(showingPlayer.isNull());
                checkBoxCategoryMap.put(cat, cb);
            }
        }
        categoryBox.getChildren().clear();
        categoryBox.getChildren().addAll(checkBoxCategoryMap.values());
    }
    private void updateCategoriesCheckBoxesMulti() {
        checkBoxCategoryMapMulti.entrySet().removeIf(e -> !model.getCategories().contains(e.getKey()));
        for(Category cat : model.getCategories()) {
            if(!checkBoxCategoryMapMulti.containsKey(cat)) {
                CheckBox cb = new CheckBox(cat.getName());
                cb.setOnAction(e -> actionSetMultiVideoCategory(e, cat));
                ObservableList<Video> selItems = videoListView.getSelectionModel().getSelectedItems();
                cb.disableProperty().bind(Bindings.createBooleanBinding(selItems::isEmpty, selItems));
                checkBoxCategoryMapMulti.put(cat, cb);
            }
        }
        categoryBoxMulti.getChildren().clear();
        categoryBoxMulti.getChildren().addAll(checkBoxCategoryMapMulti.values());
    }

    private void updateSelectedCheckBoxes() {
        if(showingPlayer.getValue() != null) {
            FxVideo video = showingPlayer.getValue().getFxVideo();
            for (Category cat : model.getCategories()) {
                checkBoxCategoryMap.get(cat).setSelected(video.getVideo().getCategories().contains(cat));
            }
        }
    }
    private void updateSelectedCheckBoxesMulti() {
        ObservableList<Video> sel = videoListView.getSelectionModel().getSelectedItems();
        for (Category cat : model.getCategories()) {
            checkBoxCategoryMapMulti.get(cat).setIndeterminate(false);
            checkBoxCategoryMapMulti.get(cat).setSelected(false);
            int num = JkStreams.filter(sel, Objects::nonNull, v -> v.getCategories().contains(cat)).size();
            if(sel.isEmpty()) {
                checkBoxCategoryMapMulti.get(cat).setSelected(false);
            } else if(num == sel.size()) {
                checkBoxCategoryMapMulti.get(cat).setSelected(true);
            } else if(num > 0){
                checkBoxCategoryMapMulti.get(cat).setIndeterminate(true);
            }
        }
    }

    private void actionSetVideoCategory(ActionEvent event, Category category) {
        setVideoCategory(event, category, showingPlayer.getValue().getFxVideo().getVideo());
        updateSelectedCheckBoxesMulti();
        videoListView.refresh();
    }
    private void actionSetMultiVideoCategory(ActionEvent event, Category category) {
        videoListView.getSelectionModel().getSelectedItems().forEach(v -> setVideoCategory(event, category, v));
        updateSelectedCheckBoxes();
        videoListView.refresh();
    }
    private void setVideoCategory(ActionEvent event, Category category, Video video) {
        CheckBox source = (CheckBox) event.getSource();
        if(source.isSelected()) {
            video.getCategories().add(category);
        } else {
            video.getCategories().remove(category);
        }
    }
    
    private void manageAutoSnap(Collection<Video> videos) {
        AtomicBoolean doPlay = new AtomicBoolean(false);
        SimpleObjectProperty<MediaPlayer> mp = new SimpleObjectProperty<>();
        if(showingPlayer.get() != null) {
            mp.set(showingPlayer.get().getMediaView().getMediaPlayer());
            doPlay.set(mp.get().getStatus() == MediaPlayer.Status.PLAYING);
        }
        if(doPlay.get())  mp.get().pause();
        SimpleBooleanProperty finished = new SnapshotManager().runAutoSnap(videos);
        if(finished != null) {
            finished.addListener((obs,o,n) -> {
                if(n) {
                    updateSnapshotsPane();
                    if(doPlay.get())  mp.get().play();
                }
            });
        }
    }

    @Override
    public void closePane() {
        JfxVideoPlayer videoPlayer = showingPlayer.getValue();
        if (videoPlayer != null) {
            videoPlayer.closePlayer();
        }
        LOG.debug("Closed management pane");
    }
}
