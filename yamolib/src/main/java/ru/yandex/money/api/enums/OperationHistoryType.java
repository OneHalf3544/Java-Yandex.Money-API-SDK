package ru.yandex.money.api.enums;

/**
 * Перечисление типов запросов истории операций. Принимает значение приход
 * (deposition) и расход (payment)
 * @author dvmelnikov
 */

public enum OperationHistoryType {
    /**
     * Тип расход (платежи)
     */
    PAYMENT("payment"),

    /**
     * Тип приход (пополнения)
     */
    DEPOSITION("deposition"),

    /**
     * Тип приход (пополнения)
     */
    INCOMING_TRANSFERS_UNACCEPTED("incoming-transfers-unaccepted");

    private final String code;

    OperationHistoryType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
