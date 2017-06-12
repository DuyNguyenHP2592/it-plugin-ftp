package it.plugin.ftp;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by XMEN on 12/10/2016.
 */

public class DAL {
    String TAG = "DAL";
    SQLiteDatabase db;

    Activity activity;
    String dbname;

    DAL(Activity a, String dbName) {
        activity = a;
        dbname = dbName;
    }

    private void Open() {
        if (db != null)
            if (db.isOpen())
                db.close();
        File dbfile = activity.getDatabasePath(dbname);
        if (!dbfile.exists()) {
            dbfile.getParentFile().mkdirs();
        }
        db = SQLiteDatabase.openDatabase(dbfile.getAbsolutePath(), null,SQLiteDatabase.OPEN_READWRITE);
        //db = SQLiteDatabase.openDatabase("/16_IT/SURVEY/Database/SURVEY.db", null, SQLiteDatabase.OPEN_READWRITE);
        //Log.d(TAG, "Opened sqlite db: " + dbfile.getAbsolutePath());
    }

    public ArrayList<SUR_LOC_IN_IMAGES> getAll_SUR_LOC_IN_IMAGES() {
        ArrayList<SUR_LOC_IN_IMAGES> list = new ArrayList<SUR_LOC_IN_IMAGES>();
        SUR_LOC_IN_IMAGES obj = new SUR_LOC_IN_IMAGES();
        try {
            Open();
            Cursor c = db.rawQuery("SELECT * FROM SUR_LOC_IN_IMAGES WHERE Flag='C' and FlagUp IS NULL and FTPServer IS NOT NULL ", null);
            while (c.moveToNext()) {
                obj = new SUR_LOC_IN_IMAGES();
                obj.setSUR_LOC_IN_IMAGES_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_IMAGES_ID")));
                obj.setSUR_LOC_IN_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_ID")));
                obj.setFolder(c.getString(c.getColumnIndex("Folder")));
                obj.setUrlDevice(c.getString(c.getColumnIndex("UrlDevice")));
                obj.setUrlLarge(c.getString(c.getColumnIndex("UrlLarge")));
                obj.setFlag(c.getString(c.getColumnIndex("Flag")));
                obj.setFlagUp(c.getString(c.getColumnIndex("FlagUp")));
                obj.setFileName(c.getString(c.getColumnIndex("FileName")));
                obj.setFTPServer(c.getString(c.getColumnIndex("FTPServer")));
                obj.setFTPPort(c.getString(c.getColumnIndex("FTPPort")));
                obj.setFTPUser(c.getString(c.getColumnIndex("FTPUser")));
                obj.setFTPPass(c.getString(c.getColumnIndex("FTPPass")));
                obj.setFTPFolder(c.getString(c.getColumnIndex("FTPFolder")));
                list.add(obj);
            }
            db.close();
        } catch (Exception ex) {
            Log.d(TAG, "getAll_SUR_LOC_IN_IMAGES: " + ex.getMessage());
        }
        return list;
    }

    public SUR_LOC_IN_IMAGES get_SUR_LOC_IN_IMAGES(String id) {
        SUR_LOC_IN_IMAGES obj = new SUR_LOC_IN_IMAGES();
        try {
            Open();
            Cursor c = db.rawQuery("SELECT TOP 1 * FROM SUR_LOC_IN_IMAGES WHERE SUR_LOC_IN_IMAGES_ID='" + id + "'", null);
            if (c.moveToFirst()) {
                obj.setSUR_LOC_IN_IMAGES_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_IMAGES_ID")));
                obj.setSUR_LOC_IN_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_ID")));
                obj.setFolder(c.getString(c.getColumnIndex("Folder")));
                obj.setUrlDevice(c.getString(c.getColumnIndex("UrlDevice")));
                obj.setUrlLarge(c.getString(c.getColumnIndex("UrlLarge")));
                obj.setFlag(c.getString(c.getColumnIndex("Flag")));
                obj.setFlagUp(c.getString(c.getColumnIndex("FlagUp")));
                obj.setFileName(c.getString(c.getColumnIndex("FileName")));
                obj.setFTPServer(c.getString(c.getColumnIndex("FTPServer")));
                obj.setFTPPort(c.getString(c.getColumnIndex("FTPPort")));
                obj.setFTPUser(c.getString(c.getColumnIndex("FTPUser")));
                obj.setFTPPass(c.getString(c.getColumnIndex("FTPPass")));
                obj.setFTPFolder(c.getString(c.getColumnIndex("FTPFolder")));
                db.close();
                return obj;
            } else {
                Log.d(TAG, "Error: getById_SUR_LOC_IN_IMAGES");
                db.close();
                return null;
            }

        } catch (Exception ex) {
            Log.d(TAG, "getById_SUR_LOC_IN_IMAGES: " + ex.getMessage());
            return null;
        }
    }

