package xxx.joker.apps.video.manager.jfx.controller.categories;

import javafx.scene.control.TableCell;

public class EditableTextCellre<T> extends TableCell<T,String> {

//	private TextField textField;
//	private BiPredicate<String,String> predChange;
//
//	public EditableTextCellre(BiPredicate<String, String> predChange) {
//		this.predChange = predChange;
//	}
//
//	@Override
//	public void startEdit() {
//		if (!isEmpty()) {
//			super.startEdit();
//			createTextField();
//			setText(null);
//			setGraphic(textField);
//			textField.selectAll();
//		}
//	}
//
//	@Override
//	public void cancelEdit() {
//		super.cancelEdit();
//		setText(getItem());
//		setGraphic(null);
//	}
//
//	@Override
//	public void updateItem(String item, boolean empty) {
//		super.updateItem(item, empty);
//
//		if (empty) {
//			setText(item);
//			setGraphic(null);
//		} else {
//			if (isEditing()) {
//				if (textField != null) {
//					textField.setText(getString());
//				}
//				setText(null);
//				setGraphic(textField);
//			} else {
//				setText(getString());
//				setGraphic(null);
//			}
//		}
//	}
//
//	private void createTextField() {
//		textField = new TextField(getString());
//		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
//		textField.setOnAction((e) -> commitEdit(textField.getText()));
//		textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
//			if (!newValue) {
//				T item = getTableView().getItems().get(getTableRow().getIndex());
//				String trim = textField.getText().trim();
//				boolean test = predChange.test(item, trim);
//				commitEdit(trim) : textField.setText(item.ge);
//			}
//		});
//	}
//
//	private String getString() {
//		return getItem() == null ? "" : getItem().trim();
//	}
}