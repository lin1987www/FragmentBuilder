package okhttp3;

import java.io.IOException;

public class ConnectionCloseInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        response = chain.proceed(request);
        if (response != null) {
            response.header("Connection", "close");
        }
        return response;
    }
}