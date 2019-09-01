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
import javafx.stage.FileChooser;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.apps.video.manager.jfx.model.beans.SortFilter;
import xxx.joker.apps.video.manager.main.SceneManager;
import xxx.joker.apps.video.manager.provider.StagePosProvider;
import xxx.joker.libs.core.datetime.JkTime;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.format.JkOutputFmt;
import xxx.joker.libs.core.format.JkSizeUnit;
import xxx.joker.libs.javafx.JkFxUtil;
import xxx.joker.libs.core.utils.JkFiles;
import xxx.joker.libs.core.utils.JkStreams;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static xxx.joker.libs.core.utils.JkStrings.strf;

public class HomepagePane extends BorderPane implements CloseablePane {

	private static final Logger logger = LoggerFactory.getLogger(HomepagePane.class);

	private final VideoModel model = VideoModelImpl.getInstance();

	private SortFilter sortFilter;
	private GridPane gridPaneFilterCat;
	private TableView<Video> tview;

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
		fixedLength.textProperty().bind(Bindings.createStringBinding(() -> JkTime.of(model.getVideos().stream().mapToLong(v -> v.getDuration().getTotalMillis()).sum()).toStringElapsed(false), model.getVideos()));
		Label selLength = new Label("");
		selLength.textProperty().bind(Bindings.createStringBinding(() -> JkTime.of(model.getSelectedVideos().stream().filter(Objects::nonNull).mapToLong(v -> v.getDuration().getTotalMillis()).sum()).toStringElapsed(false), model.getSelectedVideos()));
		addDetailsLine(gp, rowNum, "Total length:", fixedLength, selLength);
		rowNum++;

		Label fixedSize = new Label("");
		fixedSize.textProperty().bind(Bindings.createStringBinding(() -> JkOutputFmt.humanSize(model.getVideos().stream().mapToLong(Video::getSize).sum()), model.getVideos()));
		Label selSize = new Label("");
		selSize.textProperty().bind(Bindings.createStringBinding(() -> JkOutputFmt.humanSize(model.getSelectedVideos().stream().filter(Objects::nonNull).mapToLong(Video::getSize).sum()), model.getSelectedVideos()));
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
		tview = new TableView<>();
		tview.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		tview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<Video,String> tcolTitle = new TableColumn<>("VIDEO NAME");
		JkFxUtil.setTableCellFactoryString(tcolTitle, "videoTitle");
		tview.getColumns().add(tcolTitle);
		tcolTitle.setMinWidth(500);

		TableColumn<Video,Long> tcolSize = new TableColumn<>("SIZE");
		JkFxUtil.setTableCellFactory(tcolSize, "size", l -> JkOutputFmt.humanSize(l, JkSizeUnit.MB, false), Long::new);
		tview.getColumns().add(tcolSize);
		tcolSize.setMinWidth(100);

		TableColumn<Video,Integer> tcolWidth = new TableColumn<>("W");
		JkFxUtil.setTableCellFactoryInteger(tcolWidth, "width");
		tview.getColumns().add(tcolWidth);
		tcolWidth.setMinWidth(80);

		TableColumn<Video,Integer> tcolHeight = new TableColumn<>("H");
		JkFxUtil.setTableCellFactoryInteger(tcolHeight, "height");
		tview.getColumns().add(tcolHeight);
		tcolHeight.setMinWidth(80);

		TableColumn<Video,Double> tcolResolution = new TableColumn<>("W/H");
		tcolResolution.setCellValueFactory(param -> new SimpleObjectProperty<>((double) param.getValue().getWidth() / param.getValue().getHeight()));
		JkFxUtil.setTableCellFactory(tcolResolution, "", d -> strf("%.2f", d), Double::new);
		tview.getColumns().add(tcolResolution);
		tcolResolution.setMinWidth(80);

		TableColumn<Video,JkTime> tcolLength = new TableColumn<>("LENGTH");
		JkFxUtil.setTableCellFactory(tcolLength, "duration", d -> d.toStringElapsed(false), JkTime::fromElapsedString);
		tview.getColumns().add(tcolLength);
		tcolLength.setMinWidth(80);

