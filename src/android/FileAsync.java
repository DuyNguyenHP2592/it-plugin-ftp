package it.plugin.ftp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.Settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FileAsync extends AsyncTask<Void, Integer, Boolean> {
    public final ftpLauncher mListener;
    //CopyStreamAdapter streamListener;
    public String filename = "";
    public String directory = "";
    public String hostName = "";
    public String username = "";
    public String password = "";
    public int port = 21;
    public Boolean bUpload;
    public String filePath = "";
    public int run; // số thứ tự file đang up
    public int sum;//Tổng số file sẽ up
    //khai báo Activity để lưu trữ địa chỉ của MainActivity
    Activity activity;
    public String dbName = "LOCAL.db";
    public Boolean bAlowRun = false;
    public String url = "http://188.88.100.109:8083/api/";

    //constructor này được truyền vào là MainActivity
    public FileAsync(Activity ctx, ftpLauncher listener) {
        activity = ctx;
        mListener = listener;
    }

    //hàm này sẽ được thực hiện đầu tiên
    @Override
    protected void onPreExecute() {
        //Toast.makeText(contextParent, "Uploading...", Toast.LENGTH_SHORT).show();
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            while (bAlowRun) {
                SimpleDateFormat dtRun = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                System.out.println(dt.format(new Date()) + " Start delete old images");
                DAL dbe = new DAL(activity, dbName);
                ArrayList<SUR_LOC_IN_IMAGES> listImage = new ArrayList<SUR_LOC_IN_IMAGES>();
                listImage = getAllImagesNeedDelete();

                int row = 0;
                int count = listImage == null ? 0 : listImage.size();
                if (count > 0)
                    for (SUR_LOC_IN_IMAGES obj : listImage) {
                        if (bAlowRun == false)
                            break;
                        //Delete from local
                        String flagClient=DeleteFile(obj.getUrlDevice());
                        DeleteFile(obj.getUrlDevice().replace(".jpg","_small.jpg"));
                        //Update to Server
                        obj.setFlagClient(flagClient);
                        Boolean bPutServer = putUpdateStatusImage(obj);
                        if (bPutServer) {
                            System.out.println(dt.format(new Date()) + " Put delete images: " + obj.getUrlDevice() + " | status:" + bPutServer);
                        } else
                            System.out.println(dt.format(new Date()) + " WebAPI error when put delete images.");

                        row++;
                    }
                else
                    mListener.Uploading("", "", "", "", "", dtRun.format(new Date()));
                SystemClock.sleep(600000);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String DeleteFile(String url) {
        try {
            File file = new File(url);
            if (file.exists()) {
                file.delete();
                if (file.exists()) {
                    file.getCanonicalFile().delete();
                    if (file.exists()) {
                        activity.getApplicationContext().deleteFile(file.getName());
                    }
                }
                return "D";
            } else
                return "F";

        } catch (Exception ex) {
            return "F";
        }
    }

    private ArrayList<SUR_LOC_IN_IMAGES> getAllImagesNeedDelete() {
        try {
            try {
                //TelephonyManager tm = (TelephonyManager) activity.getSystemService(activity.getApplicationContext().TELEPHONY_SERVICE);
                String deviceID = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
                String strUrl = url + "Sur/GetDeleteImages/" + deviceID;//"eae30ec4ba63d8f4";
                System.out.println("url get images from server: " + strUrl);
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
                client.setReadTimeout(60, TimeUnit.SECONDS);
                Request request = new Request.Builder().url(strUrl).build();
                Response responses = client.newCall(request).execute();

                String jsonData = responses.body().string();
                JSONArray arr = new JSONArray(jsonData);
                ArrayList<SUR_LOC_IN_IMAGES> list = new ArrayList<SUR_LOC_IN_IMAGES>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    SUR_LOC_IN_IMAGES objIMG = new SUR_LOC_IN_IMAGES();
                    objIMG.setSUR_LOC_IN_IMAGES_ID(obj.getString("SUR_LOC_IN_IMAGES_ID"));
                    objIMG.setUrlDevice(obj.getString("UrlDevice"));
                    list.add(objIMG);
                }
                System.out.println("Need upload images: " + arr.length());
                return list;
            } catch (IOException ex) {
                System.out.println("getAllImagesNeedUpload: " + ex.getMessage());
                return null;
            }
        } catch (JSONException ex) {
            return null;
        }
    }

    private Boolean putUpdateStatusImage(SUR_LOC_IN_IMAGES obj) {
        try {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String strUrl = url + "Sur/PutSUR_LOC_IN_IMAGES/" + obj.getSUR_LOC_IN_IMAGES_ID();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            //String json="{\"SUR_LOC_IN_IMAGES_ID\":\"" + obj.getSUR_LOC_IN_IMAGES_ID() + "\",\"FlagUp\":\"" + obj.getFlagUp() + "\",\"Folder\":\"" + obj.getFolder() + "\"}";
            String json = gson.toJson(obj);
            RequestBody body = RequestBody.create(JSON, json);
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(60, TimeUnit.SECONDS);
            Request request = new Request.Builder().url(strUrl).put(body).build();
            Response responses = client.newCall(request).execute();

            if (("200,204").contains(Integer.toString(responses.code())))
                return true;
            else
                return false;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * sau khi tiến trình thực hiện xong thì hàm này sảy ra
     */
    @Override
    protected void onPostExecute(Boolean result) {
        System.out.println("Upload successful: " + run + " " + filename);
        bUpload = true;
        super.onPostExecute(result);
    }

}