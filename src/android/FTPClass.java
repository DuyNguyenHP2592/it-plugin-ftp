package it.plugin.ftp;

import android.app.ProgressDialog;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by XMEN on 12/1/2016.
 */

public class FTPClass {
    public FTPClient mFTPClient = null;
    public String host;
    public String username;
    public String password;
    CopyStreamAdapter streamListener;
    ProgressDialog pDialog;
    boolean status = false;

    public boolean ftpUpload(String srcFilePath, String desFileName,
                             String desDirectory, String host, String username, String password, int port,
                             final ProgressDialog pDialog) {
        this.pDialog = pDialog;

        this.host = host;
        this.username = username;
        this.password = password;
        mFTPClient = new FTPClient();

        try {
            mFTPClient.connect(host, port); // connecting to the host
            mFTPClient.login(username, password); // Authenticate using username
            // and password
            mFTPClient.changeWorkingDirectory(desDirectory); // change directory
            System.out.println("Dest Directory-->" + desDirectory); // to that
            // directory
            // where image
            // will be
            // uploaded
            mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
            mFTPClient.setBufferSize (1024); // Truyền 100kb / 1 lần
            final File file = new File(srcFilePath);
            System.out.println("on going file-->" + srcFilePath);
            BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(file), 10000);
            mFTPClient.enterLocalPassiveMode();
            streamListener = new CopyStreamAdapter() {
                @Override
                public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                    int percent = (int) (totalBytesTransferred * 100 /file.length());
                    //pDialog.setProgress(percent);
                    Log.d("TAG","Percent: " +percent+" %");
                    if (totalBytesTransferred == file.length()) {
                        System.out.println("100% transfered");
                        removeCopyStreamListener(streamListener);
                    }
                }

            };
            mFTPClient.setCopyStreamListener(streamListener);
            status = mFTPClient.storeFile(desFileName, buffIn);
            System.out.println("Status Value-->" + status);
            buffIn.close();
            mFTPClient.logout();
            mFTPClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }
//    CopyStreamAdapter copyStreamAdapter = new CopyStreamAdapter() {
//        @Override
//        public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
//            System.out.println("totalBytesTransferred: " + Long.toString(totalBytesTransferred));
//        }
//    };
}
