package com.sokolowski.AWS;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.*;
import javax.imageio.*;

/**
 * Created by DS on 24.01.17.
 */
public class SQSReceiver {

    private static AWSCredentials credentials = null;
    private final static String sqsURL = "https://sqs.us-west-2.amazonaws.com/983680736795/SokolowskiSQS";
    private final static String bucketName = "lab4-weeia";
    private BufferedImage img;



public static void main(String[] args){

//        System.out.println("Hello world!");
//        try {
//            credentials = new ProfileCredentialsProvider().getCredentials();
//        } catch (Exception e) {
//            throw new AmazonClientException(
//                    "Cannot load the credentials from the credential profiles file. " +
//                            "Please make sure that your credentials file is at the correct " +
//                            "location (~/.aws/credentials), and is in valid format.",
//                    e);
//        }



    JsonReader myCredentials=new JsonReader();

        AWSCredentials credentials = new BasicAWSCredentials(myCredentials.getAccessKeyId(), myCredentials.getSecretAccessKey());




        AmazonSQS sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        sqs.setRegion(usWest2);

        AmazonS3 s3 = new AmazonS3Client(credentials);
        s3.setRegion(usWest2);





        List<String> filesList = getMessages(sqs); //list of files to be changed

        for (String file : filesList) {
            String[] files = file.split(","); //splits attribute to separate names of files

            for (int i=0; i<files.length; ++i) { //dlaczego ++i ??
                try {
                    processFile(files[i], s3);
                    System.out.println(i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        System.out.println();

    }

    /**
     * Takes one message from sqs and returns a list with fules to process
     * @param sqs sqs class from amazon
     * @return list with files to process
     */
    private static List<String> getMessages(AmazonSQS sqs) {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsURL);
        List<Message> messages = sqs.receiveMessage(receiveMessageRequest.withMessageAttributeNames("All")).getMessages();

        List<String> filesToProcess = new ArrayList<String>();

        for (Message message : messages) {
           // System.out.println("  Message");
            System.out.println("    MessageId:     " + message.getMessageId());
            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
            System.out.println("    Body:          " + message.getBody());
            for (Entry<String, MessageAttributeValue> entry : message.getMessageAttributes().entrySet()) {
//                System.out.println("  Attribute");
//                System.out.println("    Name:  " + entry.getKey());
//                System.out.println("    Value: " + entry.getValue().getStringValue());
                filesToProcess.add(entry.getValue().getStringValue());
            }
//            System.out.println("Deleting a message.\n");
            String messageReceiptHandle = message.getReceiptHandle();
            sqs.deleteMessage(new DeleteMessageRequest(sqsURL, messageReceiptHandle));
        }
        return filesToProcess;
    }

    private static void processFile(String key, AmazonS3 s3) throws IOException {

        System.out.println("Downloading an object");
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
        System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());

        ImageProcessor imageProc=new ImageProcessor(object.getObjectContent());

        InputStream newImage= imageProc.changeImage();
        object.setObjectContent(newImage);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(newImage.available());
       // metadata.addUserMetadata("modified","true");
        object.setObjectMetadata(metadata);
        System.out.println("Deleting an object\n");
        s3.deleteObject(bucketName, key);

        System.out.println(object.getObjectContent());
        System.out.println("Processing...");
        System.out.println("Processing...");

        System.out.println("Uploading a new object to S3 from a file\n");

        s3.putObject(new PutObjectRequest(bucketName, key, object.getObjectContent(), object.getObjectMetadata()));
    }


}