    public Boolean update_SUR_LOC_IN_IMAGES(SUR_LOC_IN_IMAGES obj) {
        try {
            String sql = "UPDATE SUR_LOC_IN_IMAGES set ";
            sql += obj.getFolder() != null ? " Folder='" + obj.getFolder() + "'," : "";
            sql += obj.getUrlDevice() != null ? " UrlDevice='" + obj.getUrlDevice() + "'," : "";
            sql += obj.getUrlLarge() != null ? " UrlLarge='" + obj.getUrlLarge() + "'," : "";
            sql += obj.getFlag() != null ? " Flag='" + obj.getFlag() + "'," : "";
            sql += obj.getFlagUp() != null ? " FlagUp='" + obj.getFlagUp() + "'," : "";
            sql += obj.getFileName() != null ? " FileName='" + obj.getFileName() + "'," : "";
            sql += obj.getFTPServer() != null ? " FTPServer='" + obj.getFTPServer() + "'," : "";
            sql += obj.getFTPPort() != null ? " FTPPort='" + obj.getFTPPort() + "'," : "";
            sql += obj.getFTPUser() != null ? " FTPUser='" + obj.getFTPUser() + "'," : "";
            sql += obj.getFTPPass() != null ? " FTPPass='" + obj.getFTPPass() + "'," : "";
            sql += obj.getFTPFolder() != null ? " FTPFolder='" + obj.getFTPFolder() + "'," : "";
            sql = sql.substring(0, sql.length() - 1) + " where SUR_LOC_IN_IMAGES_ID='" + obj.getSUR_LOC_IN_IMAGES_ID() + "'";
            Open();
            db.execSQL(sql);
            db.close();
            Log.d(TAG, "update_SUR_LOC_IN_IMAGES: " + sql);
            return true;
        } catch (Exception ex) {
            Log.d(TAG, "update_SUR_LOC_IN_IMAGES: " + ex.getMessage());
            return false;
        }
    }

    public Boolean insert_SUR_LOC_IN_IMAGES(SUR_LOC_IN_IMAGES obj) {
        try {
            String sql = "INSERT INTO SUR_LOC_IN_IMAGES (SUR_LOC_IN_IMAGES_ID,UrlDevice,Flag,FileName,FTPServer,FTPPort,FTPUser,FTPPass,FTPFolder) values('<SUR_LOC_IN_IMAGES_ID>','<UrlDevice>','<Flag>','<FileName>','<FTPServer>','<FTPPort>','<FTPUser>','<FTPPass>','<FTPFolder>') ";
            sql = sql.replace("<SUR_LOC_IN_IMAGES_ID>", obj.getSUR_LOC_IN_IMAGES_ID());
            sql = sql.replace("<UrlDevice>", obj.getUrlDevice());
            sql = sql.replace("<Flag>", obj.getFlag());
            sql = sql.replace("<FileName>", obj.getFileName());
            sql = sql.replace("<FTPServer>", obj.getFTPServer());
            sql = sql.replace("<FTPPort>", obj.getFTPPort());
            sql = sql.replace("<FTPUser>", obj.getFTPUser());
            sql = sql.replace("<FTPPass>", obj.getFTPPass());
            sql = sql.replace("<FTPFolder>", obj.getFTPFolder());
            Open();
            db.execSQL(sql);
            db.close();
            Log.d(TAG, "insert_SUR_LOC_IN_IMAGES: " + sql);
            return true;
        } catch (Exception ex) {
            Log.d(TAG, "insert_SUR_LOC_IN_IMAGES: " + ex.getMessage());
            return false;
        }
    }

