package xxx.joker.apps.video.manager.jfx.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.config.Config;
import xxx.joker.apps.video.manager.data.beans.Category;
import xxx.joker.apps.video.manager.data.beans.Video;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.apps.video.manager.jfx.model.beans.SortFilter;
import xxx.joker.apps.video.manager.main.SceneManager;
import xxx.joker.apps.video.manager.provider.StagePosProvider;
import xxx.joker.libs.javalibs.datetime.JkTime;
import xxx.joker.libs.javalibs.format.JkOutputFmt;
import xxx.joker.libs.javalibs.format.JkSizeUnit;
import xxx.joker.libs.javalibs.javafx.JkFxUtil;
import xxx.joker.libs.javalibs.utils.JkFiles;
import xxx.joker.libs.javalibs.utils.JkStreams;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

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
		sortFilter.triggerSort();

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

		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("gridPane");
		addRadioLine(gridPane, "Cataloged:", 0, sortFilter::setCataloged);
		addRadioLine(gridPane, "To split:", 1, sortFilter::setToBeSplit);
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

		return box;
	}
	private void fillGridPaneCategFilter() {
		gridPaneFilterCat.getChildren().clear();
		List<Category> existingCatList = model.getVideos().stream()
										   .flatMap(v -> v.getCategories().stream())
										   .sorted()
										   .distinct()
										   .collect(Collectors.toList());
		for(int i = 0; i < existingCatList.size(); i++) {
			Category cat = existingCatList.get(i);
			addRadioLine(gridPaneFilterCat, cat, i);
		}
	}

	private void clearFilterRadios() {
		toggleMap.values().forEach(
			tg -> tg.getValue().selectToggle(tg.getValue().getToggles().get(tg.getValue().getToggles().size()-1))
		);
	}

	private void addRadioLine(GridPane gridPane, Category cat, int row) {
		addRadioLine(gridPane, cat.getName(), row, b -> sortFilter.setCategory(cat, b));
	}
	private void addRadioLine(GridPane gridPane, String catName, int row, Consumer<Boolean> setter) {
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

		gridPane.add(pair.getKey(), 0, row);
		for(int i = 0; i < pair.getValue().getToggles().size(); i++) {
			gridPane.add(((RadioButton)pair.getValue().getToggles().get(i)), i+1, row);
		}
	}

	private Pane createCenterPane() {
		tview = new TableView<>();
		tview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<Video,Path> tcolPath = new TableColumn<>("VIDEO NAME");
		JkFxUtil.setTableCellFactory(tcolPath, "path", JkFiles::getFileName, Paths::get);
		tcolPath.setPrefWidth(350);
		tview.getColumns().add(tcolPath);

		TableColumn<Video,Long> tcolSize = new TableColumn<>("SIZE");
		JkFxUtil.setTableCellFactory(tcolSize, "size", l -> JkOutputFmt.humanSize(l, JkSizeUnit.MB, false), Long::new);
		tview.getColumns().add(tcolSize);

		TableColumn<Video,Integer> tcolWidth = new TableColumn<>("W");
		JkFxUtil.setTableCellFactoryInteger(tcolWidth, "width");
		tview.getColumns().add(tcolWidth);

		TableColumn<Video,Integer> tcolHeight = new TableColumn<>("H");
		JkFxUtil.setTableCellFactoryInteger(tcolHeight, "height");
		tview.getColumns().add(tcolHeight);

		TableColumn<Video,Double> tcolResolution = new TableColumn<>("W/H");
		tcolResolution.setCellValueFactory(param -> new SimpleObjectProperty<>((double) param.getValue().getWidth() / param.getValue().getHeight()));
		JkFxUtil.setTableCellFactory(tcolResolution, "", d -> strf("%.2f", d), Double::new);
		tview.getColumns().add(tcolResolution);

		TableColumn<Video,JkTime> tcolLength = new TableColumn<>("LENGTH");
		JkFxUtil.setTableCellFactory(tcolLength, "duration", d -> d.toStringElapsed(false), JkTime::fromElapsedString);
		tview.getColumns().add(tcolLength);

		TableColumn<Video,Integer> tcolPlayTimes = new TableColumn<>("NPLAY");
		JkFxUtil.setTableCellFactoryInteger(tcolPlayTimes, "playTimes");
		tview.getColumns().add(tcolPlayTimes);

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
		GridPane gp = new GridPane();
		gp.getStyleClass().add("paneDetails");

		int rowNum = 0;

		Label fixedNum = new Label("");
		fixedNum.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(model.getVideos().size()), model.getVideos()));
		Label selNum = new Label("");
		selNum.textProperty().bind(Bindings.createStringBinding(() -> model.getSelectedVideos().size()+"", model.getSelectedVideos()));
		addDetailsLine(gp, rowNum, "Number fo videos:", fixedNum, selNum);
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

		return gp;
	}
	private void addDetailsLine(GridPane gp, int row, String label, Label fixedLabel, Label selLabel) {
		gp.add(new Label(label), 0, row);
		gp.add(fixedLabel, 1, row);
		gp.add(selLabel, 2, row);
	}



	private Pane createRightPane() {
		VBox box = new VBox();
		box.getStyleClass().add("rightBox");

		Button btnManageCategories = new Button("MANAGE CATEGORIES");
		btnManageCategories.setOnAction(e -> SceneManager.displayCategoryManagement());
		box.getChildren().add(btnManageCategories);

		Button btnAddVideos = new Button("ADD VIDEOS");
		btnAddVideos.setOnAction(this::actionAddVideos);
		box.getChildren().add(btnAddVideos);

		Button btnGoToCategorizeVideo = new Button("CATEGORIZE VIDEOS");
		btnGoToCategorizeVideo.disableProperty().bind(Bindings.createBooleanBinding(model.getSelectedVideos()::isEmpty, model.getSelectedVideos()));
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
				if(!model.getVideos().contains(v)) {
					if(!JkFiles.areEquals(orig, outPath)) {
						Path vpath = JkFiles.moveFileSafely(orig, outPath);
						v.setPath(vpath);
					}
					model.getVideos().add(v);
					logger.info("New video added {}", p);
				} else if(!JkFiles.areEquals(orig, outPath)){
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