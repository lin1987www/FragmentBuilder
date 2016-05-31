package com.lin1987www.jackson;

import android.support.v4.app.ContextHelper;
import android.support.v4.app.ExecutorSet;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@DatabaseTable(tableName = "httprequestrecords")
public class HttpRequestRecord {
    public HttpRequestRecord() {
        this(null, null, null, null, null, null, null, null);
    }

    public HttpRequestRecord(String method, String url, String params,
                             String body, String error, Long sequence, String request_headers,
                             String response_headers) {
        this.method = method;
        this.url = url;
        this.params = params;
        this.body = body;
        this.error = error;
        Date date = new Date();
        this.time = date.getTime();
        this.time_string = dateFormat.format(date);
        this.sequence = sequence;
        this.request_headers = request_headers;
        this.response_headers = response_headers;
    }

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField()
    public String url;

    @DatabaseField()
    public String params;

    @DatabaseField()
    public String body;

    @DatabaseField()
    public String error;

    @DatabaseField()
    public Long time;

    @DatabaseField()
    public String time_string;

    @DatabaseField()
    public Long sequence;

    @DatabaseField()
    public String method;

    @DatabaseField()
    public String request_headers;

    @DatabaseField()
    public String response_headers;

    private static TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final SimpleDateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'",
                Locale.US);
        dateFormat.setTimeZone(GMT);
    }

    public static void success(String method, String url, String params,
                               String body, Long sequence, String request_headers,
                               String response_headers) {
        record(method, url, params, body, null, sequence, request_headers,
                response_headers);
    }

    public static void error(String method, String url, String params,
                             String error, Long sequence, String request_headers,
                             String response_headers) {
        record(method, url, params, null, error, sequence, request_headers,
                response_headers);
    }

    public static void error(String method, String url, String params,
                             String body, String error, Long sequence, String request_headers,
                             String response_headers) {
        record(method, url, params, body, error, sequence, request_headers,
                response_headers);
    }

    private static void record(String method, String url, String params,
                               String body, String error, Long sequence, String request_headers,
                               String response_headers) {
        Runnable runnable = new RecordRunnable(new HttpRequestRecord(method,
                url, params, body, error, sequence, request_headers,
                response_headers));
        ExecutorSet.nonBlockExecutor.submit(runnable);
        //ExecutorServiceHelper.getScheduledThreadPoolExecutor().submit(runnable);
    }

    private static class RecordRunnable implements Runnable {
        private HttpRequestRecord mRecord;

        public RecordRunnable(HttpRequestRecord record) {
            mRecord = record;
        }

        @Override
        public void run() {
            try {
                Dao<HttpRequestRecord, Long> dao = DatabaseHelper
                        .getDatabaseHelper(ContextHelper.getApplication())
                        .getHttpRequestRecordDao();
                dao.create(mRecord);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