    public ArrayList<SUR_LOC_IN_IMAGES> getAll_SUR_LOC_IN_IMAGES(String strSUR_LOC_IN_ID) {
        ArrayList<SUR_LOC_IN_IMAGES> list = new ArrayList<SUR_LOC_IN_IMAGES>();
        SUR_LOC_IN_IMAGES obj = new SUR_LOC_IN_IMAGES();
        try {
            Open();
            Cursor c = db.rawQuery("SELECT * FROM SUR_LOC_IN_IMAGES WHERE SUR_LOC_IN_ID='"+strSUR_LOC_IN_ID+"' ", null);
            while (c.moveToNext()) {
                obj = new SUR_LOC_IN_IMAGES();
                obj.setSUR_LOC_IN_IMAGES_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_IMAGES_ID")));
                obj.setSUR_LOC_IN_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_ID")));
                obj.setSUR_LOC_IN_ERROR_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_ERROR_ID")));
                obj.setSUR_LOC_IN_FEE_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_FEE_ID")));
                obj.setSUR_THU_TU_GD_ID(c.getString(c.getColumnIndex("SUR_THU_TU_GD_ID")));
                obj.setPositionCode(c.getString(c.getColumnIndex("PositionCode")));
                obj.setFolder(c.getString(c.getColumnIndex("Folder")));
                obj.setUrlDevice(c.getString(c.getColumnIndex("UrlDevice")));
                obj.setUrlLarge(c.getString(c.getColumnIndex("UrlLarge")));
                obj.setFlag(c.getString(c.getColumnIndex("Flag")));
                obj.setFlagUp(c.getString(c.getColumnIndex("FlagUp")));
                obj.setFileName(c.getString(c.getColumnIndex("FileName")));
                obj.setFTPServer(c.getString(c.getColumnIndex("FTPServer")));
                obj.setFTPPort(c.getString(c.getColumnIndex("FTPPort")));
                obj.setFTPUser(c.getString(c.getColumnIndex("FTPUser")));
                obj.setFTPPass(c.getString(c.getColumnIndex("FTPPass")));
                obj.setFTPFolder(c.getString(c.getColumnIndex("FTPFolder")));
                list.add(obj);
            }
            db.close();
        } catch (Exception ex) {
            Log.d(TAG, "getAll_SUR_LOC_IN_IMAGES(String strSUR_LOC_IN_ID): " + ex.getMessage());
        }
        return list;
    }

