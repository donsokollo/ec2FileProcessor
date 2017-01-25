package com.sokolowski.AWS;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
/**
 * Created by Sokol on 2017-01-25.
 */
public class JsonReader {

    private String region;
    private String accessKeyId;
    private String secretAccessKey;

    JsonReader(){
        readJson();
        //String region=this.region;
        //String accessKeyId=this.accessKeyId;
        //String secretAccessKey=this.secretAccessKey;


    }
    public String getRegion(){
        return region;
    }
    public String getAccessKeyId(){
        return accessKeyId;
    }
    public String getSecretAccessKey(){
        return secretAccessKey;
    }

    private void readJson() {

        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader("config.json"));

            JSONObject jsonObject = (JSONObject) obj;
            this.accessKeyId = (String) jsonObject.get("accessKeyId");
            this.secretAccessKey = (String) jsonObject.get("secretAccessKey");
            this.region = (String) jsonObject.get("region");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }



}
