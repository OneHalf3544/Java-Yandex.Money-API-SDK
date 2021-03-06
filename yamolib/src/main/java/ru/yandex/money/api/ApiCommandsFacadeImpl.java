package ru.yandex.money.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import ru.yandex.money.api.enums.MoneySource;
import ru.yandex.money.api.enums.OperationHistoryType;
import ru.yandex.money.api.response.*;
import ru.yandex.money.api.rights.IdentifierType;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * <p>Класс для работы с командами API Яндекс.Деньги. </p>
 * <p>За бесопанстость работы с удаленным сервером овечает Apache HttpClient.
 * Он по умолчанию работает в режиме BrowserCompatHostnameVerifier,
 * этот параметр указывает клиенту, что нужно проверять цепочку сертификатов
 * сервера Янедкс.Денег, с которым мы работаем. При этом дополнительно указывать
 * сертификат Яндекс.Денег не имеет смысла, так как в java встроено доверие
 * ресурсам сертифицированным официальным CA, таким как GTE CyberTrust
 * Solutions, Inc.</p>
 *
 * @author dvmelnikov
 */

public class ApiCommandsFacadeImpl implements ApiCommandsFacade {

    public static final String ACCOUNT_INFO_COMMAND_NAME = "account-info";
    public static final String OPERATION_HISTORY_COMMAND_NAME = "operation-history";
    public static final String OPERATION_DETAILS_COMMAND_NAME = "operation-details";
    public static final String REQUEST_PAYMENT_COMMAND_NAME = "request-payment";
    public static final String PROCESS_PAYMENT_COMMAND_NAME = "process-payment";
    public static final String REVOKE_COMMAND_NAME = "revoke";
    public static final String FUNDRAISING_STATS_COMMAND_NAME = "fundraising-stats";