		TableColumn<Video,Integer> tcolPlayTimes = new TableColumn<>("N.PLAY");
		JkFxUtil.setTableCellFactoryInteger(tcolPlayTimes, "playTimes");
		tview.getColumns().add(tcolPlayTimes);
		tcolPlayTimes.setMinWidth(80);

		TableColumn<Video,Integer> tcolNumSnapshots = new TableColumn<>("SNAP");
		JkFxUtil.setTableCellValueBinding(tcolNumSnapshots, "md5");
		tcolNumSnapshots.setCellValueFactory(param -> new SimpleObjectProperty<>(model.findSnapshots(param.getValue()).size()));
		tview.getColumns().add(tcolNumSnapshots);
		tcolPlayTimes.setMinWidth(80);

		TableColumn<Video, LocalDateTime> tcolCreationTm = new TableColumn<>("CREATION");
		JkFxUtil.setTableCellFactoryLocalDateTime(tcolCreationTm, "insertTstamp", DateTimeFormatter.ofPattern("yyyy-MM-dd  HH24:mm:ss"));
		tview.getColumns().add(tcolCreationTm);
		tcolCreationTm.setMinWidth(200);

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

//		tcolTitle.minWidthProperty().bind(tview.widthProperty().multiply(0.6));

		model.getSelectedVideos().setAll(tableItems);

		return vbox;
	}
	private Pane createDetailsSection() {
		BorderPane bp = new BorderPane();

		HBox hbox = new HBox();
		hbox.getStyleClass().addAll("spacing20", "bgPink");
		bp.setRight(hbox);

		model.getSelectedVideos().addListener((ListChangeListener<Video>)c -> {
			hbox.getChildren().clear();
			if(model.getSelectedVideos().size() == 1) {
				Video video = model.getSelectedVideos().get(0);
				List<Path> snapPaths = model.findSnapshots(video);
				try {
					for (int i = 0; i < 6 && i < snapPaths.size(); i++) {
						String url = snapPaths.get(i).toUri().toURL().toExternalForm();
						Image snap = new Image(url);
						ImageView imageView = new ImageView(snap);
						imageView.setPreserveRatio(true);
						imageView.setFitWidth(200);
						imageView.setFitHeight(200);
						hbox.getChildren().add(0, imageView);
					}
				} catch(Exception e) {
					throw new JkRuntimeException(e);
				}
			}
		});

		return bp;
	}
	private void addDetailsLine(GridPane gp, int row, String label, Label fixedLabel, Label selLabel) {
		gp.add(new Label(label), 0, row);
		gp.add(fixedLabel, 1, row);
		gp.add(selLabel, 2, row);
	}



	private Pane createRightPane() {
		VBox box = new VBox();
		box.getStyleClass().add("rightBox");

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

		VBox vbox = new VBox();
		vbox.getStyleClass().add("boxPlayVideos");

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

		return box;
	}

	private void actionAddVideos(ActionEvent event) {
		FileChooser fc = new FileChooser();
		fc.setTitle("Select videos");
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP4", "*.mp4"));
		List<File> files = fc.showOpenMultipleDialog(JkFxUtil.getWindow(event));
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
				Video v = Video.createFromPath(p);
				Path orig = v.getPath();
				Path outPath = Config.VIDEOS_FOLDER.resolve(p.getFileName().toString().replace(";", ""));
				boolean addVideo = JkStreams.filter(model.getVideos(), vi -> vi.getMd5().equals(v.getMd5())).isEmpty();
				if(addVideo) {
					if(!JkFiles.areEquals(orig, outPath)) {
						Path vpath = JkFiles.moveFileSafely(orig, outPath);
						v.setPath(vpath);
					}
					model.getVideos().add(v);
					logger.info("New video added {}", p);
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


	@Override
	public void closePane() {

	}
}
