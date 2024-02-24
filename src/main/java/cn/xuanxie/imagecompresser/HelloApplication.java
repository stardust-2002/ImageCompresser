package cn.xuanxie.imagecompresser;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class HelloApplication extends Application {
    private static File originImage;
    private static File compressedImage;

    @Override
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 600, 500);
        stage.setScene(scene);

        //菜单
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("文件");
        MenuItem openItem = new MenuItem("打开");
        MenuItem saveItem = new MenuItem("保存");
        fileMenu.getItems().addAll(openItem, saveItem);
        Menu compress = new Menu("压缩");
        MenuItem resizeItem = new MenuItem("减小尺寸");
        MenuItem requalityItem = new MenuItem("降低质量");
        compress.getItems().addAll(resizeItem, requalityItem);
        menuBar.getMenus().addAll(fileMenu, compress);
        borderPane.setTop(menuBar);

        //图片
        ImageView imageView = new ImageView();
        //图片过大设置滚动条
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(imageView);
        borderPane.setCenter(scrollPane);

        //图片体积文本
        Text text = new Text();
        borderPane.setBottom(text);

        //菜单添加事件监听
        openItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.*"),
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("BMP", "*.bmp")
            );
            originImage = fileChooser.showOpenDialog(stage);
            if (originImage != null) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(originImage);
                    Image image = new Image(fileInputStream);
                    imageView.setImage(image);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                String imageSize = "图片体积：" + String.format("%.2f", (double) originImage.length() / (1024 * 1024)) + " MB";
                text.setText(imageSize);
            }
        });
        saveItem.setOnAction(e -> {
            if (compressedImage == null) return;
            FileChooser fileChooser = new FileChooser();
            String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
            fileChooser.setInitialDirectory(new File(desktopPath));
            String originImageName = originImage.getName();
            originImageName = originImageName.substring(0,originImageName.lastIndexOf('.'));
            fileChooser.setInitialFileName(originImageName);
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("BMP", "*.bmp"),
                    new FileChooser.ExtensionFilter("All Images", "*.*")
            );
            File targetImage = fileChooser.showSaveDialog(stage);
            if (targetImage != null) {
                String targetImageName = targetImage.getName();
                String targetImageType = targetImageName.substring(targetImageName.lastIndexOf(".") + 1);
                //保存压缩后图片
                try {
                    ImageIO.write(ImageIO.read(compressedImage), targetImageType, targetImage);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        resizeItem.setOnAction(e -> {
            if (originImage == null) return;
            Image image = null;
            try {
                image = new Image(new FileInputStream(originImage));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }

            VBox vBox = new VBox(20);
            HBox hBox1 = new HBox(40);
            Label originWidth = new Label("原宽度");
            Text originWidthVal = new Text(String.valueOf(image.getWidth()));
            HBox originWidthBox = new HBox(10, originWidth, originWidthVal);
            Label compressedWidth = new Label("压缩后宽度");
            TextField compressedWidthVal = new TextField();
            HBox compressedWidthBox = new HBox(10, compressedWidth, compressedWidthVal);
            hBox1.setAlignment(Pos.CENTER);
            hBox1.getChildren().addAll(originWidthBox, compressedWidthBox);

            HBox hBox2 = new HBox(40);
            Label originHeight = new Label("原高度");
            Text originHeightVal = new Text(String.valueOf(image.getHeight()));
            HBox originHeightBox = new HBox(10, originHeight, originHeightVal);
            Label compressedHeight = new Label("压缩后高度");
            TextField compressedHeightVal = new TextField();
            HBox compressedHeightBox = new HBox(10, compressedHeight, compressedHeightVal);
            hBox2.setAlignment(Pos.CENTER);
            hBox2.getChildren().addAll(originHeightBox, compressedHeightBox);

            HBox hBox3 = new HBox();
            Button button = new Button("确认");
            hBox3.setAlignment(Pos.CENTER);
            hBox3.getChildren().add(button);
            vBox.getChildren().addAll(hBox1, hBox2, hBox3);

            Stage firm = createFirm(stage, new Scene(vBox));
            firm.setTitle("减小尺寸");

            button.setOnAction(event -> {
                String newWidth = compressedWidthVal.getText();
                String newHeight = compressedHeightVal.getText();
                try {
                    compressedImage = new ImageCompresser(originImage)
                            .resize(Integer.parseInt(newWidth), Integer.parseInt(newHeight));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                firm.close();
            });

            firm.show();
        });
        requalityItem.setOnAction(e -> {
            if (originImage == null) return;
            VBox vBox = new VBox(20);
            HBox hBox1 = new HBox(40);
            Label compressedQuality = new Label("压缩后质量");
            ChoiceBox<String> compressedQualityVal = new ChoiceBox<>(
                    FXCollections.observableArrayList("0.9", "0.8", "0.7", "0.6", "0.5", "0.4", "0.3", "0.2", "0.1"));
            compressedQualityVal.setValue("0.9");
            hBox1.setAlignment(Pos.CENTER);
            hBox1.getChildren().addAll(compressedQuality, compressedQualityVal);
            HBox hBox2 = new HBox();
            Button button = new Button("确认");
            hBox2.setAlignment(Pos.CENTER);
            hBox2.getChildren().addAll(button);
            vBox.getChildren().addAll(hBox1, hBox2);

            Stage firm = createFirm(stage, new Scene(vBox));
            firm.setTitle("降低质量");
            button.setOnAction(event -> {
                String newQuality = compressedQualityVal.getValue();
                try {
                    compressedImage = new ImageCompresser(originImage).requality(Float.parseFloat(newQuality));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                firm.close();
            });
            firm.show();
        });
        stage.setTitle("ImageCompresser");
        stage.show();
    }

    static Stage createFirm(Stage parent, Scene scene) {
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.initOwner(parent);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setWidth(400);
        stage.setHeight(250);
        stage.setX(parent.getX() + (parent.getWidth() - stage.getWidth()) / 2);
        stage.setY(parent.getY() + (parent.getHeight() - stage.getHeight()) / 2);
        stage.setScene(scene);
        return stage;
    }

    public static void main(String[] args) {
        launch();
    }
}