    private static final ThreadLocal<SimpleDateFormat> RFC_3339 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        }
    };

    private final CommandUrlHolder uri;
    private final YamoneyApiClient<?, ?> yamoneyApiClient;

    /**
     * Создает экземпляр класса.
     *
     * @param client настроенный HttpClient для взаимодействия с сервером Яндекс.Деньги.
     *               Для request-payment и process-payment может понядобиться httpClient
     *               c таймаутом до 60 секунд.
     */
    public ApiCommandsFacadeImpl(HttpClient client) {
        this(client, URI_YM_API);
    }

    /**
     * Создает экземпляр класса.
     *
     * @param client настроенный HttpClient для взаимодействия с сервером Яндекс.Деньги.
     *               Для request-payment и process-payment может понядобиться httpClient
     *               c таймаутом до 60 секунд.
     */
    public ApiCommandsFacadeImpl(HttpClient client, CommandUrlHolder urlHolder) {
        this.yamoneyApiClient = new YamoneyApiHttpClient(client);
        this.uri = urlHolder;
    }

    /**
     * Создает экземпляр класса. Внутри создается httpClient
     * с переданными в параметрах ConnectionManager и HttpParams. Это может
     * быть нужно для нескольких одновременных соединений.
     *
     * @param client             настроенный HttpClient для взаимодействия с сервером Яндекс.Деньги.
     *                           Для request-payment и process-payment может понядобиться httpClient
     *                           c таймаутом до 60 секунд.
     * @param yandexMoneyTestUrl адрес тестововго хоста. Используйте для отладки,
     *                           если у вас есть "эмулятор" Яндекс.Денег
     */
    public ApiCommandsFacadeImpl(HttpClient client, String yandexMoneyTestUrl) {
        this(client, new CommandUrlHolder.ConstantUrlHolder(yandexMoneyTestUrl));
    }

    /**
     * Запрос данных о счете. Баланс, статус идентифицированнности, является ли профессиональным счетом.
     *
     * @param accessToken string токен авторизации пользователя
     * @return Данные счета
     * @throws IOException                При сетевых ошибках
     * @throws InvalidTokenException      Если токен некорректен, или отозван
     * @throws InsufficientScopeException Если для данного токена нет прав на использование account-info
     */
    @Override
    public AccountInfoResponse accountInfo(String accessToken)
            throws IOException, InvalidTokenException, InsufficientScopeException {
        return yamoneyApiClient.executeForJsonObjectFunc(uri, ACCOUNT_INFO_COMMAND_NAME, Collections.<String, String>emptyMap(), accessToken, AccountInfoResponse.class);
    }

    @Override
    public OperationHistoryResponse operationHistory(String accessToken)
            throws IOException, InvalidTokenException, InsufficientScopeException {
        return operationHistory(accessToken, null, null);
    }

    @Override
    public OperationHistoryResponse operationHistory(String accessToken, Integer startRecord)
            throws IOException, InvalidTokenException, InsufficientScopeException {
        return operationHistory(accessToken, startRecord, null);
    }

    @Override
    public OperationHistoryResponse operationHistory(String accessToken,
                                                     Integer startRecord, Integer records) throws IOException,
            InvalidTokenException, InsufficientScopeException {
        return operationHistory(accessToken, startRecord, records, null);
    }

    @Override
    public OperationHistoryResponse operationHistory(String accessToken, Integer startRecord, Integer records,
                                                     OperationHistoryType operationsType) throws IOException,
            InvalidTokenException, InsufficientScopeException {
        return operationHistory(accessToken, startRecord, records, operationsType, null, null, null, null);
    }

    @Override
    public OperationHistoryResponse operationHistory(String accessToken,
                                                     Integer startRecord, Integer records,
                                                     OperationHistoryType operationsType, Boolean fetchDetails,
                                                     Date from, Date till, String label) throws IOException,
            InvalidTokenException, InsufficientScopeException {

        Map<String, String> params = Maps.newHashMap();

        addParamIfNotNull("start_record", startRecord, params);
        addParamIfNotNull("records", records, params);
        addParamIfNotNull("type", operationsType.getCode(), params);
        addParamIfNotNull("details", fetchDetails, params);
        addParamIfNotNull("from", from, params);
        addParamIfNotNull("till", till, params);
        addParamIfNotNull("label", label, params);

        return yamoneyApiClient.executeForJsonObjectFunc(
                uri, OPERATION_HISTORY_COMMAND_NAME, params, accessToken, OperationHistoryResponse.class);
    }

    @Override
    public FundraisingStatsResponse fundraisingStats(String accessToken, String label)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        Map<String, String> params = ImmutableMap.of(
                "label", label
        );
        return yamoneyApiClient.executeForJsonObjectFunc(
                uri, FUNDRAISING_STATS_COMMAND_NAME, params, accessToken, FundraisingStatsResponse.class);
    }

    @Override
    public OperationDetailResponse operationDetail(String accessToken,
                                                   String operationId) throws IOException, InvalidTokenException,
            InsufficientScopeException {

        Map<String, String> params = ImmutableMap.of(
                "operation_id", operationId
        );
        return yamoneyApiClient.executeForJsonObjectFunc(
                uri, OPERATION_DETAILS_COMMAND_NAME, params, accessToken, OperationDetailResponse.class);
    }

    @Override
    public RequestPaymentResponse requestPaymentP2P(String accessToken, String to,
                                                    BigDecimal amount, String comment, String message)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        Map<String, String> params = Maps.newHashMap();
        params.put("amount", String.valueOf(amount));
        return requestPaymentP2P(accessToken, to, comment, message, null, params);
    }

    @Override
    public RequestPaymentResponse requestPaymentP2P(String accessToken, String to, IdentifierType identifierType,
                                                    BigDecimal amount, String comment, String message, String label)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        Map<String, String> params = Maps.newHashMap();
        params.put("amount", String.valueOf(amount));
        addParamIfNotNull("identifier_type", identifierType, params);
        return requestPaymentP2P(accessToken, to, comment, message, label, params);
    }

    @Override
    public RequestPaymentResponse requestPaymentP2PDue(String accessToken, String to, IdentifierType identifierType,
                                                       BigDecimal amountDue, String comment, String message,
                                                       String label)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        Map<String, String> params = Maps.newHashMap();
        params.put("amount_due", String.valueOf(amountDue));
        addParamIfNotNull("identifier_type", identifierType, params);
        return requestPaymentP2P(accessToken, to, comment, message, label, params);
    }

    private RequestPaymentResponse requestPaymentP2P(String accessToken, String to,
                                                     String comment, String message, String label,
                                                     Map<String, String> params)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        params.put("pattern_id", "p2p");
        params.put("to", to);
        addParamIfNotNull("comment", comment, params);
        addParamIfNotNull("message", message, params);
        addParamIfNotNull("label", label, params);

        return yamoneyApiClient.executeForJsonObjectFunc(
                uri, REQUEST_PAYMENT_COMMAND_NAME, params, accessToken, RequestPaymentResponse.class);
    }

    @Override
    public RequestPaymentResponse requestPaymentToPhone(String accessToken, String phone, BigDecimal amount)
            throws InsufficientScopeException, InvalidTokenException, IOException {

        Map<String, String> params = ImmutableMap.of(
                "pattern_id", "phone-topup",
                "phone-number", phone,
                "amount", String.valueOf(amount));

        return yamoneyApiClient.executeForJsonObjectFunc(
                uri, REQUEST_PAYMENT_COMMAND_NAME, params, accessToken, RequestPaymentResponse.class);
    }

    @Override
    public RequestPaymentResponse requestPaymentShop(String accessToken,
                                                     String patternId, Map<String, String> params) throws IOException,
            InvalidTokenException, InsufficientScopeException {

        return requestPaymentShop(accessToken, patternId, params, false);
    }

    @Override
    public RequestPaymentResponse requestPaymentShop(String accessToken, String patternId,
                                                     Map<String, String> params, boolean showContractDetails) throws IOException,
            InvalidTokenException, InsufficientScopeException {

        Map<String, String> pars = Maps.newHashMap();
        pars.put("pattern_id", patternId);
        for (String name : params.keySet()) {
            pars.put(name, params.get(name));
        }
        if (showContractDetails) {
            pars.put("show_contract_details", "true");
        }

        return yamoneyApiClient.executeForJsonObjectFunc(
                uri, REQUEST_PAYMENT_COMMAND_NAME, pars, accessToken, RequestPaymentResponse.class);
    }

    @Override
    public ProcessPaymentResponse processPaymentByWallet(String accessToken, String requestId)
            throws IOException, InsufficientScopeException, InvalidTokenException {

        return processPayment(accessToken, requestId, MoneySource.wallet, null);
    }

    @Override
    public ProcessPaymentResponse processPaymentByCard(String accessToken, String requestId, String csc)
            throws IOException, InsufficientScopeException, InvalidTokenException {

        return processPayment(accessToken, requestId, MoneySource.card, csc);
    }

    private ProcessPaymentResponse processPayment(String accessToken,
                                                  String requestId, MoneySource moneySource, String csc)
            throws IOException, InsufficientScopeException,
            InvalidTokenException {

        Map<String, String> params = Maps.newHashMap();
        params.put("request_id", requestId);
        params.put("money_source", moneySource.toString());
        if (csc != null && (moneySource.equals(MoneySource.card))) {
            params.put("csc", csc);
        }
        return yamoneyApiClient.executeForJsonObjectFunc(
                uri, PROCESS_PAYMENT_COMMAND_NAME, params, accessToken, ProcessPaymentResponse.class);
    }

    @Override
    public void revokeOAuthToken(String accessToken) throws InvalidTokenException, IOException {
        revokeToken(yamoneyApiClient, accessToken);
    }

    private <Resp> void revokeToken(YamoneyApiClient<?, Resp> yamoneyApiClient, String accessToken)
            throws IOException, InvalidTokenException {
        Resp response = null;
        try {
            response = yamoneyApiClient.execPostRequest(uri.getUrlForCommand(REVOKE_COMMAND_NAME),
                    accessToken, Collections.<String, String>emptyMap());
            switch (yamoneyApiClient.getStatusCodeFromResponse(response)) {
                case HttpStatus.SC_UNAUTHORIZED:
                    throw new InvalidTokenException("invalid token");
                case HttpStatus.SC_BAD_REQUEST:
                    throw new ProtocolRequestException("invalid request");
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    throw new InternalServerErrorException("internal yandex.money server error");
            }
        } finally {
            yamoneyApiClient.closeResponse(response);
        }
    }

    private void addParamIfNotNull(String paramName, Object value, Map<String, String> params) {
        if (value != null) {
            params.put(paramName, String.valueOf(value));
        }
    }

    private void addParamIfNotNull(String paramName, Date date, Map<String, String> params) {
        if (date == null) {
            return;
        }
        params.put(paramName, formatDate(date));
    }

    String formatDate(Date date) {
        return RFC_3339.get().format(date).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
    }
}
