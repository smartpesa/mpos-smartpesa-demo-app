package com.smartpesa.smartpesa.util;

import java.math.BigDecimal;

public class SmallCalculator {

    private static final int ADD = 1;
    private static final int NONE = 0;
    private static final int SUB = -1;

    private int lastOperation;
    private BigDecimal mCurrent;

    public BigDecimal add(BigDecimal value) {
        doOperation(value);
        lastOperation = ADD;
        return mCurrent;
    }

    private void doAdd(BigDecimal value) {
        if (mCurrent == null || mCurrent.compareTo(BigDecimal.ZERO) == 0) {
            mCurrent = value;
        } else {
            mCurrent = mCurrent.add(value);
        }
    }

    public BigDecimal sub(BigDecimal value) {
        doOperation(value);
        lastOperation = SUB;
        return mCurrent;
    }

    private void doSub(BigDecimal value) {
        if (mCurrent == null || mCurrent.compareTo(BigDecimal.ZERO) == 0) {
            mCurrent = value;
        } else {
            mCurrent = mCurrent.subtract(value);
            if (mCurrent.compareTo(BigDecimal.ZERO) < 0) {
                mCurrent = BigDecimal.ZERO;
            }
        }
    }

    public BigDecimal equal(BigDecimal value) {
        doOperation(value);
        BigDecimal r = mCurrent;
        clear();
        return r;
    }

    private void doOperation(BigDecimal value) {
        if (lastOperation == ADD) {
            doAdd(value);
        } else if (lastOperation == SUB) {
            doSub(value);
        } else {
            mCurrent = value;
        }
    }

    public void clear() {
        lastOperation = NONE;
        mCurrent = null;
    }

    public boolean isPerformingOperation() {
        return lastOperation != NONE && mCurrent != null;
    }
}
