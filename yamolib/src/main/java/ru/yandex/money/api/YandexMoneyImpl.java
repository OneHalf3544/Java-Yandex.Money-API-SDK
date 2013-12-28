package ru.yandex.money.api;

import org.apache.http.client.HttpClient;
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
@Deprecated
public class YandexMoneyImpl implements YandexMoney {

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
        this(clientId, YamoneyApiClient.createHttpClient(60100));
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

    @Override
    public String getClientId() {
        return tokenRequester.getClientId();
    }

    @Override
    public String authorizeUri(Collection<Permission> permissions, String redirectUri, Boolean mobileMode) {
        return tokenRequester.authorizeUri(permissions, redirectUri, mobileMode);
    }

    @Override
    public String authorizeUri(String scope, String redirectUri, Boolean mobileMode) {
        return tokenRequester.authorizeUri(scope, redirectUri, mobileMode);
    }

    @Override
    public ReceiveOAuthTokenResponse receiveOAuthToken(String code,
                                                       String redirectUri) throws IOException, InsufficientScopeException {
        return tokenRequester.receiveOAuthToken(code, redirectUri);
    }

    @Override
    public ReceiveOAuthTokenResponse receiveOAuthToken(String code,
                                                       String redirectUri, String clientSecret) throws IOException, InsufficientScopeException {
        return tokenRequester.receiveOAuthToken(code, redirectUri, clientSecret);
    }

    @Override
    public void revokeOAuthToken(String accessToken) throws InvalidTokenException, IOException {
        apiCommandsFacade.revokeOAuthToken(accessToken);
    }

    @Override
    public AccountInfoResponse accountInfo(String accessToken)
            throws IOException, InvalidTokenException, InsufficientScopeException {
        return apiCommandsFacade.accountInfo(accessToken);
    }

    @Override
    public OperationHistoryResponse operationHistory(String accessToken)
            throws IOException, InvalidTokenException, InsufficientScopeException {
        return apiCommandsFacade.operationHistory(accessToken);
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
    public OperationHistoryResponse operationHistory(String accessToken,
                                                     Integer startRecord, Integer records,
                                                     Set<OperationHistoryType> operationsType) throws IOException,
            InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.operationHistory(accessToken, startRecord, records, operationsType);
    }

    @Override
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

    @Override
    public OperationDetailResponse operationDetail(String accessToken,
                                                   String operationId) throws IOException, InvalidTokenException,
            InsufficientScopeException {

        return apiCommandsFacade.operationDetail(accessToken, operationId);
    }

    @Override
    public RequestPaymentResponse requestPaymentP2PDue(String accessToken, String to, IdentifierType identifierType,
                                                       BigDecimal amountDue, String comment, String message, String label)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.requestPaymentP2PDue(accessToken, to, identifierType, amountDue, comment, message, label);
    }

    @Override
    public RequestPaymentResponse requestPaymentP2P(String accessToken, String to, IdentifierType identifierType,
                                                    BigDecimal amount, String comment, String message, String label)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.requestPaymentP2P(accessToken, to, identifierType, amount, comment, message, label);
    }

    @Override
    public RequestPaymentResponse requestPaymentP2P(String accessToken, String to, BigDecimal amount,
                                                    String comment, String message)
            throws IOException, InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.requestPaymentP2P(accessToken, to, amount, comment, message);
    }

    @Override
    public RequestPaymentResponse requestPaymentToPhone(String accessToken, String phone, BigDecimal amount)
            throws InsufficientScopeException, InvalidTokenException, IOException {
        return apiCommandsFacade.requestPaymentToPhone(accessToken, phone, amount);
    }

    @Override
    public RequestPaymentResponse requestPaymentShop(String accessToken,
                                                     String patternId, Map<String, String> params) throws IOException,
            InvalidTokenException, InsufficientScopeException {

        return apiCommandsFacade.requestPaymentShop(accessToken, patternId, params);
    }

    @Override
    public ProcessPaymentResponse processPaymentByWallet(String accessToken,
                                                         String requestId) throws IOException, InsufficientScopeException,
            InvalidTokenException {
        return apiCommandsFacade.processPaymentByWallet(accessToken, requestId);
    }

    @Override
    public ProcessPaymentResponse processPaymentByCard(String accessToken, String requestId, String csc)
            throws IOException, InsufficientScopeException, InvalidTokenException {

        return apiCommandsFacade.processPaymentByCard(accessToken, requestId, csc);
    }

    @Override
    public OperationIncome notifyIncome(String accessToken, String lastOperationId)
            throws InsufficientScopeException, InvalidTokenException, IOException {

        OperationDetailResponse lastKnownOperationResponse = operationDetail(accessToken, lastOperationId);
        if (!lastKnownOperationResponse.isSuccess()) {
            throw new IllegalArgumentException(String.format("wrong operation_id: %s, error: %s",
                    lastOperationId, lastKnownOperationResponse.getError()));
        }

        List<Operation> list = new ArrayList<Operation>();
        Date currentTime = new Date();
        Integer rowStart = 0;
        do {
            OperationHistoryResponse res = operationHistory(accessToken, rowStart, 100,
                    EnumSet.of(OperationHistoryType.deposition), true, lastKnownOperationResponse.getDatetime(), currentTime, null);

            list.addAll(res.getOperations());
            rowStart = res.getNextRecord();

        } while (rowStart != null);

        Operation lastOperation = list.isEmpty() ? lastKnownOperationResponse : list.get(0);
        return new OperationIncome(list, lastOperation.getOperationId());
    }
}
