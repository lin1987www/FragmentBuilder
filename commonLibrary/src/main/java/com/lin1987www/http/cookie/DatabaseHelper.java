package com.lin1987www.http.cookie;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getName();
    private static final String DATABASE_NAME = "HttpCookies";
    private static final int DATABASE_VERSION = 1;

    private static String getDirPath(Context context) {
        // SD Card 路徑 /sdcard/Android/data/<package_name>/files
        // http://stackoverflow.com/questions/1209469/storing-android-application-data-on-sd-card
        // 預設資料庫路徑 /data/data/<package_name>/databases/
        String packageName = context.getApplicationContext().getPackageName();
        String SDCardPath = android.os.Environment
                .getExternalStorageDirectory().getAbsolutePath();
        String dataBaseDirPath = String.format("%s/Android/data/%s/databases/",
                SDCardPath, packageName);
        File dir = new File(dataBaseDirPath);
        if (!dir.exists()) {
            synchronized (File.class) {
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        Log.e(TAG, String.format("ERROR! db dir path: %s",
                                dataBaseDirPath));
                        throw new RuntimeException("建立資料庫資料夾失敗");
                    }
                }
            }
        }
        Log.d(TAG, String.format("db dir path: %s", dataBaseDirPath));
        return dataBaseDirPath;
    }

    private static String getFilePath(Context context) {
        String dirPath = getDirPath(context);
        String filePath = String.format("%s/%s.db", dirPath, DATABASE_NAME);
        Log.d(TAG, String.format("db file path: %s", filePath));
        return filePath;
    }

    private static String GetDataBaseName(Context context) {
        return getFilePath(context);
    }

    public DatabaseHelper(Context context) {
        super(context, GetDataBaseName(context), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase,
                         ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Cookie.class);
        } catch (SQLException e) {
            Log.e(TAG, "Could not create new table for Thing", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,
                          ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Cookie.class, true);
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "Could not upgrade the table for Thing", e);
        }
    }

    private Dao<Cookie, String> cookieDao;

    public Dao<Cookie, String> getCookieDao() throws SQLException {
        if (cookieDao == null) {
            cookieDao = getDao(Cookie.class);
        }
        return cookieDao;
    }

    @Override
    public void close() {
        super.close();
        cookieDao = null;
    }
}
