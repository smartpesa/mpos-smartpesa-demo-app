package com.smartpesa.smartpesa.fragment.history;

import java.math.BigDecimal;

public class DisplayStatistics {

    private int count;
    private BigDecimal amount;
    private String transactionDescription;

    public DisplayStatistics() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }


}