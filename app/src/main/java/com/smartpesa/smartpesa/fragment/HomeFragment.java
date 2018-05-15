package com.smartpesa.smartpesa.fragment;

import com.smartpesa.smartpesa.R;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

public class HomeFragment extends BaseFragment {

    @Bind(R.id.merchantInitial_TV) TextView merchantInitialTV;
    @Bind(R.id.merchantNameTV) TextView merchantNameTV;
    @Bind(R.id.operator_name_tv) TextView operatorNameTv;
    @Bind(R.id.historyLabelTv) TextView historyLabelTv;
    @Bind(R.id.otherLabelTv) TextView otherLabelTv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        setUpMerchant();
    }

    private void setUpMerchant() {

        VerifiedMerchantInfo mVerifyMerchantInfo = getMerchantComponent(getActivity()).provideMerchant();
        String merchantName = mVerifyMerchantInfo.getMerchantName();
        String merchantInitial = merchantName.substring(0, 1);
        String operatorName = mVerifyMerchantInfo.getOperatorName();

        if (!TextUtils.isEmpty(merchantInitial)) {
            merchantInitialTV.setText(merchantInitial);
        }

        if (!TextUtils.isEmpty(merchantName)) {
            merchantNameTV.setText(merchantName);
        } else {
            merchantNameTV.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(operatorName)) {
            operatorNameTv.setText(operatorName);
        } else {
            operatorNameTv.setVisibility(View.GONE);
        }

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-BoldCondensed.ttf");
        merchantNameTV.setTypeface(font);
        historyLabelTv.setTypeface(font);
        otherLabelTv.setTypeface(font);
//        operatorNameTv.setTypeface();
    }
}

