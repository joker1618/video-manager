package xxx.joker.apps.video.manager.jfx.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.datalayer.VideoRepo;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.fxlayer.fxview.controls.JfxTable;
import xxx.joker.apps.video.manager.fxlayer.fxview.controls.JfxTableCol;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.FxVideo;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoBuilder;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoStage;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.apps.video.manager.jfx.model.beans.SortFilter;
import xxx.joker.apps.video.manager.main.SceneManager;
import xxx.joker.apps.video.manager.provider.StagePosProvider;
import xxx.joker.libs.core.datetime.JkDateTime;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.enums.JkSizeUnit;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.files.JkEncryption;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.format.JkOutput;
import xxx.joker.libs.core.javafx.JfxUtil;
import xxx.joker.libs.core.lambdas.JkStreams;
import xxx.joker.libs.core.utils.JkBytes;
import xxx.joker.libs.datalayer.entities.RepoResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static xxx.joker.libs.core.utils.JkConsole.display;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class HomepagePane extends BorderPane implements CloseablePane {

	private static final Logger logger = LoggerFactory.getLogger(HomepagePane.class);

	private final VideoModel model = VideoModelImpl.getInstance();

	private SortFilter sortFilter;
	private GridPane gridPaneFilterCat;
	private JfxTable<Video> tview;

	private Map<String,Pair<Label,ToggleGroup>> toggleMap = new TreeMap<>(String::compareToIgnoreCase);

	public HomepagePane() {
		createPane();
	}

	public void refreshView() {
		fillGridPaneCategFilter();
		tview.refresh();
	}

	private void createPane() {
		// left -> sort & filter
		setLeft(createLeftPane());

		// center -> table videos & videos stats
		setCenter(createCenterPane());

		// right --> buttons
		setRight(createRightPane());

		getStylesheets().add(getClass().getResource("/css/HomepagePane.css").toExternalForm());
	}

	private Pane createLeftPane() {
		sortFilter = new SortFilter();

		VBox box = new VBox();
		box.getStyleClass().add("leftBox");

		HBox boxCaption = new HBox(new Label("FILTER"));
		boxCaption.getStyleClass().add("boxCaption");
		box.getChildren().add(boxCaption);

		TextField nameFilter = new TextField("");
		sortFilter.videoNameProperty().bind(nameFilter.textProperty());
        HBox boxNameFilter = new HBox(nameFilter);
        boxNameFilter.getStyleClass().addAll("centeredBox");
        box.getChildren().add(boxNameFilter);

		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("gridPane");
		addRadioLine(gridPane, "Cataloged:", 0, sortFilter::setCataloged, false);
		box.getChildren().add(gridPane);

		gridPaneFilterCat = new GridPane();
		gridPaneFilterCat.getStyleClass().add("gridPane");
		fillGridPaneCategFilter();
		ScrollPane scrollPane = new ScrollPane(gridPaneFilterCat);
		scrollPane.getStyleClass().add("scrollPaneCateg");
		box.getChildren().add(scrollPane);

		Button button = new Button("CLEAR");
		button.setOnAction(e -> clearFilterRadios());
		HBox hbox = new HBox(button);
		hbox.getStyleClass().addAll("centeredBox", "boxClearOpt");
		box.getChildren().add(hbox);

		BorderPane bp = new BorderPane();
		bp.setCenter(box);
		bp.getStyleClass().add("leftBox");

		GridPane gp = new GridPane();
		bp.setBottom(gp);
		gp.getStyleClass().add("paneDetails");

		int rowNum = 0;

		Label fixedNum = new Label("");
		fixedNum.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(model.getVideos().size()), model.getVideos()));
		Label selNum = new Label("");
		selNum.textProperty().bind(Bindings.createStringBinding(() -> model.getSelectedVideos().size()+"", model.getSelectedVideos()));
		addDetailsLine(gp, rowNum, "Num videos", fixedNum, selNum);
		rowNum++;

		Label fixedLength = new Label("");
		fixedLength.textProperty().bind(Bindings.createStringBinding(() -> JkDuration.of(model.getVideos().stream().mapToLong(v -> v.getLength() == null ? 0L : v.getLength().toMillis()).sum()).toStringElapsed(false), model.getVideos()));
		Label selLength = new Label("");
		selLength.textProperty().bind(Bindings.createStringBinding(() -> JkDuration.of(model.getSelectedVideos().stream().filter(Objects::nonNull).mapToLong(v -> v.getLength() == null ? 0L : v.getLength().toMillis()).sum()).toStringElapsed(false), model.getSelectedVideos()));
		addDetailsLine(gp, rowNum, "Total length:", fixedLength, selLength);
		rowNum++;

		Label fixedSize = new Label("");
		fixedSize.textProperty().bind(Bindings.createStringBinding(() -> JkOutput.humanSize(model.getVideos().stream().mapToLong(Video::getSize).sum()), model.getVideos()));
		Label selSize = new Label("");
		selSize.textProperty().bind(Bindings.createStringBinding(() -> JkOutput.humanSize(model.getSelectedVideos().stream().filter(Objects::nonNull).mapToLong(Video::getSize).sum()), model.getSelectedVideos()));
		addDetailsLine(gp, rowNum, "Total size:", fixedSize, selSize);
		rowNum++;

		return bp;
