package com.sokolowski.AWS;

import com.amazonaws.services.s3.model.S3ObjectInputStream;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by Sokol on 2017-01-25.
 */
public class ImageProcessor {
    String filename;
    BufferedImage img;
    File imageFile;
    S3ObjectInputStream procImage;

    ImageProcessor(String filename){
        this.filename=filename;
        this.imageFile=new File(filename);
    }


    ImageProcessor(S3ObjectInputStream file){
        try {
            this.img=ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveImage(){
        try{
            File outputfile = new File("abc.png");
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }



    public S3ObjectInputStream changeImage(){
//        AffineTransform at = new AffineTransform();
//        at.rotate(Math.PI/2, img.getHeight()/2,img.getWidth()/2);
//        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
//
//        BufferedImage newImage =new BufferedImage(img.getHeight(), img.getWidth(), img.getType()); //20, 20 is a height and width of img ofc
//
//        op.filter(img, newImage);
//        this.img = newImage;

        BufferedImage oldImage = img;
        BufferedImage newImage = new BufferedImage(oldImage.getHeight(), oldImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) newImage.getGraphics();
        graphics.rotate(Math.toRadians(90), newImage.getWidth() / 2, newImage.getHeight() / 2);
        graphics.translate((newImage.getWidth() - oldImage.getWidth()) / 2, (newImage.getHeight() - oldImage.getHeight()) / 2);
        graphics.drawImage(oldImage, 0, 0, oldImage.getWidth(), oldImage.getHeight(), null);
        this.img=newImage;

        final ByteArrayOutputStream output = new ByteArrayOutputStream() {
            @Override
            public synchronized byte[] toByteArray() {
                return this.buf;
            }
        };
        try {
            ImageIO.write(newImage, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream is = new ByteArrayInputStream(output.toByteArray(), 0, output.size());
        return (S3ObjectInputStream) is;
    }
}
