package edu.bupt.testinfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.util.Log;

public class UploadData {
    private String uploadDataURL = null;
    private String jsonObject;

    public UploadData(String uploadDataURL, JSONObject jsonObject) {
        this.uploadDataURL = uploadDataURL;
        this.jsonObject = jsonObject.toString();
    }

    public UploadData(String uploadDataURL, String jsonObject) {
        this.uploadDataURL = uploadDataURL;
        this.jsonObject = jsonObject;

    }

    public String upData() {
        String resultID = "-1";

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("jsonString", jsonObject));
        InputStream inputStream = null;

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(uploadDataURL);
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while ((line = reader.readLine()) != null) {
                result = result + line;
            }
            Log.v("uploadData",uploadDataURL);
            Log.v("uploadData", result);
            Log.v("result", "result" + result);
            resultID = result;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultID;
    }
}
