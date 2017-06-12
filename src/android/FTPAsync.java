package it.plugin.ftp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FTPAsync extends AsyncTask<Void, Integer, Boolean> {
    public final ftpLauncher mListener;
    //CopyStreamAdapter streamListener;
    public String filename = "";
    public String directory = "";
    public String hostName = "";
    public String username = "";
    public String password = "";
    public int port = 21;
    public Boolean bUpload;
    //FTPClass utp = new FTPClass();
    public String filePath = "";
    public int run; // số thứ tự file đang up
    public int sum;//Tổng số file sẽ up
    //khai báo Activity để lưu trữ địa chỉ của MainActivity
    Activity activity;
    public String dbName = "LOCAL.db";
    public Boolean bAlowRun = false;
    public String url = "http://188.88.100.109:8083/api/";

    //constructor này được truyền vào là MainActivity
    public FTPAsync(Activity ctx, ftpLauncher listener) {
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
                System.out.println(dt.format(new Date()) + " Start upload images");
                DAL dbe = new DAL(activity, dbName);
                ArrayList<SUR_LOC_IN_IMAGES> listImage = new ArrayList<SUR_LOC_IN_IMAGES>();
                listImage = getAllImagesNeedUpload();// dbe.getAll_SUR_LOC_IN_IMAGES();

                int row = 0;
                int count = listImage == null ? 0 : listImage.size();
                if (count > 0)
                    for (SUR_LOC_IN_IMAGES obj : listImage) {
                        if(bAlowRun==false)
                            break;
                        //Upload file to FTP server "/mnt/sdcard/SURVEY/2016_12_12/1.jpg"
                        String flag = ftpUpload(obj.getUrlDevice(), obj.getFileName(), obj.getFTPFolder(),
                                obj.getFTPServer(), obj.getFTPUser(), obj.getFTPPass(), Integer.parseInt(obj.getFTPPort()), row, count);
                        //Update to Server and Database
                        obj.setFlagUp(flag);
                        if (flag != "D") {
                            Boolean bPutServer = putUpdateStatusImage(obj);
                            if (bPutServer) {
                                //Boolean bUpdateSQLite = dbe.update_SUR_LOC_IN_IMAGES(obj);
                                System.out.println(dt.format(new Date()) + " Up server: " + bPutServer);// + " / update sqlite: " + bUpdateSQLite);
                            } else
                                System.out.println(dt.format(new Date()) + " WebAPI error or not found folder on server: ");
                        } else
                            System.out.println(dt.format(new Date()) + " FlagUp: " + flag);
                        row++;
                    }
                else
                    mListener.Uploading("", "", "", "", "", dtRun.format(new Date()));
                SystemClock.sleep(30000);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    void InsertTestData() {
        try {
            SimpleDateFormat dtFolder = new SimpleDateFormat("yyyy_MM_dd");
            File directory = new File("/mnt/sdcard/SURVEY/" + dtFolder.format(new Date()) + "/");
            File[] files = directory.listFiles();
            Log.d("Files", "Size: " + files.length);
            DAL dbe = new DAL(activity, dbName);
            SUR_LOC_IN_IMAGES obj = new SUR_LOC_IN_IMAGES();
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().contains("_small"))
                    continue;
                SimpleDateFormat dt = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                obj = new SUR_LOC_IN_IMAGES();
                obj.setSUR_LOC_IN_IMAGES_ID(dt.format(new Date()));
                obj.setFlag("C");
                obj.setUrlDevice(files[i].getPath());
                obj.setFileName(files[i].getName());
                obj.setFTPFolder("/TEST/");
                obj.setFTPServer("188.88.100.109");
                obj.setFTPUser("survey");
                obj.setFTPPass("survey12345");
                obj.setFTPPort("21");
                dbe.insert_SUR_LOC_IN_IMAGES(obj);
            }
        } catch (Exception ex) {
            Log.d("FTP Async", "InsertTestData: " + ex.getMessage());
        }

    }

    private ArrayList<SUR_LOC_IN_IMAGES> getAllImagesNeedUpload() {
        try {
            try {
                //TelephonyManager tm = (TelephonyManager) activity.getSystemService(activity.getApplicationContext().TELEPHONY_SERVICE);
                String deviceID = Settings.Secure.getString(activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                String strUrl = url + "Sur/GetPostImages/" + deviceID;//"eae30ec4ba63d8f4";
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
                    objIMG.setSUR_LOC_IN_ID(obj.getString("SUR_LOC_IN_ID"));
                    objIMG.setFolder(obj.getString("Folder"));
                    objIMG.setUrlDevice(obj.getString("UrlDevice"));
                    objIMG.setUrlLarge(obj.getString("UrlLarge"));
                    objIMG.setFlag(obj.getString("Flag"));
                    objIMG.setFlagUp(obj.getString("FlagUp"));
                    objIMG.setFileName(obj.getString("FileName"));
                    objIMG.setFTPServer(obj.getString("FTPServer"));
                    objIMG.setFTPPort(obj.getString("FTPPort"));
                    objIMG.setFTPUser(obj.getString("FTPUser"));
                    objIMG.setFTPPass(obj.getString("FTPPass"));
                    objIMG.setFTPFolder(obj.getString("FTPFolder"));
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
            Gson gson =new GsonBuilder().serializeNulls().create();
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

    public String ftpUpload(String srcFilePath, String desFileName,
                            String desDirectory, String host, String username, String password, int port, int running, int total) {
        try {
            System.out.println("Start upload file: " + (run + 1) + "-->" + filePath);
            this.username = username;
            this.password = password;
            this.filePath = srcFilePath;
            this.run = running;
            this.sum = total;
            FTPClient mFTPClient = new FTPClient();
            mFTPClient.setConnectTimeout(30000);

            mFTPClient.connect(host, port); // connecting to the host
            mFTPClient.login(username, password); // Authenticate using username
            mFTPClient.changeWorkingDirectory(desDirectory); // change directory
            //System.out.println("Dest Directory-->" + desDirectory); // to that
            mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
            mFTPClient.setBufferSize(1024); // Truyền 100kb / 1 lần
            final File file = new File(filePath);
            BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(file), 10000);
            mFTPClient.enterLocalPassiveMode();
            //Hiển thị % upload
//            streamListener = new CopyStreamAdapter() {
//                @Override
//                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
//                    int percent = (int) (totalBytesTransferred * 100 / file.length());
//                    //System.out.println("Percent: " + percent + " %");
//                    //mListener.Uploading(filePath, percent, totalBytesTransferred, file.length(), run, sum);
//                    if (totalBytesTransferred == file.length()) {
//                        //System.out.println("100% transfered");
//                        mListener.Uploading(filePath, percent, totalBytesTransferred, file.length(), run, sum);
//                        removeCopyStreamListener(streamListener);
//                    }
//                }
//
//            };
            //mFTPClient.setCopyStreamListener(streamListener);
            boolean status = mFTPClient.storeFile(desFileName, buffIn);
            System.out.println("Status FTP: " + (run + 1) + "-->" + status);
            buffIn.close();
            mFTPClient.logout();
            mFTPClient.disconnect();
            if (status) {
                mListener.Uploading(filePath, "0", "0", "0", Integer.toString ((run + 1))+"/ ", Integer.toString (sum));
                return "U";
            } else
                return "D";
        } catch (Exception e) {
            SimpleDateFormat dtRun = new SimpleDateFormat("HH:mm:ss");
            mListener.Uploading("", "", "", "", "", dtRun.format(new Date()));
            System.out.println("ftpUpload error: " + e.getMessage());
            return "D";
        }
    }

    /*
    * srcFilePath: file on server
    * desFileName: file save to local
    * desDirectory: changeWorkingDirectory on server
    * */
    public Boolean ftpDownload(String srcFilePath, String desFileName, String host, String username, String password, int port) {
        try {
            FTPClient mFTPClient = new FTPClient();
            mFTPClient.connect(host, port); // connecting to the host
            mFTPClient.login(username, password); // Authenticate using username and password
            //mFTPClient.changeWorkingDirectory(desDirectory); // change directory
            mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
            mFTPClient.setBufferSize(1024); // Truyền 100kb / 1 lần
            //final File file = new File(filePath);
            System.out.println("Start download file: " + run + "-->" + desFileName);
            FileOutputStream buffIn = new FileOutputStream(desFileName);
            mFTPClient.enterLocalPassiveMode();
            boolean status = mFTPClient.retrieveFile(srcFilePath, buffIn);
            System.out.println("Status download: " + run + "-->" + status);
            mListener.Uploading(filePath, "0", "0", "0", Integer.toString (run), Integer.toString (sum));
            buffIn.close();
            mFTPClient.logout();
            mFTPClient.disconnect();
            return status;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}