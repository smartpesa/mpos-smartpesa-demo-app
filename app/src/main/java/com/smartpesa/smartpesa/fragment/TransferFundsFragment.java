package com.smartpesa.smartpesa.fragment;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.MainActivity;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.fragment.payment.FundTransferPaymentFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.institutions.AcquiringInstitution;
import smartpesa.sdk.models.institutions.GetAcquiringInstitutionCallback;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

public class TransferFundsFragment extends BaseFragment implements View.OnClickListener {
    MaterialBetterSpinner destinationBankSpinner, fromAccountTypeSpinner, toAccountTypeSpinner;
    Context mContext;
    Lazy<ServiceManager> serviceManager;
    Button continueBTN;
    ProgressBar mProgressBar;
    int fromAccount, toAccount;
    List<AcquiringInstitution> institutionList;
    UUID aiic;
    SmartPesaTransactionType transactionType;
    VerifiedMerchantInfo verifyMerchantInfo;

    public TransferFundsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();

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
        View view = inflater.inflate(R.layout.fragment_transfer_funds, container, false);
        initializeComponents(view);

        processFundTransfer();

        //get default from account
        fromAccountTypeSpinner.setText(UIHelper.getAccountNameFromType(verifyMerchantInfo.getPlatformPermissions().getDefaultFromAccountType()));
        fromAccount = UIHelper.getAccountPositionFromEnum(verifyMerchantInfo.getPlatformPermissions().getDefaultFromAccountType());

        //get default to account
        toAccountTypeSpinner.setText(UIHelper.getAccountNameFromType(verifyMerchantInfo.getPlatformPermissions().getDefaultToAccountType()));
        toAccount = UIHelper.getAccountPositionFromEnum(verifyMerchantInfo.getPlatformPermissions().getDefaultToAccountType());

        fromAccountTypeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromAccount = position;
            }
        });

        toAccountTypeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toAccount = position;
            }
        });

        destinationBankSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!TextUtils.isEmpty(destinationBankSpinner.getText().toString())) {
                    aiic = institutionList.get(position).getAcquiringInsId();
                }
            }
        });
        return view;
    }

    private void getAcquiringDetails() {

        mProgressBar.setVisibility(View.VISIBLE);

        serviceManager.get().getAcquiringInstitutions(new GetAcquiringInstitutionCallback() {
            @Override
            public void onSuccess(List<AcquiringInstitution> acquiringInstitutions) {
                if (null == getActivity()) return;

                mProgressBar.setVisibility(View.GONE);

                institutionList = acquiringInstitutions;

                ArrayList<String> institutions = new ArrayList<String>();

                for (int i = 0; i < institutionList.size(); i++) {
                    institutions.add(institutionList.get(i).getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, institutions);

                destinationBankSpinner.setAdapter(adapter);
            }

            @Override
            public void onError(SpException exception) {
                if (null == getActivity()) return;

                if (exception instanceof SpSessionException) {
                    mProgressBar.setVisibility(View.GONE);
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transferContinueBTN:
                if (TextUtils.isEmpty(destinationBankSpinner.getText().toString())) {
                    UIHelper.showToast(mContext, mContext.getResources().getString(R.string.select_bank));
                    YoYo.with(Techniques.Shake)
                            .duration(1000)
                            .playOn(destinationBankSpinner);
                } else {
                    if (getActivity() instanceof MainActivity) {
                        FundTransferPaymentFragment fragment = FundTransferPaymentFragment.newInstance(SmartPesaTransactionType.GENERAL_TRANSFER, fromAccount, toAccount, aiic.toString());
                        MainActivity.getComponent((MainActivity) getActivity()).provideNavigator()
                                .pushFragment(fragment);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void initializeComponents(View view) {
        destinationBankSpinner = (MaterialBetterSpinner) view.findViewById(R.id.destinationBankSpin);
        fromAccountTypeSpinner = (MaterialBetterSpinner) view.findViewById(R.id.fromAccountTypeSpin);
        toAccountTypeSpinner = (MaterialBetterSpinner) view.findViewById(R.id.toAccountTypeSpin);

        continueBTN = (Button) view.findViewById(R.id.transferContinueBTN);
        continueBTN.setOnClickListener(this);

        mProgressBar = (ProgressBar) view.findViewById(R.id.transferFundsPB);

        String[] list = {""};
        String[] accountType = getActivity().getResources().getStringArray(R.array.account_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, list);
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, accountType);
        destinationBankSpinner.setAdapter(adapter);
        fromAccountTypeSpinner.setAdapter(accountAdapter);
        toAccountTypeSpinner.setAdapter(accountAdapter);
    }

    private void processFundTransfer() {
        getAcquiringDetails();
        transactionType = SmartPesaTransactionType.GENERAL_TRANSFER;
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_transfer_funds));
        }
    }
}
