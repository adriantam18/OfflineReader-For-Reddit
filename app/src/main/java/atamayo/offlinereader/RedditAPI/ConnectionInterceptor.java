package atamayo.offlinereader.RedditAPI;

import android.content.Context;

import java.io.IOException;

import atamayo.offlinereader.Utils.InternetConnection;
import okhttp3.Interceptor;
import okhttp3.Response;

public class ConnectionInterceptor implements Interceptor {
    private final Context mContext;

    public ConnectionInterceptor(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (!InternetConnection.isConnectedToNetwork(mContext)) {
            throw new NoConnectionException();
        }

        return chain.proceed(chain.request());
    }
}
