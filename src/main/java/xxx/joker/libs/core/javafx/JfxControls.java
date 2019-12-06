package xxx.joker.libs.core.javafx;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.util.JkStrings;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JfxControls {

	public static ImageView createImageView(Double fitWidth, Double fitHeight) {
		return createImageView1(null, fitWidth, fitHeight, true);
	}
	public static ImageView createImageView(Double fitWidth, Double fitHeight, boolean preserveRatio) {
		return createImageView1(null, fitWidth, fitHeight, preserveRatio);
	}
	public static ImageView createImageView(Path imgPath, int fitWidth, int fitHeight) {
		return createImageView1(new Image(JkFiles.toURL(imgPath)), (double) fitWidth, (double)fitHeight, true);
	}
	public static ImageView createImageView(Path imgPath, Double fitWidth, Double fitHeight) {
		return createImageView1(new Image(JkFiles.toURL(imgPath)), fitWidth, fitHeight, true);
	}
	public static ImageView createImageView(Image image, int fitWidth, int fitHeight) {
		return createImageView1(image, (double) fitWidth, (double) fitHeight, true);
	}
	public static ImageView createImageView(Image image) {
		return createImageView1(image, null, null, true);
	}
	public static ImageView createImageView(Image image, Double fitWidth, Double fitHeight) {
		return createImageView1(image, fitWidth, fitHeight, true);
	}
	public static ImageView createImageView(Image image, Double fitWidth, Double fitHeight, boolean preserveRatio) {
		return createImageView1(image, fitWidth, fitHeight, preserveRatio);
	}
	private static ImageView createImageView1(Image image, Double fitWidth, Double fitHeight, boolean preserveRatio) {
		ImageView imageView = new ImageView();
		if(image != null)	imageView.setImage(image);
		imageView.setPreserveRatio(preserveRatio);
		if(fitWidth != null)	imageView.setFitWidth(fitWidth);
		if(fitHeight != null)	imageView.setFitHeight(fitHeight);
		return imageView;
	}

	public static HBox createHBox(String styleClasses, Collection<? extends Node> nodes) {
		HBox hbox = new HBox();
		hbox.getChildren().addAll(nodes);
		List<String> scList = JkStrings.splitList(styleClasses, " ");
		hbox.getStyleClass().addAll(scList);
		hbox.getStyleClass().addAll("jfxBox", "jfxHBox");
		return hbox;
	}
	public static HBox createHBox(String styleClasses, Node... nodes) {
		return createHBox(styleClasses, Arrays.asList(nodes));
	}
	public static VBox createVBox(String styleClasses, Collection<? extends Node> nodes) {
		VBox vbox = new VBox();
		vbox.getChildren().addAll(nodes);
		List<String> scList = JkStrings.splitList(styleClasses, " ");
		vbox.getStyleClass().addAll(scList);
		vbox.getStyleClass().addAll("jfxBox", "jfxVBox");
		return vbox;
	}
	public static VBox createVBox(String styleClasses, Node... nodes) {
		return createVBox(styleClasses, Arrays.asList(nodes));
	}
}
