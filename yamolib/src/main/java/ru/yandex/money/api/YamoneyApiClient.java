package ru.yandex.money.api;

import com.google.common.collect.Maps;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.transformEntries;

/**
 * <p/>
 * <p/>
 * Created: 26.05.2014 23:48
 * <p/>
 *
 * @author OneHalf
 */
public abstract class YamoneyApiClient<Req, Resp> {

    private static final Log LOGGER = LogFactory.getLog(YamoneyApiClient.class);

    /**
     * Кодировка для url encoding/decoding
     */
    protected static final String CHARSET = "UTF-8";
    protected static final String USER_AGENT = "yamolib";

    public static final Maps.EntryTransformer<String, String, String> CARD_CODE_MASKER = new Maps.EntryTransformer<String, String, String>() {
        @Override
        public String transformEntry(String key, String value) {
            if (key.equalsIgnoreCase("csc")) {
                return "***";
            }
            return value;
        }
    };

    protected void logParameters(URI uri, Map<String, String> params) {
        if (!LOGGER.isInfoEnabled()) {
            return;
        }
        // Пишем в логи все параметры, кроме кода карточки
        LOGGER.info("request url '" + uri +"' with parameters: " + transformEntries(params, CARD_CODE_MASKER));
    }

    <T> T executeForJsonObjectCommon(URI url, Map<String, String> params, Class<T> classOfT) throws IOException {

        Resp response = null;
        try {
            response = execPostRequest(url, params);
            checkCommonResponse(response);

            return parseJson(classOfT, getInputStreamFromResponse(response));
        } finally {
            closeResponse(response);
        }
    }

    protected abstract InputStream getInputStreamFromResponse(Resp response) throws IOException;

    protected void logWWWAuthenticate(Resp response) {
        int iCode = getStatusCodeFromResponse(response);
        if (iCode != HttpStatus.SC_OK) {
            String wwwAuthenticate = getHeaderValue(response);
            LOGGER.info("http status: " + iCode + (wwwAuthenticate == null ? "" : ", " + wwwAuthenticate));
        }
    }

    protected abstract String getHeaderValue(Resp response);

    protected Resp execPostRequest(URI httpPost, Map<String, String> params) throws IOException {
        return execPostRequest(httpPost, null, params);
    }

    protected abstract Resp execPostRequest(URI httpPost, String accessToken, Map<String, String> params) throws IOException;

    <T> T parseJson(Class<T> classOfT, InputStream inputStream) throws IOException {

        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(
                    FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            T result = gson.fromJson(new InputStreamReader(inputStream, CHARSET), classOfT);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("result: " + result);
            }
            return result;
        } catch (JsonParseException e) {
            throw new IllegalStateException("response decoding failed", e);
        }
    }

    void checkApiCommandResponse(Resp httpResp) throws InvalidTokenException,
            InsufficientScopeException, InternalServerErrorException {

        if (getStatusCodeFromResponse(httpResp) == HttpStatus.SC_UNAUTHORIZED) {
            throw new InvalidTokenException("invalid token");
        }
        if (getStatusCodeFromResponse(httpResp) == HttpStatus.SC_FORBIDDEN) {
            throw new InsufficientScopeException("insufficient scope");
        }
        checkCommonResponse(httpResp);
    }

    void checkCommonResponse(Resp httpResp) throws InternalServerErrorException {

        switch (getStatusCodeFromResponse(httpResp)) {
            case HttpStatus.SC_BAD_REQUEST:
                throw new ProtocolRequestException("invalid request");
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                throw new InternalServerErrorException("internal yandex.money server error");
        }

        checkResponseNonEmpty(httpResp);
    }

    protected abstract void checkResponseNonEmpty(Resp httpResp);

    <T> T executeForJsonObjectFunc(CommandUrlHolder urlHolder, String commandName, Map<String, String> params,
                                   String accessToken, Class<T> classOfT)
            throws InsufficientScopeException, IOException, InvalidTokenException {

        Resp response = null;

        try {

            response = execPostRequest(urlHolder.getUrlForCommand(commandName),
                    accessToken, params(params, urlHolder));

            checkApiCommandResponse(response);

            return parseJson(classOfT, getInputStreamFromResponse(response));

        } finally {
            closeResponse(response);
        }
    }

    private Map<String, String> params(Map<String, String> params, CommandUrlHolder urlHolder) {
        HashMap<String, String> result = Maps.newHashMap(params);
        result.putAll(urlHolder.getAdditionalParams());
        return result;
    }

    protected abstract void closeResponse(Resp response) throws IOException;

    protected abstract int getStatusCodeFromResponse(Resp httpResp);
}
