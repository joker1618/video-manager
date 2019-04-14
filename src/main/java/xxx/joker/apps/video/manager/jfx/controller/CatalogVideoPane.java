package xxx.joker.apps.video.manager.jfx.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoBuilder;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoPlayer;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.apps.video.manager.main.SceneManager;
import xxx.joker.libs.javafx.JkFxUtil;
import xxx.joker.libs.core.utils.JkStreams;

import java.nio.file.Files;
import java.util.*;

import static xxx.joker.libs.core.utils.JkStrings.strf;

public class CatalogVideoPane extends BorderPane implements CloseablePane {

	private static final Logger logger = LoggerFactory.getLogger(CatalogVideoPane.class);

	private final VideoModel model = VideoModelImpl.getInstance();

	private ObservableList<Video> videoList;
	private SimpleIntegerProperty videoIndex;
	private SimpleObjectProperty<JkVideoPlayer> showingPlayer;

	private HBox categoryBox;
	private Map<Category,CheckBox> checkBoxCategoryMap;
	private final JkVideoBuilder videoPlayerBuilder;

	private Set<Category> defaultCategories = new HashSet<>();
	private SimpleStringProperty defaultCatsAction = new SimpleStringProperty();

	public CatalogVideoPane() {
		this.videoList = FXCollections.observableArrayList(model.getSelectedVideos());
		this.videoIndex = new SimpleIntegerProperty(-1);
		this.showingPlayer = new SimpleObjectProperty<>();

		this.videoPlayerBuilder = new JkVideoBuilder().setShowBorder(true);

		// CENTER
		showingPlayer.addListener((observable, oldValue, newValue) -> {
			setCenter(newValue);
			if(newValue != null) {
				updateCheckableFields();
			}
		});

		// RIGHT
		setRight(createOptionFieldsPane());

		getStylesheets().add(getClass().getResource("/css/CatalogVideoPane.css").toExternalForm());

		updateShowingVideo(0, false);
	}

	private Pane createOptionFieldsPane() {
		VBox container = new VBox();
		container.getStyleClass().add("choosePane");

		// delete button
		Button btnExit = new Button("EXIT");
		btnExit.setOnAction(e -> { showingPlayer.getValue().closePlayer(); SceneManager.displayHomepage(); });
		HBox hb = new HBox(btnExit);
		hb.getStyleClass().add("boxButtons");
		container.getChildren().add(hb);

		// previous and next button
		Button btnPrev = new Button("PREVIOUS");
		btnPrev.setOnAction(e -> updateShowingVideo(videoIndex.getValue() - 1, false));
		Label lblCounter = new Label("");
		lblCounter.getStyleClass().add("boldText");
		lblCounter.textProperty().bind(Bindings.createStringBinding(
			() ->  strf("%d/%d", videoIndex.getValue()+1, videoList.size()),
			videoIndex, videoList
		));
		Button btnNext = new Button("DONE");
		btnNext.setOnAction(e -> updateShowingVideo(videoIndex.getValue() + 1, true));
		HBox btnBox = new HBox(btnPrev, lblCounter, btnNext);
		btnBox.getStyleClass().add("boxButtons");
		container.getChildren().add(btnBox);

		// delete button
		Button btnDelete = new Button("DELETE");
		btnDelete.setOnAction(e -> actionDeleteVideo());
		btnBox = new HBox(btnDelete);
		btnBox.getStyleClass().add("boxButtons");
		container.getChildren().add(btnBox);

		// Categories check box
		categoryBox = new HBox(new VBox(), new VBox());
		categoryBox.getChildren().forEach(ch -> ch.getStyleClass().add("subBox"));
		checkBoxCategoryMap = new TreeMap<>();
		categoryBox.getStyleClass().add("boxCategories");
		updateCategoriesCheckBoxPane();

		ScrollPane scrollPane = new ScrollPane(categoryBox);
		scrollPane.getStyleClass().add("scrollPaneCategories");
		container.getChildren().addAll(scrollPane);

		// Button add category
		Button btnAddCategory = new Button("ADD CATEGORY");
		btnAddCategory.setOnAction(e -> actionAddCategory());
		btnBox = new HBox(btnAddCategory);
		btnBox.getStyleClass().add("boxButtons");
		container.getChildren().add(btnBox);

		// Box defaults
		VBox defaultBox = new VBox();
		defaultBox.getStyleClass().addAll("boxButtons", "simpleBorder");
		container.getChildren().add(defaultBox);

		defaultBox.getChildren().add(new Label("DEFAULT CATEGORIES"));

		ComboBox<String> comboDefault = new ComboBox<>();
		comboDefault.getItems().setAll("NO", "ADD", "SET");
		comboDefault.getSelectionModel().select(0);
		defaultCatsAction.bind(comboDefault.getSelectionModel().selectedItemProperty());
		defaultBox.getChildren().add(comboDefault);

		Button btnSetDefault = new Button("SET DEFAULT CATEGORIES");
		defaultBox.getChildren().add(btnSetDefault);

		VBox labelBox = new VBox();
		labelBox.getStyleClass().add("boxButtons");
		defaultBox.getChildren().add(labelBox);

		btnSetDefault.setOnAction(e -> {
			defaultCategories.clear();
			defaultCategories.addAll(showingPlayer.get().getVideo().getCategories());
			labelBox.getChildren().clear();
			defaultCategories.forEach(cat -> labelBox.getChildren().add(new Label(cat.getName())));
		});

		return container;
	}

