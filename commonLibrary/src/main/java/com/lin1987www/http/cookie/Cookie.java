package com.lin1987www.http.cookie;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import java6.net.HttpCookie;

@DatabaseTable(tableName = "cookies")
public class Cookie {
    public Cookie() {
    }

    @DatabaseField(uniqueIndexName = "_idx_domain_path_name")
    public String name;

    @DatabaseField()
    public String value;

    @DatabaseField()
    public String comment;

    @DatabaseField(uniqueIndexName = "_idx_domain_path_name")
    private String domain;

    @DatabaseField()
    public Long maxage;

    @DatabaseField(uniqueIndexName = "_idx_domain_path_name")
    public String path;

    @DatabaseField()
    public boolean isSecure;

    @DatabaseField()
    public int version;

    // RFC 2109 - http://www.ietf.org/rfc/rfc2109.txt

    @DatabaseField()
    public String commentURL;

    @DatabaseField()
    public boolean isDiscard;

    @DatabaseField()
    public String portList;

    @DatabaseField()
    public boolean isHttpOnly;

    // RFC 2965 - http://www.ietf.org/rfc/rfc2965.txt

    @DatabaseField()
    public Long createdTime;

    @DatabaseField()
    public String creationTimeString;

    @DatabaseField()
    public Long expiryTime;

    @DatabaseField()
    public String expiryTimeString;

    /**
     * 這欄位是cookie有設定expires時使用的
     */
    @DatabaseField()
    public String expires;

    @DatabaseField()
    public String debugURL;

    @DatabaseField()
    public String debugHeader;

    @DatabaseField(id = true, useGetSet = true)
    private String id;

    public String getId() {
        return String.format("%s %s %s", domain, path, name);
    }

    public void setId(String id) {
        this.id = id;
    }

    public Cookie(HttpCookie cookie) {
        this.name = cookie.getName();
        this.value = cookie.getValue();
        this.comment = cookie.getComment();
        this.domain = cookie.getDomain();
        this.maxage = cookie.getMaxAge();
        this.path = cookie.getPath();
        this.isSecure = cookie.getSecure();
        this.version = cookie.getVersion();
        this.commentURL = cookie.getCommentURL();
        this.isDiscard = cookie.getDiscard();
        this.portList = cookie.getPortlist();
        this.isHttpOnly = cookie.isHttpOnly();
        this.createdTime = cookie.getCreatedTime();
        this.expires = cookie.getExpires();
        this.debugHeader = cookie.getHeader();
        // debugURL 由CookieHandlerFactory提供getCookieHandler中的add來處理
        this.creationTimeString = dateFormat.format(new Date(cookie
                .getCreatedTime()));
        this.expiryTime = cookie.getCreatedTime() + cookie.getMaxAge() * 1000;
        this.expiryTimeString = dateFormat.format(expiryTime);
        // 如果有提供 Expires 時，則準確的轉換期時間
        if (!TextUtils.isEmpty(cookie.getExpires())) {
            Date expiresDate = parseDateString(cookie.getExpires());
            if (expiresDate != null) {
                this.expiryTime = expiresDate.getTime();
                this.expiryTimeString = dateFormat.format(expiresDate);
            }
        }
    }

    public HttpCookie getHttpCookie() {
        HttpCookie cookie = new HttpCookie(this.name, this.value);
        cookie.setComment(this.comment);
        cookie.setDomain(this.domain);
        // hasExpired 會自己去減 當初建立的時間，只有要當建立的 whenCreated 就可以正常運作
        cookie.setMaxAge(this.maxage);
        cookie.setPath(this.path);
        cookie.setSecure(this.isSecure);
        cookie.setVersion(this.version);
        cookie.setCommentURL(this.commentURL);
        cookie.setDiscard(this.isDiscard);
        cookie.setPortlist(this.portList);
        cookie.setHttpOnly(this.isHttpOnly);
        cookie.setCreatedTime(this.createdTime);
        cookie.setExpires(this.expires);
        return cookie;
    }

    private static TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final SimpleDateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'",
                Locale.US);
        dateFormat.setTimeZone(GMT);
    }

    //
    // date formats used by Netscape's cookie draft
    // as well as formats seen on various sites
    //
    private final static String[] COOKIE_DATE_FORMATS = {
            "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'",
            "EEE',' dd MMM yyyy HH:mm:ss 'GMT'",
            "EEE MMM dd yyyy HH:mm:ss 'GMT'Z",
            "EEE',' dd-MMM-yy HH:mm:ss 'GMT'",
            "EEE',' dd MMM yy HH:mm:ss 'GMT'", "EEE MMM dd yy HH:mm:ss 'GMT'Z"};

    private Date parseDateString(String dateString) {
        Date result = null;
        Calendar cal = new GregorianCalendar(GMT);
        for (int i = 0; i < COOKIE_DATE_FORMATS.length; i++) {
            SimpleDateFormat df = new SimpleDateFormat(COOKIE_DATE_FORMATS[i],
                    Locale.US);
            cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
            df.setTimeZone(GMT);
            df.setLenient(false);
            df.set2DigitYearStart(cal.getTime());
            try {
                cal.setTime(df.parse(dateString));
                if (!COOKIE_DATE_FORMATS[i].contains("yyyy")) {
                    // 2-digit years following the standard set
                    // out it rfc 6265
                    int year = cal.get(Calendar.YEAR);
                    year %= 100;
                    if (year < 70) {
                        year += 2000;
                    } else {
                        year += 1900;
                    }
                    cal.set(Calendar.YEAR, year);
                }
                result = cal.getTime();
            } catch (Exception e) {
                // Ignore, try the next date format
            }
        }
        return result;
    }

}
