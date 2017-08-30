package okhttp3;

import com.lin1987www.common.Utility;

import java.io.IOException;

import okio.BufferedSource;

public class ConnectionResetByPeerInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        // Ref:
        // https://medium.com/@kanishksinha/easily-retrying-network-requests-on-android-with-retrofit2-and-okhttp3-interceptor-ab289e35e342
        // https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/internal/http/RetryAndFollowUpInterceptor.java
        Request request = chain.request();
        Response response = null;
        Throwable ex = null;
        while (true) {
            try {
                response = chain.proceed(request);
                if (response != null) {
                    /*
                    ResponseBody responseBody = response.body();
                    BufferedSource source = responseBody.source();
                    source.request(Long.MAX_VALUE); // Buffer the entire body.
                    */
                }
                ex = null;
                break;
            } catch (Throwable throwable) {
                ex = Utility.getNestedError(throwable);
                if (Utility.ECONNRESET.equals(ex.getMessage()) ||
                        Utility.UNEXPECTED_END_OF_STREAM.equals(ex.getMessage())
                        ) {
                    request = request.newBuilder().build();
                    continue;
                } else {
                    break;
                }
            }
        }
        if (ex != null) {
            throw new IOException(ex);
        }
        return response;
    }
}