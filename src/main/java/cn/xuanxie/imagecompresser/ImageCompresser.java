package cn.xuanxie.imagecompresser;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ImageCompresser {
    private final BufferedImage image;
    private final String imageFormat;

    public ImageCompresser(File originImage) throws IOException {
        this.image = ImageIO.read(originImage);
        String imagePath = originImage.getPath();
        this.imageFormat = imagePath.substring(imagePath.lastIndexOf(".") + 1);
    }

    File resize(int newWidth, int newHeight) throws IOException {
        BufferedImage compressedImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D graphics2D = compressedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, newWidth, newHeight, null);
        graphics2D.dispose();
        File outputFile = new File("temp-compressedImage");
        ImageIO.write(compressedImage, imageFormat, new File("temp-compressedImage"));
        return outputFile;
    }

    File requality(float newQuality) throws IOException {
        // 获取JPEG图片写入器
        ImageWriter writer = null;
        Iterator<ImageWriter> writerIter = ImageIO.getImageWritersByFormatName("jpeg");
        if (writerIter.hasNext()) {
            writer = writerIter.next();
        }

        // 创建输出流
        File outputFile = new File("temp-compressedImage");
        ImageWriteParam writeParam = new JPEGImageWriteParam(null);
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        // 调整压缩质量，直到文件大小符合目标大小
        float quality = 1.0f; // 初始压缩质量
        while (quality > newQuality) {
            // 设置压缩质量
            writeParam.setCompressionQuality(quality);

            // 将调整后的图片写入输出流
            ImageIO.createImageOutputStream(outputFile);
            if (writer != null) {
                writer.setOutput(ImageIO.createImageOutputStream(outputFile));
                writer.write(null, new IIOImage(image, null, null), writeParam);
            }

            // 更新压缩质量
            quality -= 0.1f;
        }

        if (writer != null) {
            writer.dispose();
        }
        return outputFile;
    }
}
