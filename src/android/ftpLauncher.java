package it.plugin.ftp;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class ftpLauncher extends CordovaPlugin {
    public static final String TAG = ftpLauncher.class.getSimpleName();
    public CallbackContext callbackContext;
    public static int index = 0;
    public static int maxCount = 0;
    public static ArrayList<String> arrayURL = null;
    public static String url = "";
    public static String fileName = "";
    public static FTPClient ftpClient;
    public static String nameFileDownload = "";
    //public static String indexRecord;

    public static String[] arrayString = null;
    private CallbackContext uploadCallbackContext;
    private CallbackContext APICallbackContext;
    private FTPAsync fragment;
    private APIAsync fragmentAPI;
    private FileAsync fragmentFile;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init plugin");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        //this.callbackContext = callbackContext;
        //Log.v(TAG, "Plugin performs action:" + action);
        if (action == null || action == "") {
            return false;
        } else {
            //try {
            //String urlJsonString = args.getString(0);
            //indexRecord = args.getString(1);
            if (action.equals("downloadAsciiString")) {
                //cordova.getThreadPool().execute(new DownloadAsAsciiString(url, callbackContext));
            } else if (action.equals("downloadAsciifile")) {
                //cordova.getThreadPool().execute(new DownloadAsFile(filename, true, url, callbackContext));
            } else if (action.equals("downloadBinaryFile")) {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        if (nameFileDownload != null && !nameFileDownload.isEmpty() && nameFileDownload.length() > 0) {
                            ConnectURLServer();
                        }
                    }
                });
                //cordova.getThreadPool().execute(new DownloadAsFile(filename, false, url, callbackContext));
            } else if (action.equals("upload")) {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        if (maxCount >= 0) {
                            ConnetAndLogin();
                        }
                    }
                });

            } else if (action.equals("upFile")) {
                Log.d("TAG", "In plugin");
                return upFile(args, callbackContext);
            } else if (action.equals("uploadProcess")) {
                Log.d("TAG", "uploadProcess");
                return uploadProcess(callbackContext);
            } else if (action.equals("postGateInProcess")) {
                Log.d("TAG", "postGateInProcess");
                return postGateInProcess(callbackContext);
            } else if (action.equals("startUpload")) {
                Log.d("TAG", "startUpload");
                return startUpload(callbackContext);
            } else if (action.equals("stopUpload")) {
                Log.d("TAG", "stopUpload");
                return stopUpload(callbackContext);
            } else if (action.equals("download")) {
                Log.d("TAG", "download");
                return download(args, callbackContext);//
            } else if (action.equals("connectWifi")) {
                Log.d("TAG", "connectWifi");
                return connectWifi(args, callbackContext);
            } else if (action.equals("deleteFile")) {
                Log.d("TAG", "deleteFile");
                return deleteFile(args, callbackContext);
            } else {
                // Action does not match!
                return false;
            }