    public ArrayList<SUR_LOC_IN> getAll_SUR_LOC_IN(String strFlag){
        ArrayList<SUR_LOC_IN> list = new ArrayList<SUR_LOC_IN>();
        SUR_LOC_IN obj = new SUR_LOC_IN();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate;
        try {
            Open();
            Cursor c = db.rawQuery("SELECT * FROM SUR_LOC_IN WHERE FlagUp='F' ", null);
            while (c.moveToNext()) {
                obj = new SUR_LOC_IN();
                obj.setSUR_LOC_IN_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_ID")));
                obj.setYD_OperateDynamicCode(c.getString(c.getColumnIndex("YD_OperateDynamicCode")));
                obj.setOPTRCode(c.getString(c.getColumnIndex("OPTRCode")));
                obj.setCNTRNo(c.getString(c.getColumnIndex("CNTRNo")));
                obj.setCNTRSize(c.getString(c.getColumnIndex("CNTRSize")));
                obj.setCNTRType(c.getString(c.getColumnIndex("CNTRType")));
                obj.setEF(c.getString(c.getColumnIndex("EF")));
                obj.setSealNo(c.getString(c.getColumnIndex("SealNo")));
                obj.setCntrGrossWeight(c.getFloat(c.getColumnIndex("CntrGrossWeight")));
                obj.setCNTRGrade(c.getString(c.getColumnIndex("CNTRGrade")));
                obj.setCNTRAges(c.getString(c.getColumnIndex("CNTRAges")));
                obj.setCO_CNTRStatusCode(c.getString(c.getColumnIndex("CO_CNTRStatusCode")));
                obj.setTruckNo(c.getString(c.getColumnIndex("TruckNo")));
                obj.setDriverName(c.getString(c.getColumnIndex("DriverName")));
                obj.setDriverPhone(c.getString(c.getColumnIndex("DriverPhone")));
                obj.setCustomerName(c.getString(c.getColumnIndex("CustomerName")));
                obj.setUC_UserID(c.getInt(c.getColumnIndex("UC_UserID")));
                obj.setDeviceID(c.getString(c.getColumnIndex("DeviceID")));
                obj.setRemark(c.getString(c.getColumnIndex("Remark")));
                obj.setFlagUp(c.getString(c.getColumnIndex("FlagUp")));
                obj.setPLAN_CNTRID(c.getFloat(c.getColumnIndex("PLAN_CNTRID")));
                obj.setDEPOT(c.getString(c.getColumnIndex("DEPOT")));
                obj.setUpdateUser(c.getInt(c.getColumnIndex("UpdateUser")));
                obj.setBLNo(c.getString(c.getColumnIndex("BLNo")));
                obj.setReCheck(c.getString(c.getColumnIndex("ReCheck")));
                obj.setOptions(c.getString(c.getColumnIndex("Options")));
                obj.setRemarkOther(c.getString(c.getColumnIndex("RemarkOther")));
                obj.setCreateTime(c.getString(c.getColumnIndex("CreateTime")));
                obj.setUpdateTime(c.getString(c.getColumnIndex("UpdateTime")));
                obj.setParentID(c.getString(c.getColumnIndex("ParentID")));
                list.add(obj);
            }
            db.close();
        } catch (Exception ex) {
            Log.d(TAG, "getAll_SUR_LOC_IN(String strFlag): " + ex.getMessage());
        }
        return list;
    }

    public ArrayList<SUR_LOC_IN_ERROR> getAll_SUR_LOC_IN_ERROR(String strSUR_LOC_IN_ID) {
        ArrayList<SUR_LOC_IN_ERROR> list = new ArrayList<SUR_LOC_IN_ERROR>();
        SUR_LOC_IN_ERROR obj = new SUR_LOC_IN_ERROR();
        try {
            Open();
            Cursor c = db.rawQuery("SELECT * FROM SUR_LOC_IN_ERROR WHERE SUR_LOC_IN_ID='"+strSUR_LOC_IN_ID+"' ", null);
            while (c.moveToNext()) {
                obj = new SUR_LOC_IN_ERROR();
                obj.setSUR_LOC_IN_ERROR_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_ERROR_ID")));
                obj.setSUR_LOC_IN_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_ID")));
                obj.setSUR_THU_TU_GD_ID(c.getString(c.getColumnIndex("SUR_THU_TU_GD_ID")));
                obj.setCode_SUR_VAT_TU(c.getString(c.getColumnIndex("Code_SUR_VAT_TU")));
                obj.setCode_SUR_VI_TRI(c.getString(c.getColumnIndex("Code_SUR_VI_TRI")));
                obj.setCode_SUR_HU_HAI(c.getString(c.getColumnIndex("Code_SUR_HU_HAI")));
                obj.setCode_SUR_SUA_CHUA(c.getString(c.getColumnIndex("Code_SUR_SUA_CHUA")));
                obj.setWeight(c.getFloat(c.getColumnIndex("Weight")));
                obj.setHeight(c.getFloat(c.getColumnIndex("Height")));
                obj.setLenght(c.getFloat(c.getColumnIndex("Lenght")));
                obj.setWidth(c.getFloat(c.getColumnIndex("Width")));
                obj.setQuantity(c.getString(c.getColumnIndex("Quantity")));
                obj.setMeasure(c.getString(c.getColumnIndex("Measure")));
                obj.setFlagUp(c.getString(c.getColumnIndex("FlagUp")));
                obj.setCreateTime(c.getString(c.getColumnIndex("CreateTime")));
                obj.setRemark(c.getString(c.getColumnIndex("Remark")));
                obj.setCodeRemark(c.getString(c.getColumnIndex("CodeRemark")));
                obj.setDeep(c.getFloat(c.getColumnIndex("Deep")));
                obj.setLenght_Repair(c.getFloat(c.getColumnIndex("Lenght_Repair")));
                obj.setWidth_Repair(c.getFloat(c.getColumnIndex("Width_Repair")));
                obj.setDeep_Repair(c.getFloat(c.getColumnIndex("Deep_Repair")));
                obj.setSoLuongKichThuoc(c.getInt(c.getColumnIndex("SoLuongKichThuoc")));
                list.add(obj);
            }
            db.close();
        } catch (Exception ex) {
            Log.d(TAG, "getAll_SUR_LOC_IN_IMAGES(String strSUR_LOC_IN_ID): " + ex.getMessage());
        }
        return list;
    }