	private void updateCategoriesCheckBoxPane() {
		logger.debug("Updating categories check bos pane");
		
		for(Category cat : model.getCategories()) {
			if(!checkBoxCategoryMap.containsKey(cat)) {
				CheckBox cb = new CheckBox(cat.getName());
				cb.setOnAction(e -> actionSetVideoCategory(e, cat));
				checkBoxCategoryMap.put(cat, cb);
			}
		}

		categoryBox.getChildren().forEach(ch -> ((Pane)ch).getChildren().clear());

		int counter = 0;
		for(CheckBox cb : checkBoxCategoryMap.values()) {
			Pane subPane = JkFxUtil.getChildren(categoryBox, counter % 2);
			subPane.getChildren().add(cb);
			counter++;
		}
	}

	private void updateCheckableFields() {
		Video video = showingPlayer.getValue().getVideo();
		for(Category cat : model.getCategories()) {
			checkBoxCategoryMap.get(cat).setSelected(video.getCategories().contains(cat));
		}
	}

	private void actionDeleteVideo() {
		Video videoToDel = showingPlayer.getValue().getVideo();

		try {
			showingPlayer.getValue().closePlayer();
			videoList.remove(videoToDel);
			model.getVideos().remove(videoToDel);
			updateShowingVideo(videoIndex.getValue(), false);
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
				showingPlayer.getValue().getVideo().getCategories().add(cat);
				updateCategoriesCheckBoxPane();
				updateCheckableFields();
			}
		}
	}

	private void updateShowingVideo(int idx, boolean setCataloged) {
		if(idx < 0)	return;

		JkVideoPlayer videoPlayer = showingPlayer.getValue();
		if(videoPlayer != null) {
			videoPlayer.closePlayer();
        }

        if(idx < videoList.size()) {
			Video v = videoList.get(idx);
			if(!"NO".equals(defaultCatsAction.get())) {
				if("SET".equals(defaultCatsAction.get())) {
					v.getCategories().clear();
				}
				v.getCategories().addAll(defaultCategories);
			}
			logger.info("Set new video {}", v);
			videoIndex.setValue(idx);
			JkVideoPlayer vp = videoPlayerBuilder.createPane(v);
			vp.play();
			showingPlayer.setValue(vp);

		} else {
			logger.info("End catalog list. Exiting...");
			showingPlayer.setValue(null);
			SceneManager.displayHomepage();
		}
	}

	private void actionSetVideoCategory(ActionEvent event, Category category) {
		CheckBox source = (CheckBox) event.getSource();
		if(source.isSelected()) {
			showingPlayer.getValue().getVideo().getCategories().add(category);
		} else {
			showingPlayer.getValue().getVideo().getCategories().remove(category);
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