//            } catch (JSONException e) {
//                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
//                Log.v(TAG, "JSON_EXCEPTION" + e.getMessage());
//            }
            return true;
        }
    }

    public boolean connectWifi(JSONArray args, CallbackContext callbackContext) {
        try {
            String SSID = args.getString(0);
            String Password = args.getString(1);
            addWifiConfig(SSID, Password, "WPA2", "AES");
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        } catch (Exception ex) {		
            Log.d(TAG, ex.getMessage());
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            return false;
        }
    }

    public boolean uploadProcess(CallbackContext callbackContext) {
        uploadCallbackContext = callbackContext;
        return true;
    }

    public void addWifiConfig(String ssid, String password, String securityParam, String securityDetailParam) {
         Log.d(TAG, "Start connect ssid: " + ssid + " pass: " + password + " securityParam: " + securityParam + " securityDetailParam: " + securityDetailParam);
        if (ssid == null) {
            throw new IllegalArgumentException("Required parameters can not be NULL #");
        }

        String BACKSLASH = "\"";
        //String wifiName = ssid;
        WifiConfiguration conf = new WifiConfiguration();
        // On devices with version Kitkat and below, We need to send SSID name
        // with double quotes. On devices with version Lollipop, We need to send
        // SSID name without double quotes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            conf.SSID = ssid;
        } else {
            conf.SSID = BACKSLASH + ssid + BACKSLASH;
        }
        //String security = securityParam;
        Log.d(TAG, "Security Type : " + securityParam);
        if (securityParam.equalsIgnoreCase("WEP")) {
            conf.wepKeys[0] = password;
            conf.wepTxKeyIndex = 0;
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if (securityParam.equalsIgnoreCase("NONE")) {
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if ("WPA".equalsIgnoreCase(securityParam)
                || "WPA2".equalsIgnoreCase(securityParam)
                || "WPA/WPA2 PSK".equalsIgnoreCase(securityParam)) {
            // appropriate ciper is need to set according to security type used,
            // ifcase of not added it will not be able to connect
            conf.preSharedKey = BACKSLASH + password + BACKSLASH;
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        }
        //String securityDetails = securityDetailParam;
        if (securityDetailParam.equalsIgnoreCase("TKIP")) {
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        } else if (securityDetailParam.equalsIgnoreCase("AES")) {
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        } else if (securityDetailParam.equalsIgnoreCase("WEP")) {
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if (securityDetailParam.equalsIgnoreCase("NONE")) {
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
        }
		
        Log.d(TAG, "Setup wifi ok");
        WifiManager wifiManager = (WifiManager) cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        int newNetworkId = wifiManager.addNetwork(conf);
        wifiManager.enableNetwork(newNetworkId, true);
        wifiManager.saveConfiguration();
        wifiManager.setWifiEnabled(true);
        Log.d(TAG, "Connect successful: " + ssid);
    }

    public boolean postGateInProcess(CallbackContext callbackContext) {
        APICallbackContext = callbackContext;
        return true;
    }

    private boolean startUpload(CallbackContext callbackContext) {
        try {

            if (fragment != null) {
                fragment.bAlowRun = false;
                fragment.cancel(true);
                fragment = null;
            }
            if (fragmentAPI != null) {
                fragmentAPI.bAlowRun = false;
                fragmentAPI.cancel(true);
                fragmentAPI = null;
            }
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            //FTP upload image
            fragment = new FTPAsync(cordova.getActivity(), this);
            fragment.bAlowRun = true;
            fragment.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            //API Post Gate-in
            fragmentAPI = new APIAsync(cordova.getActivity(), this);
            fragmentAPI.bAlowRun = true;
            fragmentAPI.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            //API delete old images
            fragmentFile = new FileAsync(cordova.getActivity(), this);
            fragmentFile.bAlowRun = true;
            fragmentFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return true;
        } catch (Exception ex) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            return false;
        }
    }

    public boolean deleteFile(JSONArray args, CallbackContext callbackContext) {
        try {
            String filePath = args.getString(0);
            DeleteFile(filePath);
            DeleteFile(filePath.replace(".jpg", "_small.jpg"));
            System.out.println("Delete image successful: " + args.getString(0));
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        } catch (Exception ex) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            return false;
        }
    }

    private void DeleteFile(String url) {
        try {
            File file = new File(url);
            if (file.exists()) {
                file.delete();
                if (file.exists()) {
                    file.getCanonicalFile().delete();
                    if (file.exists()) {
                        cordova.getActivity().getApplicationContext().deleteFile(file.getName());
                    }
                }
            }

        } catch (Exception ex) {
        }
    }

    private boolean stopUpload(CallbackContext callbackContext) {
        try {
            if (fragment == null)
                return true;
            fragment.bAlowRun = false;
            fragment.cancel(true);
            fragment = null;
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        } catch (Exception ex) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            return false;
        }
    }


    public boolean upFile(JSONArray args, CallbackContext callbackContext) {
        try {
            Log.d("TAG", "Run UpFile");
            Activity activity = cordova.getActivity();
            FTPAsync ftp = new FTPAsync(activity, this);
            ftp.directory = "/16_IT/SURVEY/";
            ftp.filePath = "/mnt/sdcard/SURVEY/img1.jpg";
            ftp.filename = "img1.jpg";
            ftp.hostName = "188.88.100.100";
            ftp.port = 21;
            ftp.username = "it.quyen";
            ftp.password = "gh0st123!@#";
            ftp.execute();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        } catch (Exception ex) {
            PluginResult r = new PluginResult(PluginResult.Status.ERROR);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            Log.d("TAG", ex.getMessage());
            return false;
        }
    }

    public void Uploading(String file, String percent, String size, String totalSize, String run, String total) {
        JSONObject obj = new JSONObject();
        JSONArray json = new JSONArray();
        try {
            obj.put("file", file);
            obj.put("percent", percent);
            obj.put("size", size);
            obj.put("totalSize", totalSize);
            obj.put("run", run);
            obj.put("total", total);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        json.put(obj);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        uploadCallbackContext.sendPluginResult(pluginResult);
    }

    public void APIUploading(String cntr, String run, String total) {
        JSONObject obj = new JSONObject();
        JSONArray json = new JSONArray();
        try {
            obj.put("cntr", cntr);
            obj.put("run", run);
            obj.put("total", total);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        json.put(obj);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        APICallbackContext.sendPluginResult(pluginResult);
    }

    OkHttpClient client = new OkHttpClient();

    private String run(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            Log.d(TAG, "download: " + response.body().string());
            return response.body().string();
        } catch (IOException ex) {
            return null;
        }
    }

    /*
    *
    * */
    private boolean download(final JSONArray args, CallbackContext callbackContext) {
        try {
            Boolean status = ftpDownload(args.getString(4), args.getString(5), args.getString(6),
                    args.getString(0), args.getString(1), args.getString(2), args.getInt(3));
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, status);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        } catch (Exception ex) {
            System.out.println("download: " + ex.getMessage());
            return false;
        }
    }

    public JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);

        urlConnection.setDoOutput(true);

        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        char[] buffer = new char[1024];

        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        jsonString = sb.toString();

        System.out.println("JSON: " + jsonString);

        return new JSONObject(jsonString);
    }

    /**
     * Download file from FTP server
     *
     * @param srcFTPFile   file on server
     * @param srcLocalFile file save to local
     * @param host         changeWorkingDirectory on server
     * @param username
     * @param password
     * @param port
     */
    public Boolean ftpDownload(String srcFTPFile, String srcLocalFile, String fileName, String host, String username, String password, int port) {
        try {
            System.out.println("FTP download file: -->" + srcFTPFile);
            FTPClient mFTPClient = new FTPClient();
            mFTPClient.connect(host, port); // connecting to the host
            mFTPClient.login(username, password); // Authenticate using username and password
            //mFTPClient.changeWorkingDirectory(desDirectory); // change directory
            mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
            mFTPClient.setBufferSize(1024); // Truyền 100kb / 1 lần
            File file;
            if (srcLocalFile.contains("#"))
                file = new File(cordova.getActivity().getDatabasePath(fileName).getAbsolutePath());
            else
                file = new File(srcLocalFile);
            FileOutputStream buffIn = new FileOutputStream(file.getAbsolutePath());
            //mFTPClient.enterLocalPassiveMode();
            boolean status = mFTPClient.retrieveFile(srcFTPFile, buffIn);
            System.out.println("FTP status: -->" + status);
            buffIn.close();
            mFTPClient.logout();
            mFTPClient.disconnect();
            return status;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void ConnetAndLogin() {
        //Ip server
        String server = "188.88.100.100";
        //Port server
        int port = 21;
        String user = "it.duy";
        String pass = "duy123";
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            showServerReply(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                this.callbackContext.error("Operation failed. Server reply code: " + replyCode);
            }
            boolean success = ftpClient.login(user, pass);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            showServerReply(ftpClient);
            if (!success) {
                ftpClient.logout();
                ftpClient.disconnect();
                PluginResult r = new PluginResult(PluginResult.Status.OK, "Disconnect network. Connect to server and Login failed");
                r.setKeepCallback(true);
                callbackContext.sendPluginResult(r);
            }

            if (maxCount == 0) {
                UploadSingleFileToServer(ftpClient, url, fileName);
            } else if (maxCount > 0) {
                UploadListFileToServer(ftpClient, url, fileName);
            }
        } catch (IOException ex) {
            PluginResult r = new PluginResult(PluginResult.Status.ERROR);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
        }
    }

    public void UploadListFileToServer(FTPClient ftpClient, String fullPathFile, String fileName2) throws IOException {
        InputStream input = new FileInputStream(new File(fullPathFile));
        String movePath = "/16_IT/SURVEY/" + fileName2;
        boolean success = ftpClient.storeFile(movePath, input);
        if (success) {
            index = index + 1;
            if (index < maxCount) {
                //ftpClient.logout();
                //ftpClient.disconnect();

                String ms = url + "|Success";
                PluginResult result = new PluginResult(PluginResult.Status.OK, ms);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);

                url = arrayString[index].toString();
                fileName = url.split("/")[4].toString();
                UploadListFileToServer(ftpClient, url, fileName);
            } else if (index >= maxCount) {
                ftpClient.logout();
                ftpClient.disconnect();
                String ms = "Upload total file success";
                PluginResult result = new PluginResult(PluginResult.Status.OK, ms);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        } else {
            ftpClient.logout();
            ftpClient.disconnect();
            String ms = "Disconnect network. Upload file failed.";
            PluginResult result = new PluginResult(PluginResult.Status.OK, ms);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    public void UploadSingleFileToServer(FTPClient ftpClient, String fullPathFile, String fileName2) throws IOException {
//        InputStream input = new FileInputStream(new File(fullPathFile));
//        String movePath = "/16_IT/SURVEY/" + fileName2;
//        boolean success = ftpClient.storeFile(movePath, input);
//        if (success) {
//            ftpClient.logout();
//            ftpClient.disconnect();
//            String ms = url + "|Success|" + indexRecord;
//            PluginResult result = new PluginResult(PluginResult.Status.OK, ms);
//            result.setKeepCallback(true);
//            callbackContext.sendPluginResult(result);
//        } else {
//            ftpClient.logout();
//            ftpClient.disconnect();
//            String ms = "Disconnect network. Upload file failed.";
//            PluginResult result = new PluginResult(PluginResult.Status.OK, ms);
//            result.setKeepCallback(true);
//            callbackContext.sendPluginResult(result);
//        }
    }

    private static void showServerReply(FTPClient ftpClient1) {
        String[] replies = ftpClient1.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }

    public void ConnectURLServer() {
        try {
            DownloadFileFromServer(nameFileDownload);
        } catch (IOException ex) {
            PluginResult r = new PluginResult(PluginResult.Status.ERROR);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
        }
    }

    public void DownloadFileFromServer(String nameFileDownload) throws IOException {
        try {

            String ftpUrl = "ftp://it.duy:duy123@192.168.0.106:21/16_IT/SURVEY/" + nameFileDownload;
            URL url = new URL(ftpUrl);
            URLConnection conn = url.openConnection();
            InputStream inputStream = conn.getInputStream();
            String pathMove = "mnt/sdcard/SURVEY/" + nameFileDownload;
            FileOutputStream outputStream = new FileOutputStream(pathMove);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            String ms = pathMove;
            PluginResult result = new PluginResult(PluginResult.Status.OK, ms);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