    public ArrayList<SUR_LOC_IN_FEE> getAll_SUR_LOC_IN_FEE(String strSUR_LOC_IN_ID) {
        ArrayList<SUR_LOC_IN_FEE> list = new ArrayList<SUR_LOC_IN_FEE>();
        SUR_LOC_IN_FEE obj = new SUR_LOC_IN_FEE();
        try {
            Open();
            Cursor c = db.rawQuery("SELECT * FROM SUR_LOC_IN_FEE WHERE SUR_LOC_IN_ID='"+strSUR_LOC_IN_ID+"' ", null);
            while (c.moveToNext()) {
                obj = new SUR_LOC_IN_FEE();
                obj.setSUR_LOC_IN_FEE_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_FEE_ID")));
                obj.setSUR_LOC_IN_ID(c.getString(c.getColumnIndex("SUR_LOC_IN_ID")));
                obj.setFEENo(c.getString(c.getColumnIndex("FEENo")));
                obj.setFeeName(c.getString(c.getColumnIndex("FeeName")));
                obj.setNum(c.getInt(c.getColumnIndex("Num")));
                obj.setMeasure(c.getString(c.getColumnIndex("Measure")));
                obj.setPrice(c.getFloat(c.getColumnIndex("Price")));
                obj.setAmount(c.getFloat(c.getColumnIndex("Amount")));
                obj.setPaymode(c.getString(c.getColumnIndex("Paymode")));
                obj.setRemark(c.getString(c.getColumnIndex("Remark")));
                obj.setCreateTime(c.getString(c.getColumnIndex("CreateTime")));
                obj.setFlagUp(c.getString(c.getColumnIndex("FlagUp")));
                list.add(obj);
            }
            db.close();
        } catch (Exception ex) {
            Log.d(TAG, "getAll_SUR_LOC_IN_IMAGES(String strSUR_LOC_IN_ID): " + ex.getMessage());
        }
        return list;
    }

    public Boolean update_SUR_LOC_IN(SUR_LOC_IN obj) {
        try {
            String sql = "UPDATE SUR_LOC_IN set ";
            sql += obj.getFlagUp() != null ? " FlagUp='" + obj.getFlagUp() + "'," : "";
            sql = sql.substring(0, sql.length() - 1) + " where SUR_LOC_IN_ID='" + obj.getSUR_LOC_IN_ID() + "'";
            Open();
            db.execSQL(sql);
            db.close();
            Log.d(TAG, "update_SUR_LOC_IN: " + sql);
            return true;
        } catch (Exception ex) {
            Log.d(TAG, "update_SUR_LOC_IN: " + ex.getMessage());
            return false;
        }
    }
}