//		return box;
	}
	private void fillGridPaneCategFilter() {
		gridPaneFilterCat.getChildren().clear();
		ObservableList<Category> cats = model.getCategories();
		for(int i = 0; i < cats.size(); i++) {
			addRadioLine(gridPaneFilterCat, cats.get(i), i);
		}
	}

	private void clearFilterRadios() {
		toggleMap.values().forEach(
			tg -> tg.getValue().selectToggle(tg.getValue().getToggles().get(tg.getValue().getToggles().size()-1))
		);
	}

	private void addRadioLine(GridPane gridPane, Category cat, int row) {
		addRadioLine(gridPane, cat.getName(), row, b -> sortFilter.setCategory(cat, b), true);
	}
	private void addRadioLine(GridPane gridPane, String catName, int row, Consumer<Boolean> setter, boolean showDelBtn) {
		Pair<Label, ToggleGroup> pair = toggleMap.get(catName);

		if(pair == null) {
			Label lbl = new Label(catName);

			RadioButton radioYes = new RadioButton("Y");
			RadioButton radioNo = new RadioButton("N");
			RadioButton radioSkip = new RadioButton("Skip");

			ToggleGroup tg = new ToggleGroup();
			tg.getToggles().addAll(radioYes, radioNo, radioSkip);
			tg.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == radioYes) {
					setter.accept(true);
					lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: BLACK");
				} else if (newValue == radioNo) {
					setter.accept(false);
					lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: RED");
				} else if (newValue == radioSkip) {
					setter.accept(null);
					lbl.setStyle("-fx-font-weight: normal; -fx-text-fill: BLACK");
				}
			});

			tg.selectToggle(radioSkip);
			pair = Pair.of(lbl, tg);
			toggleMap.put(catName, pair);
		}

		int col = 0;
		gridPane.add(pair.getKey(), col++, row);
		for (Toggle toggle : pair.getValue().getToggles()) {
			gridPane.add((RadioButton)toggle, col++, row);
		}
		if(showDelBtn) {
			Button btnDelCat = new Button("delete");
			gridPane.add(btnDelCat, col++, row);
			btnDelCat.setOnAction(e -> {
				model.getVideos().forEach(v -> v.getCategories().removeIf(c -> c.getName().equals(catName)));
				model.getCategories().removeIf(c -> c.getName().equals(catName));
				model.persistData();
				refreshView();
			});
		}
	}

	private Pane createCenterPane() {
		tview = new JfxTable<>();
		tview.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		tview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		JfxTableCol<Video, String> tcolTitle = JfxTableCol.createCol("VIDEO", "title");
		tview.getColumns().add(tcolTitle);
		tcolTitle.setMinWidth(500);

		JfxTableCol<Video, Long> tcolSize = JfxTableCol.createCol("SIZE", "size", l -> JkOutput.humanSize(l, JkSizeUnit.MB, false));
		tview.getColumns().add(tcolSize);
		tcolSize.setMinWidth(100);

		JfxTableCol<Video,Integer> tcolWidth = JfxTableCol.createCol("W", "width");
		tview.getColumns().add(tcolWidth);
		tcolWidth.setMinWidth(80);

		JfxTableCol<Video,Integer> tcolHeight = JfxTableCol.createCol("H", "height");
		tview.getColumns().add(tcolHeight);
		tcolHeight.setMinWidth(80);

		JfxTableCol<Video,Double> tcolResolution = JfxTableCol.createCol("W/H", v -> v.getHeight() == null ? 0d : (double)v.getWidth()/v.getHeight(), d -> strf("%.2f", d));
		tview.getColumns().add(tcolResolution);
		tcolResolution.setMinWidth(80);

		JfxTableCol<Video, JkDuration> tcolLength = JfxTableCol.createCol("LENGTH", "length", d -> d == null ? "" : d.toStringElapsed(false));
		tview.getColumns().add(tcolLength);
		tcolLength.setMinWidth(80);

		JfxTableCol<Video,Integer> tcolPlayTimes = JfxTableCol.createCol("N.PLAY", "playTimes");
		tview.getColumns().add(tcolPlayTimes);
		tcolPlayTimes.setMinWidth(80);

		JfxTableCol<Video,Integer> tcolNumSnapshots = JfxTableCol.createCol("SNAP", param -> model.findSnapshots(param).size());
		tview.getColumns().add(tcolNumSnapshots);
		tcolPlayTimes.setMinWidth(80);

		JfxTableCol<Video, JkDateTime> tcolCreationTm = JfxTableCol.createCol("CREATION", "creationTm", null, jdt -> jdt == null ? "" : jdt.format("dd/MM/yyyy   HH:mm:ss"));
		tview.getColumns().add(tcolCreationTm);
		tcolCreationTm.setMinWidth(200);

		// Center all columns but the first one
		tview.getColumns().subList(1, tview.getColumns().size()).forEach(col -> col.getStyleClass().add("centered"));

		FilteredList<Video> filteredList = new FilteredList<>(model.getVideos());
		filteredList.predicateProperty().bind(sortFilter);

		SortedList<Video> tableItems = new SortedList<>(filteredList);
		tableItems.comparatorProperty().bind(tview.comparatorProperty());
		tview.setItems(tableItems);

		VBox vbox = new VBox(tview);
		vbox.getStyleClass().add("centerBox");
		VBox.setVgrow(tview, Priority.ALWAYS);

		Consumer<Object> selEvent = obj -> {
			ObservableList<Video> sitems = tview.getSelectionModel().getSelectedItems();
			model.getSelectedVideos().setAll(sitems.isEmpty() ? tableItems : sitems);
		};
		tview.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super Video>) c -> selEvent.accept(c));
		tableItems.addListener((ListChangeListener<? super Video>) c -> selEvent.accept(c));

		vbox.getChildren().add(createDetailsSection());

		model.getSelectedVideos().setAll(tableItems);

		return vbox;
	}
	private Pane createDetailsSection() {
		BorderPane bp = new BorderPane();
		bp.getStyleClass().addAll("snapshotsPane");

		HBox hbox = new HBox();
		hbox.getStyleClass().addAll("spacing20");
        ScrollPane sp = new ScrollPane(hbox);
		bp.setCenter(sp);

        model.getSelectedVideos().addListener((ListChangeListener<Video>)c -> fillSnapshotsPane(hbox));

		return bp;
	}
	private void fillSnapshotsPane(HBox hbox) {
        Image imgDelete = new Image(getClass().getResource("/icons/delete.png").toExternalForm());

        hbox.getChildren().clear();
        if(model.getSelectedVideos().size() == 1) {
            Video video = model.getSelectedVideos().get(0);
            List<Path> snapPaths = model.findSnapshots(video);
            try {
                for (int i = 0; i < snapPaths.size(); i++) {
                    Path path = snapPaths.get(i);
                    String url = path.toUri().toURL().toExternalForm();
                    Image snap = new Image(url);
                    ImageView imageView = new ImageView(snap);
                    imageView.setPreserveRatio(true);
                    int fw = 165;
                    int fh = 100;
					HBox ivbox = new HBox(imageView);
					ivbox.getStyleClass().addAll("bgBlack", "centered");
					ivbox.setPrefWidth(fw);
					ivbox.setPrefHeight(fh);
					imageView.setFitWidth(ivbox.getPrefWidth());
                    imageView.setFitHeight(ivbox.getPrefHeight());

                    Button btnDelSnapshot = new Button();
                    btnDelSnapshot.getStyleClass().addAll("pad0");
                    ImageView iv = new ImageView(imgDelete);
                    iv.setPreserveRatio(false);
                    int delSize = 30;
                    iv.setFitWidth(delSize);
                    iv.setFitHeight(delSize);
                    btnDelSnapshot.setGraphic(iv);
                    btnDelSnapshot.setOnAction(e -> {
                        JkFiles.delete(path);
                        tview.refresh();
                        fillSnapshotsPane(hbox);
                    });

                    VBox vBox = new VBox(ivbox, btnDelSnapshot);
                    vBox.getStyleClass().addAll("spacing5", "centered");

                    hbox.getChildren().add(vBox);
                }
            } catch(Exception e) {
                throw new JkRuntimeException(e);
            }
        }
    }
	private void addDetailsLine(GridPane gp, int row, String label, Label fixedLabel, Label selLabel) {
		gp.add(new Label(label), 0, row);
		gp.add(fixedLabel, 1, row);
		gp.add(selLabel, 2, row);
	}

	private Pane createRightPane() {
		VBox box = new VBox();
		box.getStyleClass().addAll("rightBox", "topCenter");

		Button btnMark = new Button("MARK VIDEOS");
		Category cat = new Category("MARKED");
		List<Category> res = JkStreams.filter(model.getCategories(), cat::equals);
		if(res.isEmpty()) {
			model.getCategories().add(cat);
		} else {
			cat = res.get(0);
		}
		Category markCat = cat;
		btnMark.setOnAction(e -> {
			AtomicBoolean b = new AtomicBoolean(false);
			model.getSelectedVideos().forEach(v -> {
				boolean added = v.getCategories().add(markCat);
				b.set(b.get() || added);
			});
			if(b.get())		fillGridPaneCategFilter();
		});
		box.getChildren().add(btnMark);

		Button btnUnmark = new Button("UNMARK VIDEOS");
		btnUnmark.setOnAction(e -> {
			AtomicBoolean b = new AtomicBoolean(false);
			model.getSelectedVideos().forEach(v -> {
				boolean removed = v.getCategories().remove(markCat);
				b.set(b.get() || removed);
			});
			if(b.get())		fillGridPaneCategFilter();
		});
		box.getChildren().add(btnUnmark);

		Button btnAddVideos = new Button("ADD VIDEOS");
		btnAddVideos.setOnAction(this::actionAddVideos);
		box.getChildren().add(btnAddVideos);

		Button btnGoToCategorizeVideo = new Button("CATEGORIZE VIDEOS");
		btnGoToCategorizeVideo.setOnAction(e -> SceneManager.displayCatalogVideo());
		box.getChildren().add(btnGoToCategorizeVideo);

		Button btnTryer = new Button("TRYER");
		btnTryer.setOnAction(e -> tryer());
		box.getChildren().add(btnTryer);

		VBox vbox = new VBox();
		vbox.getStyleClass().addAll("boxPlayVideos", "borderBlack1", "pad10", "centered");

		Button btnPlay = new Button("PLAY");
		btnPlay.setOnAction(e -> SceneManager.displayMultiVideos());
		btnPlay.disableProperty().bind(Bindings.createBooleanBinding(() -> model.getSelectedVideos().isEmpty(), model.getSelectedVideos()));
		HBox hbox = new HBox(btnPlay);
		hbox.getStyleClass().add("centeredBox");

		List<String> names = StagePosProvider.getPositionNames();
		ToggleGroup toggleGroup = new ToggleGroup();
		for(int i = 0; i < names.size(); i++) {
			RadioButton rb = new RadioButton(names.get(i));
			toggleGroup.getToggles().add(rb);
			vbox.getChildren().add(rb);
		}
		vbox.getChildren().add(hbox);

		toggleGroup.selectedToggleProperty().addListener(
			(observable, oldValue, newValue) -> model.getPlayOptions().setMultiVideoName(((RadioButton)newValue).getText())
		);
		if(!toggleGroup.getToggles().isEmpty()) {
			toggleGroup.selectToggle(toggleGroup.getToggles().get(0));
		}

		box.getChildren().add(vbox);

		Button btnAutoSnap = new Button("AUTO SNAPSHOTS");
		btnAutoSnap.setOnAction(e -> {
			JkVideoBuilder pbuilder = new JkVideoBuilder();
			pbuilder.setDecoratedStage(false);
			pbuilder.setShowBorder(true);
			pbuilder.setShowClose(true);
			JkVideoStage autoStage = pbuilder.createStage();
			autoStage.setMaximized(true);
			runAutoSnapshots(autoStage, new ArrayList<>(model.getSelectedVideos()));
		});
		box.getChildren().add(btnAutoSnap);

		return box;
	}

	private void runAutoSnapshots(JkVideoStage stage, List<Video> videos) {
		if(videos.isEmpty()) {
			stage.close();
			tview.refresh();
		} else {
			Video video = videos.remove(0);

			RepoResource res = model.getRepo().getResource(video.getMd5(), "videoz");

			stage.playVideo(new FxVideo(video, res.getPath()));
			MediaView mv = stage.getVideoPlayer().getMediaView();
			MediaPlayer mp = mv.getMediaPlayer();
			List<Long> times = new ArrayList<>();
			mp.totalDurationProperty().addListener((obs,o,n) -> {
				if(times.isEmpty()) {
					long tot = (long) mp.getTotalDuration().toMillis();
					tot -= 8 * 1000;
					int numSnapMin = 7;
					int minMinutesMs = 7 * 60 * 1000;
					if(tot < minMinutesMs) {
						for(int i = 0; i < numSnapMin; i++) {
							times.add((i+1) * tot / numSnapMin);
						}
					} else {
						long ns = tot / (60 * 1000);
						for(int i = 0; i < ns; i++) {
							times.add(1000L * 60 * (i+1));
						}
					}
				}
				mp.play();
				mp.seek(Duration.millis(times.get(0) - 200));
			});
			mp.currentTimeProperty().addListener((obs,o,n) -> {
				synchronized (times) {
					if(!times.isEmpty()) {
						boolean doSnap = times.get(0) <= n.toMillis();
						if(doSnap) {
							times.remove(0);
							stage.getVideoPlayer().takeVideoSnapshot(500);
							double seekMs;
							if(times.isEmpty()) {
								runAutoSnapshots(stage, videos);
							} else {
								seekMs = times.get(0) - 200;
								mp.seek(Duration.millis(seekMs));
							}
						}
					}
				}
			});
		}
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
		dlg.setContentText(strf("Analyzing %d files", pathList.size()));
		dlg.show();

		for(Path p : pathList) {
			try {
				Video v = createFromPath(p);
				boolean addVideo = JkStreams.filter(model.getVideos(), vi -> vi.getMd5().equals(v.getMd5())).isEmpty();
				if(addVideo) {
					VideoRepo repo = model.getRepo();
					repo.add(v);
					RepoResource res = repo.addResource(p, v.getMd5(), "videoz");
					fillLengthWidthHeight(v);
					model.getVideos().add(v);
					logger.info("New video added {}", res.getPath());
				} else {
					logger.info("Skip add for video {}: already exists", p);
				}
			} catch (Exception e) {
				logger.error("Unable to parse video from {}\n{}", p, e);
			}
		}

		dlg.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dlg.close();
	}

	private Video createFromPath(Path path) throws IOException {
		Video video = new Video();
		video.setMd5(JkEncryption.getMD5(path));
		video.setTitle(JkFiles.getFileName(path));
		video.setSize(Files.size(path));
		return video;
	}

	@Override
	public void closePane() {

	}

	private void fillLengthWidthHeight(Video video) {
		MediaView mv = new MediaView();
		Path videoPath = model.getRepo().getResource(video.getMd5(), "videoz").getPath();
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
		iprop.addListener((obs,o,n) -> { if(n.intValue() == 3) { mediaPlayer.stop(); mediaPlayer.dispose(); tview.refresh(); }});
		mediaPlayer.play();
	}

	private void tryer() {
		MediaView mv = new MediaView();
		Video video = model.getVideos().get(0);
		Path videoPath = model.getRepo().getResource(video.getMd5(), "videoz").getPath();
		Media media = new Media(JkFiles.toURL(videoPath));
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setAutoPlay(false);
		mediaPlayer.setCycleCount(1);
		mediaPlayer.setVolume(0d);
		mv.setMediaPlayer(mediaPlayer);
		AtomicInteger aint = new AtomicInteger(0);
		SimpleIntegerProperty iprop = new SimpleIntegerProperty(0);
		mediaPlayer.totalDurationProperty().addListener((obs,o,n) -> {display("XCHANGED totalDuration: {}", JkDuration.of(n).toStringElapsed()); iprop.set(aint.incrementAndGet());});
		media.widthProperty().addListener((obs,o,n) -> {display("XCHANGED media width: {}", n.doubleValue()); iprop.set(aint.incrementAndGet());});
		media.heightProperty().addListener((obs,o,n) -> {display("XCHANGED media height: {}", n.doubleValue()); iprop.set(aint.incrementAndGet());});
		iprop.addListener((obs,o,n) -> { if(n.intValue() == 3) { mediaPlayer.stop(); mediaPlayer.dispose(); display("DISPOSED");}});
		mediaPlayer.play();
	}
}
