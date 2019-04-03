package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.payment.AbstractPaymentProgressActivity;
import com.smartpesa.smartpesa.adapters.LoyaltyAdapter;
import com.smartpesa.smartpesa.models.UIRedeemable;
import com.smartpesa.smartpesa.util.MoneyUtils;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;

public class LoyaltyDialogFragment extends BaseDialogFragment {

    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.loyaltyBtn) Button loyaltyBtn;
    @BindView(R.id.loyaltyTitle) TextView loyaltyTitle;
    @BindView(R.id.discountedAmountTv) TextView discountedAmountTv;
    @BindView(R.id.adjestedAmountTv) TextView adjustedAmountTv;
    @BindView(R.id.originalAmountTv) TextView originalAmountTv;

    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    ArrayList<UIRedeemable> mRedeemableArrayList;
    BigDecimal adjustedAmount, discountAmount, originalAmount;
    Lazy<ServiceManager> serviceManager;

    @Nullable public MoneyUtils mMoneyUtils;

    public static LoyaltyDialogFragment newInstance(ArrayList<UIRedeemable> redeemableArrayList, BigDecimal adjustedAmount, BigDecimal discountAmount, BigDecimal originalAmount) {
        LoyaltyDialogFragment fragment = new LoyaltyDialogFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putParcelableArrayList("loyalty", redeemableArrayList);
        paymentBundle.putString("adjustedAmount", adjustedAmount.toString());
        paymentBundle.putString("discountAmount", discountAmount.toString());
        paymentBundle.putString("originalAmount", originalAmount.toString());
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setTitle(R.string.loyalty_redemption);
        d.setCancelable(false);
        return d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_loyalty, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mMoneyUtils = SmartPesaApplication.merchantComponent(getActivity()).provideMoneyUtils();
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();

        mRedeemableArrayList = getArguments().getParcelableArrayList("loyalty");
        adjustedAmount = new BigDecimal(getArguments().getString("adjustedAmount"));
        discountAmount = new BigDecimal(getArguments().getString("discountAmount"));
        originalAmount = new BigDecimal(getArguments().getString("originalAmount"));

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new LoyaltyAdapter(mRedeemableArrayList, getActivity(), false, LoyaltyDialogFragment.this);
        mRecyclerView.setAdapter(mAdapter);

        loyaltyBtn.setText("Continue");
        loyaltyTitle.setText("Select Redemptions to redeem");
        adjustedAmountTv.setText(mMoneyUtils.format(adjustedAmount));
        discountedAmountTv.setText(mMoneyUtils.format(discountAmount));
        originalAmountTv.setText(mMoneyUtils.format(originalAmount));

        loyaltyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> positions = new ArrayList<Integer>();
                for (int i = 0; i < mRedeemableArrayList.size(); i++) {
                    if (mRedeemableArrayList.get(i).isSelected()) {
                        positions.add(i);
                    }
                }
                dismiss();
                ((AbstractPaymentProgressActivity) getActivity()).processRedeemableList(positions);
            }
        });
    }

    public void updateAmount(BigDecimal redeemAmount, boolean select) {
        if (select) {
            discountAmount = discountAmount.add(redeemAmount);
            adjustedAmount = originalAmount.subtract(discountAmount);

            setDiscountedAmount(mMoneyUtils.format(discountAmount));
            adjustedAmountTv.setText(mMoneyUtils.format(adjustedAmount));
        } else {
            discountAmount = discountAmount.subtract(redeemAmount);
            adjustedAmount = originalAmount.subtract(discountAmount);

            setDiscountedAmount(mMoneyUtils.format(discountAmount));
            adjustedAmountTv.setText(mMoneyUtils.format(adjustedAmount));
        }
    }

    public void setDiscountedAmount(String amount) {
        discountedAmountTv.setText("-" + amount);
    }
}
