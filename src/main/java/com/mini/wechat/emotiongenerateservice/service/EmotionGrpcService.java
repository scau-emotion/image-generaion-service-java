package com.mini.wechat.emotiongenerateservice.service;

import grpc.EmotionGenerate;
import grpc.EmotionServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: HongchaoLv
 * @Description:
 * @Date: Created in 12:22 2020/5/2
 * @Modified By:
 */
@GrpcService
public class EmotionGrpcService extends EmotionServiceGrpc.EmotionServiceImplBase {

    @Override
    public void generateEmotion(EmotionGenerate.GenerateEmotionRequest request,
                                StreamObserver<EmotionGenerate.GenerateEmotionResponse> responseObserver) {
        try {
            String path = System.getProperty("user.dir") + "/template/";
            String resPath = generateWordImage(path + request.getTemplateImage(), request.getContent());
            EmotionGenerate.GenerateEmotionResponse response = EmotionGenerate.GenerateEmotionResponse.newBuilder()
                    .setImage(resPath)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
            EmotionGenerate.GenerateEmotionResponse response = EmotionGenerate.GenerateEmotionResponse.newBuilder()
                    .setImage("error-gene")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private String generateWordImage(String url, String content) throws Exception{
        InputStream inputStream = new FileInputStream(url);
        Image image1 = ImageIO.read(inputStream);
        int srcWidth = image1.getWidth(null);
        int srcHeight = image1.getHeight(null);

        BufferedImage wordImage = new BufferedImage(srcWidth, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = wordImage.createGraphics();
        // 字体大小
        Font font = new Font("微软雅黑", Font.PLAIN, 30);
        FontMetrics fm = graphics2D.getFontMetrics(font);
        int fontWidth = fm.stringWidth(content);
        int deHeight = 50;

        // 1. 分割换行文字
        List<String> strList = getWordList(fontWidth, srcWidth, content, fm);
        // 需要换行时，修改图片大小
        if (strList.size() > 1) {
            // 总字符串长度是文本宽度的多少倍;
            deHeight = strList.size() * 45;
            wordImage = new BufferedImage(srcWidth, deHeight, BufferedImage.TYPE_INT_RGB);
            graphics2D = wordImage.createGraphics();
        }
        // 2. 画出文字
        drawWordImage(graphics2D, srcWidth, deHeight, strList, font);
        // 3. 输出图片
//        String path = "D:/" + new Date().getTime() + "-1.png";
//        ImageIO.write(wordImage, "png", new FileOutputStream(path));
        // 4. 拼接图片
        return stitchImage(image1, wordImage, srcWidth, srcHeight, deHeight);
    }

    private String stitchImage(Image templateImage, BufferedImage wordImage, int srcWidth, int srcHeight,
                             int deHeight) throws Exception {
        BufferedImage resImage = new BufferedImage(srcWidth, srcHeight + deHeight, BufferedImage.TYPE_INT_RGB);
        int[] imageArrayOne = new int[srcWidth * srcHeight];
        imageArrayOne = ((BufferedImage) templateImage).getRGB(0, 0, srcWidth, srcHeight, imageArrayOne, 0, srcWidth);
        int[] imageArrayTwo = new int[srcWidth * deHeight];
        imageArrayTwo = wordImage.getRGB(0,0, srcWidth, deHeight, imageArrayTwo, 0, srcWidth);
        resImage.setRGB(0, 0, srcWidth, srcHeight, imageArrayOne, 0, srcWidth);
        resImage.setRGB(0, srcHeight, srcWidth, deHeight, imageArrayTwo, 0, srcWidth);
        String imageName = new Date().getTime() + ".png";
        String path = System.getProperty("user.dir") + "/upload/" + imageName;
        ImageIO.write(resImage, "png", new FileOutputStream(path));
        return imageName;
    }

    private void drawWordImage(Graphics2D graphics2D, int srcWidth, int deHeight, List<String> strList,
                               Font font) {
        graphics2D.setColor(Color.BLUE);
        graphics2D.fillRect(0, 0, srcWidth, deHeight);
        graphics2D.setFont(font);
        graphics2D.setColor(new Color(188,188, 188));
        FontMetrics fm = graphics2D.getFontMetrics(font);
        // 离左边   文字底离顶部
        for (int i = 0; i < strList.size(); i++) {
            if (i == strList.size() - 1) {
                // 最后一行需要居中，前面太长的直接铺满
                String lastLine = strList.get(i);
                int lastWidth = fm.stringWidth(lastLine);
                graphics2D.drawString(strList.get(i), (srcWidth - lastWidth) >> 1, (i + 1) * 38);
            } else {
                graphics2D.drawString(strList.get(i), 10, (i + 1) * 38);
            }
        }
    }

    // 文字换行
    private List<String> getWordList(int fontWidth, int srcWidth, String content, FontMetrics fm) {
        List<String> strList = new ArrayList<>();

        if (fontWidth <= srcWidth - 10 ) {
            strList.add(content);
        } else {
            // 文本宽度
            int text_width = srcWidth - 20;
            // 每行字数
            double bs = fontWidth * 1.0 / text_width;
            int lineCharCount = (int) Math.ceil(content.length() / bs);
            for (int begin = 0; begin < content.length(); ) {
                int end = begin + lineCharCount;
                end = Math.min(end, content.length());
                String lineStr = content.substring(begin, end);
                int thisWidth = fm.stringWidth(lineStr);
                while (end < content.length() && text_width - thisWidth >= 30) {
                    lineStr = content.substring(begin, ++end);
                    thisWidth = fm.stringWidth(lineStr);
                }
                while (end > begin && thisWidth > text_width) {
                    lineStr = content.substring(begin, --end);
                    thisWidth = fm.stringWidth(lineStr);
                }
                strList.add(lineStr);
                begin = end;
            }
        }
        return strList;
    }
}
