package com.smartpesa.smartpesa.fragment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.payment.AllPaymentsProgressActivity;
import com.smartpesa.smartpesa.fragment.payment.AbstractPaymentFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.constants.SPConstants;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

public class LoyaltyInquiryFragment extends BaseFragment implements View.OnClickListener {

    Button balanceEnquiryBTN;
    MaterialBetterSpinner accountTypeSpinner;
    Context mContext;
    Lazy<ServiceManager> serviceManager;
    int fromAccount;
    VerifiedMerchantInfo verifyMerchantInfo;
    TextView instratuctionLabel;

    public LoyaltyInquiryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }

        verifyMerchantInfo = getMerchantComponent(getActivity()).provideMerchant();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inquiry, container, false);

        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();

        initializeUIComponents(view);

        instratuctionLabel.setText("Select account for Loyalty inquiry");
        accountTypeSpinner.setText(UIHelper.getAccountNameFromType(verifyMerchantInfo.getPlatformPermissions().getDefaultFromAccountType()));
        fromAccount = UIHelper.getAccountPositionFromEnum(verifyMerchantInfo.getPlatformPermissions().getDefaultFromAccountType());

        accountTypeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromAccount = position;
            }
        });

        return view;
    }

    private void performLoyaltyInquiry() {
        Intent paymentIntent = new Intent(getActivity(), AllPaymentsProgressActivity.class);
        paymentIntent.putExtra(SPConstants.AMOUNT, 0.00);
        paymentIntent.putExtra(SPConstants.CASH_BACK_AMOUNT, 0.00);
        paymentIntent.putExtra(SPConstants.TRANSACTION_TYPE, SmartPesaTransactionType.LOYALTY.getEnumId());
        paymentIntent.putExtra(SPConstants.FROM_ACCOUNT, fromAccount);
        getActivity().startActivityForResult(paymentIntent, AbstractPaymentFragment.REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.balanceInquiryBTN:
                performLoyaltyInquiry();
                break;
        }
    }

    //check if any fragment dialog is open and close it
    private void closeDialogFragment() {
        android.app.Fragment dialogBluetoothList = getActivity().getFragmentManager().findFragmentByTag("dialog_bluetooth_list");
        if (dialogBluetoothList != null) {
            DialogFragment dialogFragment = (DialogFragment) dialogBluetoothList;
            dialogFragment.dismiss();
        }
    }

    //initialize UI components
    private void initializeUIComponents(View view) {

        balanceEnquiryBTN = (Button) view.findViewById(R.id.balanceInquiryBTN);
        instratuctionLabel = (TextView) view.findViewById(R.id.instructionLabel);
        accountTypeSpinner = (MaterialBetterSpinner) view.findViewById(R.id.inquiryAccountTypeSpin);

        String[] accountType = getActivity().getResources().getStringArray(R.array.account_array);
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, accountType);
        accountTypeSpinner.setAdapter(accountAdapter);

        balanceEnquiryBTN.setOnClickListener(this);
    }

    //to set the title of the fragment
    @Override
    public void onResume() {
        super.onResume();

        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_loyalty_inquiry);
        }

    }

}
