package com.smartpesa.smartpesa.adapters;

import com.smartpesa.smartpesa.fragment.history.HistoryTransactionFragment;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class HistoryFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private final ArrayList<ParcelableTransactionResponse> mTransactionResponseList;

    public HistoryFragmentPagerAdapter(FragmentManager fm, ArrayList<ParcelableTransactionResponse> transactionResponseList) {
        super(fm);
        mTransactionResponseList = transactionResponseList;
    }

    @Override
    public Fragment getItem(int position) {
        ParcelableTransactionResponse transactionResponse = mTransactionResponseList.get(position);
        HistoryTransactionFragment transactionFragment = HistoryTransactionFragment.newInstance(transactionResponse);

        return transactionFragment;
    }

    @Override
    public int getCount() {
        return mTransactionResponseList.size();
    }
}
