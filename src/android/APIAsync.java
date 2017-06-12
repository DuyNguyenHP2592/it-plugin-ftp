package it.plugin.ftp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by XMEN on 12/21/2016.
 */

public class APIAsync extends AsyncTask<Void, Integer, Boolean> {
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
    public APIAsync(Activity ctx, ftpLauncher listener) {
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
                Gson gson =new GsonBuilder().serializeNulls().create(); //new Gson();
                SimpleDateFormat dtRun = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                System.out.println(dt.format(new Date()) + " Start PostGateIn");
                DAL dbe = new DAL(activity, dbName);
                ArrayList<SUR_LOC_IN> listSUR_LOC_IN = new ArrayList<SUR_LOC_IN>();
                ArrayList<SUR_LOC_IN_ERROR> listSUR_LOC_IN_ERROR = new ArrayList<SUR_LOC_IN_ERROR>();
                ArrayList<SUR_LOC_IN_FEE> listSUR_LOC_IN_FEE = new ArrayList<SUR_LOC_IN_FEE>();
                ArrayList<SUR_LOC_IN_IMAGES> listSUR_LOC_IN_IMAGES = new ArrayList<SUR_LOC_IN_IMAGES>();
                listSUR_LOC_IN = dbe.getAll_SUR_LOC_IN("F");
                int row = 1;
                int count = listSUR_LOC_IN == null ? 0 : listSUR_LOC_IN.size();
                if (count > 0)
                    for (SUR_LOC_IN item : listSUR_LOC_IN) {
                        item.setFlagUp("U");
                        listSUR_LOC_IN_ERROR = dbe.getAll_SUR_LOC_IN_ERROR(item.getSUR_LOC_IN_ID());
                        listSUR_LOC_IN_FEE = dbe.getAll_SUR_LOC_IN_FEE(item.getSUR_LOC_IN_ID());
                        listSUR_LOC_IN_IMAGES = dbe.getAll_SUR_LOC_IN_IMAGES(item.getSUR_LOC_IN_ID());

                        String jsonSUR_LOC_IN = gson.toJson(item, new TypeToken<SUR_LOC_IN>() {
                        }.getType());

                        String jsonSUR_LOC_IN_ERROR = gson.toJson(listSUR_LOC_IN_ERROR, new TypeToken<List<SUR_LOC_IN_ERROR>>() {
                        }.getType());

                        String jsonSUR_LOC_IN_FEE = gson.toJson(listSUR_LOC_IN_FEE, new TypeToken<List<SUR_LOC_IN_FEE>>() {
                        }.getType());

                        String jsonSUR_LOC_IN_IMAGES = gson.toJson(listSUR_LOC_IN_IMAGES, new TypeToken<List<SUR_LOC_IN_IMAGES>>() {
                        }.getType());
                        System.out.println("Json post:"+("{SUR_LOC_IN:"+ jsonSUR_LOC_IN +",SUR_LOC_IN_ERROR:"+ jsonSUR_LOC_IN_ERROR +",SUR_LOC_IN_FEE:"+ jsonSUR_LOC_IN_FEE +",SUR_LOC_IN_IMAGES:"+ jsonSUR_LOC_IN_IMAGES +"}").replace("\"null\"","null").replace("\"NULL\"","null"));
                        Boolean bStatus = PostGateIn(("{SUR_LOC_IN:"+ jsonSUR_LOC_IN +",SUR_LOC_IN_ERROR:"+ jsonSUR_LOC_IN_ERROR +",SUR_LOC_IN_FEE:"+ jsonSUR_LOC_IN_FEE +",SUR_LOC_IN_IMAGES:"+ jsonSUR_LOC_IN_IMAGES +"}").replace("\"null\"","null").replace("\"NULL\"","null"));
                        if (bStatus) {
                            dbe.update_SUR_LOC_IN(item);
                            mListener.APIUploading(item.getCNTRNo(),Integer.toString(row) +"/ ",Integer.toString(count));
                        }
                        row++;
                        System.out.println("PostGateIn: " + bStatus);
                    }
                else
                mListener.APIUploading("", "",dtRun.format(new Date()));
                SystemClock.sleep(30000);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean PostGateIn(String json) {
        try {

            String strUrl = url + "Sur/PostGateIn";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, json);
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(60, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(60, TimeUnit.SECONDS);
            Request request = new Request.Builder().url(strUrl).post(body).build();
            Response responses = client.newCall(request).execute();

            if (("200,204").contains(Integer.toString(responses.code())))
                return true;
            else {
                System.out.println(responses);
                return false;
            }
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