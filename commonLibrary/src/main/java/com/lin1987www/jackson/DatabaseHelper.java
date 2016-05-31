package com.lin1987www.jackson;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getName();
    private static final String DATABASE_NAME = "JavaTypeRequest";
    private static final int DATABASE_VERSION = 2;

    private static Map<Context, DatabaseHelper> mDatabaseHelperMap = new WeakHashMap<Context, DatabaseHelper>();
    private static final Object mLock = new Object();
    public static boolean enableApplicationContext = false;

    public static DatabaseHelper getDatabaseHelper(Context context) {
        // 必須相依 context
        Context targetContext = context;
        if (enableApplicationContext) {
            targetContext = targetContext.getApplicationContext();
        }
        if (!mDatabaseHelperMap.containsKey(targetContext)) {
            synchronized (mLock) {
                if (!mDatabaseHelperMap.containsKey(targetContext)) {
                    mDatabaseHelperMap
                            .put(context, new DatabaseHelper(context));
                }
            }
        }
        Log.w(TAG, String.format("目前DatabaseHelperMap size: %s",
                mDatabaseHelperMap.size()));
        return mDatabaseHelperMap.get(targetContext);
    }

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
            TableUtils.createTable(connectionSource, HttpRequestRecord.class);
        } catch (SQLException e) {
            Log.e(TAG, "Could not create new table for Thing", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,
                          ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, HttpRequestRecord.class,
                    true);
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "Could not upgrade the table for Thing", e);
        }
    }

    private Dao<HttpRequestRecord, Long> recordDao;

    public Dao<HttpRequestRecord, Long> getHttpRequestRecordDao()
            throws SQLException {
        if (recordDao == null) {
            recordDao = getDao(HttpRequestRecord.class);
        }
        return recordDao;
    }

    @Override
    public void close() {
        super.close();
        recordDao = null;
    }
}
