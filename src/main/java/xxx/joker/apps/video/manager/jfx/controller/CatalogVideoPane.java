package xxx.joker.apps.video.manager.jfx.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoBuilder;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoPlayer;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.libs.core.utils.JkFiles;
import xxx.joker.libs.core.utils.JkStreams;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static xxx.joker.libs.core.utils.JkStrings.strf;

public class CatalogVideoPane extends BorderPane implements CloseablePane {

	private static final Logger logger = LoggerFactory.getLogger(CatalogVideoPane.class);

	private final VideoModel model = VideoModelImpl.getInstance();

	private ListView<Video> videoListView;
	private ObservableList<Video> videos;
	private SimpleIntegerProperty videoIndex;
	private SimpleObjectProperty<JkVideoPlayer> showingPlayer;

	private VBox categoryBox;
	private VBox categoryBoxMulti;
	private Map<Category, CheckBox> checkBoxCategoryMap;
	private Map<Category, CheckBox> checkBoxCategoryMapMulti;
	private final JkVideoBuilder videoPlayerBuilder;

	public CatalogVideoPane() {
		this.videos = FXCollections.observableArrayList(model.getSelectedVideos());
		this.videoIndex = new SimpleIntegerProperty(-1);
		this.showingPlayer = new SimpleObjectProperty<>();

		this.videoPlayerBuilder = new JkVideoBuilder().setShowBorder(true).setShowClose(false);

		setLeft(createVideoListViewPane());

		// CENTER
		setCenter(createPlayerPane());

		getStylesheets().add(getClass().getResource("/css/CatalogVideoPane.css").toExternalForm());

	}

	private Pane createPlayerPane() {
		TextField txtVideoTitle = new TextField();
		txtVideoTitle.setDisable(true);
		Button btnChangeTitle = new Button("CHANGE TITLE");
		btnChangeTitle.disableProperty().bind(Bindings.createBooleanBinding(
				() -> StringUtils.isBlank(txtVideoTitle.getText()) || txtVideoTitle.getText().trim().equals(showingPlayer.get() == null ? "" : showingPlayer.get().getVideo().getVideoTitle()),
				txtVideoTitle.textProperty()
		));
		btnChangeTitle.setOnAction(e -> changeVideoTitle(txtVideoTitle.getText().trim()));
		HBox hboxTopLeft = new HBox(new Label("Video title:"), txtVideoTitle, btnChangeTitle);
		hboxTopLeft.getStyleClass().addAll("spacing20");

		Button btnDelete = new Button("DELETE");
		btnDelete.getStyleClass().addAll("bigButton");
		btnDelete.disableProperty().bind(showingPlayer.isNull());
		btnDelete.setOnAction(e -> actionDeleteVideo());

		BorderPane bptop = new BorderPane();
		bptop.setLeft(hboxTopLeft);
		bptop.setRight(btnDelete);

		BorderPane bp = new BorderPane();
		bp.getStyleClass().addAll("pad20");
		bp.setTop(bptop);
		showingPlayer.addListener((observable, oldValue, newValue) -> {
			bp.setCenter(newValue);
			txtVideoTitle.setDisable(newValue == null);
			if(newValue != null) {
				updateSelectedCheckBoxes();
				updateSelectedCheckBoxesMulti();
				txtVideoTitle.setText(newValue.getVideo().getVideoTitle());
			} else {
				txtVideoTitle.setText("");
			}
		});

		return bp;
	}

	private void changeVideoTitle(String newVideoTitle) {
		JkVideoPlayer vp = showingPlayer.get();
		String oldTitle = vp.getVideo().getVideoTitle();
		if(!newVideoTitle.equals(oldTitle)) {
			vp.getVideo().setVideoTitle(newVideoTitle);
			vp.setPlayerCaption(newVideoTitle);
			videoListView.refresh();
			logger.info("Changed title from [{}] to [{}]", oldTitle, newVideoTitle);
		}
	}

	private Pane createVideoListViewPane() {
		Button btnPrev = new Button("PREV");
		btnPrev.setOnAction(e -> updateShowingVideo(videoIndex.get() - 1));
		Button btnNext = new Button("NEXT");
		btnNext.setOnAction(e -> updateShowingVideo(videoIndex.get() + 1));
		HBox hboxBtns = new HBox(btnPrev, btnNext);
		hboxBtns.getStyleClass().addAll("pad10", "spacing20", "centered", "bigButtonBox");

		this.videoListView = new ListView<>();
		videoListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		videoListView.setCellFactory(param -> new ListCell<Video>() {
			@Override
			protected void updateItem(Video item, boolean empty) {
				super.updateItem(item, empty);
				setText(item == null ? null : item.getVideoTitle());
				showingPlayer.addListener((obs,o,n) -> {
					if(n == null || !n.getVideo().equals(item)) {
						getStyleClass().remove("bold");
					} else {
						getStyleClass().add("bold");
					}
				});
				if(item == null || item.getCategories().isEmpty()) {
					getStyleClass().add("txtRed");
				} else {
					getStyleClass().removeIf("txtRed"::equals);
				}
			}
		});

		videoListView.setOnMouseClicked(event -> {
			if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
				int rowIdx = videoListView.getSelectionModel().getSelectedIndex();
				logger.info("double click rowIdx {}", rowIdx);
				updateShowingVideo(rowIdx);
			}
		});

