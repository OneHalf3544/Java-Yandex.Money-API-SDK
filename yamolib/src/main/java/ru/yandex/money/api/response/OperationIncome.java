package ru.yandex.money.api.response;

import ru.yandex.money.api.response.util.Operation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OperationIncome implements Serializable {
    private static final long serialVersionUID = -8037413891091982716L;

    private List<Operation> list;

    private String lastOperation;

    public OperationIncome(Collection<Operation> list, String maxOperation) {
        this.lastOperation = maxOperation;
        this.list = new ArrayList<Operation>(list);
    }

    public String getLastOperation() {
        return lastOperation;
    }

    public void setLastOperation(String lastOperation) {
        this.lastOperation = lastOperation;
    }

    public List<Operation> getList() {
        return list;
    }

    public void setList(List<Operation> list) {
        this.list = list;
    }
}
