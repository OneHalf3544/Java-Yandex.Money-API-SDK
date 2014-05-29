package ru.yandex.money.api;

import com.google.common.base.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

public class YamoneyApiHttpClient extends YamoneyApiClient<HttpPost, HttpResponse> {

    private static final Log LOGGER = LogFactory.getLog(YamoneyApiHttpClient.class);
    private static final Function<Map.Entry<String,String>,NameValuePair> TO_NAME_VALUE_PAIR = new Function<Map.Entry<String, String>, NameValuePair>() {
        @Override
        public NameValuePair apply(Map.Entry<String, String> input) {
            return new BasicNameValuePair(input.getKey(), input.getValue());
        }
    };

    private final HttpClient httpClient;

    public YamoneyApiHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public static HttpClient createHttpClient(int socketTimeout) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 4000);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), socketTimeout);
        return httpClient;
    }

    /**
     * Ensures that the entity content is fully consumed and the content stream, if exists,
     * is closed.
     *
     * @param entity
     * @throws IOException if an error occurs reading the input stream
     *
     * @since 4.1
     */
    private void consumeEntity(final HttpEntity entity) throws IOException {
        if (entity == null) {
            return;
        }
        if (entity.isStreaming()) {
            InputStream inputStream = entity.getContent();
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @Override
    protected int getStatusCodeFromResponse(HttpResponse httpResp) {
        return httpResp.getStatusLine().getStatusCode();
    }

    @Override
    protected HttpResponse execPostRequest(URI uri, Map<String, String> params) throws IOException {
        return execPostRequest(uri, null, params);
    }

    @Override
    protected void checkResponseNonEmpty(HttpResponse httpResp) {
        if (httpResp.getEntity() == null) {
            throw new IllegalStateException("response http entity is empty");
        }
    }

    @Override
    protected HttpResponse execPostRequest(URI uri, String accessToken, Map<String, String> params) throws IOException {

        logParameters(uri, params);
        HttpPost httpPost = new HttpPost(uri);
        if (accessToken != null) {
            httpPost.addHeader("Authorization", "Bearer " + accessToken);
        }
        httpPost.setEntity(new UrlEncodedFormEntity(newArrayList(transform(params.entrySet(), TO_NAME_VALUE_PAIR)), CHARSET));

        try {
            HttpResponse response = httpClient.execute(httpPost);
            logWWWAuthenticate(response);
            return response;

        } catch (IOException e) {
            httpPost.abort();
            throw e;
        }
    }

    @Override
    protected void closeResponse(HttpResponse response) throws IOException {
        if (response != null) {
            consumeEntity(response.getEntity());
        }
    }

    @Override
    protected String getHeaderValue(HttpResponse response) {
        return response.getFirstHeader("WWW-Authenticate").getValue();
    }

    @Override
    protected InputStream getInputStreamFromResponse(HttpResponse response) throws IOException {
        return response.getEntity().getContent();
    }
}