		videoListView.setItems(videos);

		Pane optionFieldsPane = createOptionFieldsPane();

		VBox vbox = new VBox(hboxBtns, videoListView, optionFieldsPane);
		vbox.getStyleClass().addAll("leftBox", "pad10", "spacing20", "borderBlue");

		return vbox;
	}

	private Pane createOptionFieldsPane() {
		VBox container = new VBox();
		container.getStyleClass().add("choosePane");

		// Categories check box
		categoryBox = new VBox();
		categoryBoxMulti = new VBox();
		checkBoxCategoryMap = new TreeMap<>();
		checkBoxCategoryMapMulti = new TreeMap<>();
		categoryBox.getStyleClass().addAll("boxCategories", "borderRed");
		categoryBoxMulti.getStyleClass().addAll("boxCategories", "borderBlue");
		updateCategoriesCheckBoxes();
		updateCategoriesCheckBoxesMulti();

		Label lblSingle = new Label("SINGLE");
		lblSingle.getStyleClass().add("lblTitle");
		BorderPane bp1 = new BorderPane(categoryBox, lblSingle, null, null, null);
		Label lblMultiNumSel = new Label("MULTI (0)");
		lblMultiNumSel.getStyleClass().add("lblTitle");
		videoListView.getSelectionModel().getSelectedItems().addListener(
				(ListChangeListener<? super Video>) c -> {
						lblMultiNumSel.setText(strf("MULTI ({})", videoListView.getSelectionModel().getSelectedItems().size()));
						updateSelectedCheckBoxesMulti();
		});
		BorderPane bp2 = new BorderPane(categoryBoxMulti, lblMultiNumSel, null, null, null);
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

		return container;
	}

	private void updateCategoriesCheckBoxes() {
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
			Video video = showingPlayer.getValue().getVideo();
			for (Category cat : model.getCategories()) {
				checkBoxCategoryMap.get(cat).setSelected(video.getCategories().contains(cat));
			}
		}
	}
	private void updateSelectedCheckBoxesMulti() {
		ObservableList<Video> sel = videoListView.getSelectionModel().getSelectedItems();
		for (Category cat : model.getCategories()) {
			checkBoxCategoryMapMulti.get(cat).setIndeterminate(false);
			checkBoxCategoryMapMulti.get(cat).setSelected(false);
			int num = JkStreams.filter(sel, v -> v.getCategories().contains(cat)).size();
			if(sel.size() == 0) {
				checkBoxCategoryMapMulti.get(cat).setSelected(false);
			} else if(num == sel.size()) {
				checkBoxCategoryMapMulti.get(cat).setSelected(true);
			} else if(num > 0){
				checkBoxCategoryMapMulti.get(cat).setIndeterminate(true);
			}
		}
	}

	private void actionDeleteVideo() {
		Video videoToDel = showingPlayer.getValue().getVideo();

		try {
			updateShowingVideo(videoIndex.getValue() + 1);
//			showingPlayer.getValue().closePlayer();
			videos.remove(videoToDel);
			model.getVideos().remove(videoToDel);
			videoIndex.set(videoIndex.get()-1);
			Files.delete(videoToDel.getPath());
			logger.info("Deleted video {}", videoToDel.getPath());

		} catch(Exception ex) {
			logger.error("Unable to delete video {}\n{}", videoToDel.getPath(), ex);
		}
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
					showingPlayer.getValue().getVideo().getCategories().add(cat);
					updateSelectedCheckBoxes();
					updateSelectedCheckBoxesMulti();
				}
			}
		}
	}

	private void updateShowingVideo(int idx) {
		int newIndex = Math.max(Math.min(idx, videos.size()), -1);
		if(videoIndex.get() != newIndex) {
			videoIndex.setValue(newIndex);
			JkVideoPlayer videoPlayer = showingPlayer.getValue();
			if (videoPlayer != null) {
				videoPlayer.closePlayer();
			}
			if(newIndex >= 0 && newIndex < videos.size()) {
				Video video = JkStreams.filter(model.getVideos(), v -> videos.get(newIndex).getVideoTitle().equals(v.getVideoTitle())).get(0);
				JkVideoPlayer vp = videoPlayerBuilder.createPane(video);
				vp.play();
				showingPlayer.setValue(vp);
			} else {
				showingPlayer.setValue(null);
			}
		}
	}

	private void actionSetVideoCategory(ActionEvent event, Category category) {
		setVideoCategory(event, category, showingPlayer.getValue().getVideo());
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

	@Override
	public void closePane() {
		if(showingPlayer.getValue() != null) {
			showingPlayer.getValue().closePlayer();
			showingPlayer.setValue(null);
		}
	}
}
