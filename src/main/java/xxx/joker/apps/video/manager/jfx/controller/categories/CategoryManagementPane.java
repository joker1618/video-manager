package xxx.joker.apps.video.manager.jfx.controller.categories;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.apps.video.manager.jfx.controller.CloseablePane;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.libs.javalibs.datetime.JkTime;
import xxx.joker.libs.javalibs.format.JkOutputFmt;
import xxx.joker.libs.javalibs.format.JkSizeUnit;
import xxx.joker.libs.javalibs.javafx.JkFxUtil;
import xxx.joker.libs.javalibs.utils.JkStreams;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementPane extends BorderPane implements CloseablePane {

	private static final Logger logger = LoggerFactory.getLogger(CategoryManagementPane.class);

	private final VideoModel model = VideoModelImpl.getInstance();
	private ObservableList<Categ> categList;

	public CategoryManagementPane() {
		this.categList = createCategList();
		createPane();
	}

	private void createPane() {
		// tob -> caption
		HBox topBox = new HBox(new Label("CATEGORY MANAGEMENT"));
		topBox.getStyleClass().add("topBox");
		setTop(topBox);

		// center -> table videos & videos stats
		setCenter(createCenterPane());

		getStylesheets().add(getClass().getResource("/css/CategoryManagementPane.css").toExternalForm());
	}

	private Pane createCenterPane() {
		TableView<Categ> tview = new TableView<>();
		tview.setEditable(true);
//		tview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<Categ, String> tcolEditName = createEditableColCatName();
		tcolEditName.setEditable(true);
		tview.getColumns().add(tcolEditName);

		TableColumn<Categ,Integer> tcolNumVideos = new TableColumn<>("NUM VIDEOS");
		tcolNumVideos.setEditable(false);
		JkFxUtil.setTableCellFactoryInteger(tcolNumVideos, "numVideos");
		tview.getColumns().add(tcolNumVideos);

		TableColumn<Categ,Long> tcolSize = new TableColumn<>("TOT SIZE");
		tcolSize.setEditable(false);
		JkFxUtil.setTableCellFactory(tcolSize, "totSize", l -> JkOutputFmt.humanSize(l, JkSizeUnit.MB, false), Long::new);
		tview.getColumns().add(tcolSize);

		TableColumn<Categ,Long> tcolDuration = new TableColumn<>("TOT DURATION");
		tcolDuration.setEditable(false);
		JkFxUtil.setTableCellFactory(tcolDuration, "totMillis", l -> JkTime.of(l).toStringElapsed(false), Long::new);
		tview.getColumns().add(tcolDuration);

//		SortedList<Video> tableItems = new SortedList<>(filteredList);
//		tableItems.comparatorProperty().bind(tview.comparatorProperty());
		tview.setItems(categList);

		VBox vbox = new VBox(tview);
		vbox.getStyleClass().add("centerBox");
		VBox.setVgrow(tview, Priority.ALWAYS);

		return vbox;
	}

	@Override
	public void closePane() {

	}

	private ObservableList<Categ> createCategList() {
		List<Categ> categList = new ArrayList<>();
		for(Category cat : model.getCategories()) {
			List<Video> vcats = JkStreams.filter(model.getVideos(), v -> v.getCategories().contains(cat));
			long totSize = vcats.stream().mapToLong(Video::getSize).sum();
			long totMillis = vcats.stream().mapToLong(v -> v.getDuration().getTotalMillis()).sum();
			Categ categ = new Categ(cat.getName(), vcats.size(), totMillis, totSize);
			categList.add(categ);
		}
		return FXCollections.observableArrayList(categList);
	}

	private TableColumn<Categ,String> createEditableColCatName() {
		TableColumn<Categ,String> col = new TableColumn<>("CATEGORY NAME");
		col.setCellValueFactory(cellData -> cellData.getValue().editName);
		col.setCellFactory(t -> new EditableTextCell());
		col.setMinWidth(300.0);
		col.setEditable(true);
		col.setOnEditCommit(
			(TableColumn.CellEditEvent<Categ, String> t) -> {
//				Categ item = t.getTableView().getItems().get(t.getTablePosition().getRow());
//				logger.debug("column edit {} - ", t.getNewValue(), item.getEditName());
//				item.setEditName(t.getNewValue());
			});
		return col;
	}

	public static class Categ {
		String origName;
		SimpleStringProperty editName;
		int numVideos;
		long totMillis;
		long totSize;

		Categ(String name, int numVideos, long totMillis, long totSize) {
			this.origName = name;
			this.editName = new SimpleStringProperty(name);
			this.numVideos = numVideos;
			this.totMillis = totMillis;
			this.totSize = totSize;
		}

		public String getEditName() {
			return editName.get();
		}

		public SimpleStringProperty editNameProperty() {
			return editName;
		}

		public void setEditName(String editName) {
			this.editName.set(editName);
		}

		public String getOrigName() {
			return origName;
		}

		public int getNumVideos() {
			return numVideos;
		}

		public long getTotMillis() {
			return totMillis;
		}

		public long getTotSize() {
			return totSize;
		}
	}

	class EditableTextCell extends TableCell<Categ,String> {

		private TextField textField;

		public EditableTextCell() {
			this.textField = new TextField("");
		}

		@Override
		public void startEdit() {
			logger.debug("start empty={}", isEmpty());
			if (!isEmpty()) {
				super.startEdit();
				createTextField();
				setText(null);
				setGraphic(textField);
				textField.selectAll();
			}
		}

		@Override
		public void cancelEdit() {
			logger.debug("cancel {}", getItem());
			super.cancelEdit();
			Categ categ = getTableView().getItems().get(getTableRow().getIndex());
			setText(categ.origName);
			setGraphic(null);
		}

		@Override
		public void updateItem(String item, boolean empty) {
			logger.debug("update item {}, {}, {}", item, empty, isEditing());

			String trim = StringUtils.isBlank(textField.getText()) ? "" : textField.getText().trim();
			if(getTableRow().getIndex() < 0 || getTableRow().getIndex() >= getTableView().getItems().size()) {
				return;
			}
			
			Categ categ = getTableView().getItems().get(getTableRow().getIndex());
			if(StringUtils.isBlank(trim) || (!categ.getEditName().equalsIgnoreCase(trim) && !JkStreams.filter(categList, c -> c.getEditName().equalsIgnoreCase(trim)).isEmpty())) {
				cancelEdit();
			} else {
				super.updateItem(item, empty);
				setText(trim);
				setGraphic(null);
				// todo imsert here category update
			}

			if (empty) {
				setText(item);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					setGraphic(null);
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnAction((e) -> commitEdit(textField.getText().trim()));
		}

		private void performCommitEdit() {
			String trim = textField.getText().trim();
			Categ item = getTableView().getItems().get(getTableRow().getIndex());
			if(StringUtils.isBlank(trim) || (!item.getEditName().equalsIgnoreCase(trim) && !JkStreams.filter(categList, c -> c.getEditName().equalsIgnoreCase(trim)).isEmpty())) {
				textField.setText(item.getEditName());
			} else {
				commitEdit(trim);
				item.setEditName(trim);
			}
		}

		private String getString() {
			return getItem() == null ? "" : getItem().trim();
		}
	}
}
