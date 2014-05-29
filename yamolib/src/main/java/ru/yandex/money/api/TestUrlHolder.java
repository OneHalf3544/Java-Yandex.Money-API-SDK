package ru.yandex.money.api;

import ru.yandex.money.api.response.util.PaymentErrorCode;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Объект для формирования url для тестовых запросов.
 * К адресу добавляются GET-параметры, которые определяют, какой ответ должен вернуть сервер Яндекс.Денег
 * <p/>
 * <p/>
 * Created: 26.10.13 11:41
 * <p/>
 *
 * @author OneHalf
 */
public class TestUrlHolder implements CommandUrlHolder {

    public static final PaymentErrorCode SUCCESS_CODE = new PaymentErrorCode() {
        @Override
        public String getCode() {
            return "success";
        }
    };

    private final String url;

    private volatile boolean testPayment = true;
    private volatile String testCard = null;
    private volatile PaymentErrorCode testResult = null;

    public TestUrlHolder() {
        this(ApiCommandsFacade.URI_YM_API);
    }

    public TestUrlHolder(String url) {
        this.url = url;
    }

    @Override
    public URI getUrlForCommand(String commandName) {
        return URI.create(url + '/' + commandName);
    }

    public boolean isTestPayment() {
        return testPayment;
    }

    /**
     *
     * @param testPayment true, если платеж должен быть тестовым
     */
    public void setTestPayment(boolean testPayment) {
        this.testPayment = testPayment;
    }

    public String getTestCard() {
        return testCard;
    }

    /**
     * Отдавать ли признак наличия привязанной к счету карты
     */
    public void setTestCard(String testCard) {
        this.testCard = testCard;
    }

    public PaymentErrorCode getTestResult() {
        return testResult;
    }

    /**
     * @param testResult Код ошибки, которую должен вернуть метод Яндекс.Денег
     *                   Чтобы возвращался успех, установите "success" или null
     */
    public void setTestResult(PaymentErrorCode testResult) {
        this.testResult = testResult;
    }

    @Override
    public Map<String, String> getAdditionalParams() {
        if (!testPayment) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put("test_payment", "true");

        if (testCard != null) {
            result.put("test_card", testCard);
        }
        if (testResult != null) {
            result.put("test_result", testResult.getCode());
        }
        return result;
    }
}
