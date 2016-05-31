package retrofit2;

import com.lin1987www.jackson.JacksonHelper;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpHelper;
import okhttp3.RequestBody;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by Administrator on 2016/5/23.
 */
public class RetrofitHelper {
    private static Retrofit.Builder mRetrofitBuilder;

    public static Retrofit.Builder getRetrofitBuilder() {
        if (mRetrofitBuilder == null) {
            synchronized (RetrofitHelper.class) {
                if (mRetrofitBuilder == null) {
                    mRetrofitBuilder = new Retrofit.Builder()
                            .addConverterFactory(JacksonConverterFactory.create(JacksonHelper.getObjectMapper()))
                            .client(OkHttpHelper.getOkHttpClient());
                }
            }
        }
        return mRetrofitBuilder;
    }

    public static MultipartBody.Part createPart(String name, String mediaTypeString, File file) {
        return createPart(name, MediaType.parse(mediaTypeString), file);
    }

    public static MultipartBody.Part createPart(String name, MediaType mediaType, File file) {
        RequestBody fileReqBody = RequestBody.create(mediaType, file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(name, file.getName(), fileReqBody);
        return filePart;
    }
}
