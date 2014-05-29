package ru.yandex.money.api;

import com.squareup.okhttp.*;
import okio.BufferedSink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class YamoneyOkApiClient extends YamoneyApiClient<Request.Builder, Response> {

    private static final Log LOGGER = LogFactory.getLog(YamoneyOkApiClient.class);

    private final OkHttpClient httpClient;

    public YamoneyOkApiClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static OkHttpClient createOkHttpClient(int socketTimeout) {
        return new OkHttpClient()
                .setConnectTimeout(4, SECONDS)
                .setReadTimeout(socketTimeout, MILLISECONDS);
    }

    @Override
    protected Response execPostRequest(URI httpPost, String accessToken, Map<String, String> params) throws IOException {
        logParameters(httpPost, params);

        Request.Builder request = new Request.Builder()
                .url(httpPost.toURL())
                .post(new PostRequestBody(params));


        if (accessToken != null) {
            request.addHeader("Authorization", "Bearer " + accessToken);
        }

        Response response = httpClient.newCall(request.build()).execute();
        logWWWAuthenticate(response);
        return response;
    }

    @Override
    protected int getStatusCodeFromResponse(Response httpResp) {
        return httpResp.code();
    }

    @Override
    protected Response execPostRequest(URI uri, Map<String, String> params) throws IOException {
        logParameters(uri, params);
        Request.Builder builder = new Request.Builder()
                .url(uri.toURL())
                .post(new PostRequestBody(params));

        Response response = httpClient.newCall(builder.build()).execute();
        logWWWAuthenticate(response);
        return response;
    }

    @Override
    protected void checkResponseNonEmpty(Response httpResp) {
        if (httpResp.body().contentLength() == 0) {
            throw new IllegalStateException("response http entity is empty");
        }
    }

    @Override
    protected void closeResponse(Response response) {
        if (response != null) {
            try {
                response.body().close();
            } catch (IOException ignore) {
            }
        }
    }

    @Override
    protected String getHeaderValue(Response response) {
        return response.header("WWW-Authenticate");
    }

    @Override
    protected InputStream getInputStreamFromResponse(Response response) throws IOException {
        return response.body().byteStream();
    }

    private static class PostRequestBody extends RequestBody {

        private static final MediaType MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");

        private final Map<String, String> params;

        public PostRequestBody(Map<String, String> params) {
            this.params = params;
        }

        @Override
        public MediaType contentType() {
            return MEDIA_TYPE;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            Map.Entry<String, String> next = iterator.next();
            sink.writeString(next.getKey() + "=" + next.getValue(), Charset.forName(CHARSET));

            for (; iterator.hasNext(); next = iterator.next()) {
                sink.writeString(next.getKey() + "=" + next.getValue(), Charset.forName(CHARSET));
            }
        }
    }
}
