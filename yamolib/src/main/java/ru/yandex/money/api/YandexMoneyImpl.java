package ru.yandex.money.api;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import ru.yandex.money.api.enums.MoneyDirection;
import ru.yandex.money.api.enums.OperationHistoryType;
import ru.yandex.money.api.response.*;
import ru.yandex.money.api.response.util.Operation;
import ru.yandex.money.api.rights.IdentifierType;
import ru.yandex.money.api.rights.Permission;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>Класс для работы с API Яндекс.Деньги. Реализует интерфейс YandexMoney.</p>
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

public class YandexMoneyImpl implements YandexMoney {

    private static final String USER_AGENT = "yamolib";

    private final TokenRequester tokenRequester;
    private final ApiCommandsFacade apiCommandsFacade;

    /**
     * Создает экземпляр класса. Внутри создается httpClient
     * с SingleClientConnManager и таймаутом 60 секунд. Это для случая когда
     * объкту достаточно одного соединения.
     *
     * @param clientId идентификатор приложения в системе Яндекс.Деньги
     */
    public YandexMoneyImpl(String clientId) {
        this(clientId, createHttpClient(60100));
    }

    static HttpClient createHttpClient(int socketTimeout) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 4000);
        HttpConnectionParams.setSoTimeout(httpClient.getParams(), socketTimeout);
        return httpClient;
    }

    /**
     * Создает экземпляр класса. Внутри создается httpClient
     * с переданными в параметрах ConnectionManager и HttpParams. Это может
     * быть нужно для нескольких одновременных соединений.
     *
     * @param clientId идентификатор приложения в системе Яндекс.Деньги
     * @param client   настроенный httpClient с использованием нужной ключницы и таймаутов
     */
    public YandexMoneyImpl(final String clientId, HttpClient client) {
        this.tokenRequester = new TokenRequesterImpl(clientId, client);
        this.apiCommandsFacade = new ApiCommandsFacadeImpl(client);
    }

    public String getClientId() {
        return tokenRequester.getClientId();
    }

    public String authorizeUri(Collection<Permission> permissions, String redirectUri, Boolean mobileMode) {
        return tokenRequester.authorizeUri(permissions, redirectUri, mobileMode);
    }

    @Override
    public String authorizeUri(String scope, String redirectUri, Boolean mobileMode) {
        return tokenRequester.authorizeUri(scope, redirectUri, mobileMode);
    }

    public ReceiveOAuthTokenResponse receiveOAuthToken(String code,
                                                       String redirectUri) throws IOException, InsufficientScopeException {
        return tokenRequester.receiveOAuthToken(code, redirectUri);
    }

    public ReceiveOAuthTokenResponse receiveOAuthToken(String code,
                                                       String redirectUri, String clientSecret) throws IOException, InsufficientScopeException {
        return tokenRequester.receiveOAuthToken(code, redirectUri, clientSecret);
    }

    public void revokeOAuthToken(String accessToken) throws InvalidTokenException, IOException {
        apiCommandsFacade.revokeOAuthToken(accessToken);
    }

    public AccountInfoResponse accountInfo(String accessToken)
            throws IOException, InvalidTokenException, InsufficientScopeException {
        return apiCommandsFacade.accountInfo(accessToken);
    }

    public OperationHistoryResponse operationHistory(String accessToken)
            throws IOException, InvalidTokenException, InsufficientScopeException {
        return apiCommandsFacade.operationHistory(accessToken);
    }

    public OperationHistoryResponse operationHistory(String accessToken, Integer startRecord)
            throws IOException, InvalidTokenException, InsufficientScopeException {
        return operationHistory(accessToken, startRecord, null);
    }

    public OperationHistoryResponse operationHistory(String accessToken,
                                                     Integer startRecord, Integer records) throws IOException,
            InvalidTokenException, InsufficientScopeException {
        return operationHistory(accessToken, startRecord, records, null);
    }

    public OperationHistoryResponse operationHistory(String accessToken,
                                                     Integer startRecord, Integer records,
                                                     Set<OperationHistoryType> operationsType) throws IOException,
            InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.operationHistory(accessToken, startRecord, records, operationsType);
    }

    public OperationHistoryResponse operationHistory(String accessToken, Integer startRecord, Integer records,
                                                     Set<OperationHistoryType> operationsType, Boolean fetchDetails,
                                                     Date from, Date till, String label)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.operationHistory(accessToken, startRecord, records, operationsType, fetchDetails, from, till, label);
    }

    @Override
    public FundraisingStatsResponse fundraisingStats(String accessToken, String label) throws IOException, InvalidTokenException, InsufficientScopeException {
        return apiCommandsFacade.fundraisingStats(accessToken, label);
    }

    public OperationDetailResponse operationDetail(String accessToken,
                                                   String operationId) throws IOException, InvalidTokenException,
            InsufficientScopeException {

        return apiCommandsFacade.operationDetail(accessToken, operationId);
    }

    public RequestPaymentResponse requestPaymentP2PDue(String accessToken, String to, IdentifierType identifierType,
                                                       BigDecimal amountDue, String comment, String message, String label)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.requestPaymentP2PDue(accessToken, to, identifierType, amountDue, comment, message, label);
    }

    public RequestPaymentResponse requestPaymentP2P(String accessToken, String to, IdentifierType identifierType,
                                                    BigDecimal amount, String comment, String message, String label)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.requestPaymentP2P(accessToken, to, identifierType, amount, comment, message, label);
    }

    public RequestPaymentResponse requestPaymentP2P(String accessToken, String to, BigDecimal amount,
                                                    String comment, String message)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.requestPaymentP2P(accessToken, to, amount, comment, message);
    }

    public RequestPaymentResponse requestPaymentToPhone(String accessToken, String phone, BigDecimal amount)
            throws InsufficientScopeException, InvalidTokenException, IOException {
        return apiCommandsFacade.requestPaymentToPhone(accessToken, phone, amount);
    }

    public RequestPaymentResponse requestPaymentShop(String accessToken,
                                                     String patternId, Map<String, String> params) throws IOException,
            InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.requestPaymentShop(accessToken, patternId, params);
    }

    public ProcessPaymentResponse processPaymentByWallet(String accessToken,
                                                         String requestId) throws IOException, InsufficientScopeException,
            InvalidTokenException {
        return apiCommandsFacade.processPaymentByWallet(accessToken, requestId);
    }

    public ProcessPaymentResponse processPaymentByCard(String accessToken, String requestId, String csc)
            throws IOException, InsufficientScopeException, InvalidTokenException {

        return apiCommandsFacade.processPaymentByCard(accessToken, requestId, csc);
    }
    protected static class CollisionException extends Exception {
        private static final long serialVersionUID = -727297981409385426L;
    }

    public OperationIncome notifyIncome(String accessToken, Long lastOperation) {
        while (true) {
            try {
                return read(accessToken, lastOperation);
            } catch (CollisionException ignore) {
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    OperationIncome read(String token, Long lastOperation) throws Exception {
        TreeMap<Long, OperationDetailResponse> list = new TreeMap<Long, OperationDetailResponse>();
        Long maxOperation = 0L;

        Integer rowStart = new Integer(0);
        while (rowStart != null) {
            OperationHistoryResponse res = operationHistory(token, rowStart, 5);

            if (!res.isSuccess())
                throw new Exception(res.getError());

            rowStart = res.getNextRecord();

            for (Operation o : res.getOperations()) {
                Long operationId = Long.valueOf(o.getOperationId());

                maxOperation = Math.max(operationId, maxOperation);

                if (o.getDirection() == MoneyDirection.in) {
                    if (operationId.equals(lastOperation))
                        return new OperationIncome(list.values(), maxOperation);
                    OperationDetailResponse detail = operationDetail(token, o.getOperationId());
                    if (list.put(operationId, detail) != null)
                        throw new CollisionException();
                }
            }
        }
        if (lastOperation != null)
            throw new RuntimeException("No last operation found");
        return new OperationIncome(list.values(), maxOperation);
    }
}
