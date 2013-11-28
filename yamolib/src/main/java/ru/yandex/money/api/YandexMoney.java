package ru.yandex.money.api;

import ru.yandex.money.api.response.OperationIncome;

import java.io.IOException;

/**
 * Интерфейс общения приложения с API Яндекс.Деньги.
 *
 * @author dvmelnikov
 */
public interface YandexMoney extends TokenRequester, ApiCommandsFacade {

    /**
     * Метод возвращает список входящих операций. Из за огричения протокола, список операций может содеражть
     * отмененные транзакции.
     *
     * @param accessToken токен авторизации пользователя
     * @param lastOperation индетификатор последней проверенной операции, может быть  null
     *
     * @return  возвращает экземпляр класса {@link OperationIncome}
     */
    public OperationIncome notifyIncome(String accessToken, String lastOperation) throws InsufficientScopeException, InvalidTokenException, IOException;
}
