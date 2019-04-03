package com.smartpesa.smartpesa.fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.fragment.dialog.ChangePinDialog;
import com.smartpesa.smartpesa.helpers.PreferredTerminalUtils;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.merchant.GetMerchantInfoCallback;
import smartpesa.sdk.models.merchant.Merchant;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

public class MerchantInfoFragment extends BaseFragment {

    @BindView(R.id.merchantInitial_TV) TextView merchantInitialTV;
    @BindView(R.id.merchantNameTV) TextView merchantNameTV;
    @BindView(R.id.code_MERCHANT_TV) TextView merchantCodeTV;
    @BindView(R.id.email_MERCHANT_TV) TextView emailTV;
    @BindView(R.id.currency_MERCHANT_TV) TextView currencyTV;
    @BindView(R.id.addressTV) TextView addressTV;
    @BindView(R.id.merchantInfophoneTV) TextView phoneTV;
    @BindView(R.id.regionTV) TextView regionTV;
    @BindView(R.id.addressLabelTV) TextView addressLabelTV;
    @BindView(R.id.checkbox_perm_all_history) CheckBox mHistoryCb;
    @BindView(R.id.checkbox_perm_manage_operators) CheckBox mManageCb;
    @BindView(R.id.checkbox_perm_print_receipt) CheckBox mPrintCb;
    @BindView(R.id.operator_name_tv) TextView operatorNameTv;
    @BindView(R.id.operator_code_tv) TextView operatorCodeTv;
    @BindView(R.id.default_terminal_ll) LinearLayout defaultTerminalLl;
    @BindView(R.id.default_terminal_name_tv) TextView defaultTerminalNameTv;
    @BindView(R.id.remove_default_btn) Button removeDefaultBtn;
    @BindView(R.id.changPinBtn) Button changePinBtn;

    Context mContext;
    Lazy<ServiceManager> serviceManager;
    VerifiedMerchantInfo mVerifyMerchantInfo;
    PreferredTerminalUtils mPreferredTerminalUtils;

    public MerchantInfoFragment() {
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
        mVerifyMerchantInfo = getMerchantComponent(getActivity()).provideMerchant();

        mPreferredTerminalUtils = new PreferredTerminalUtils(mContext, mVerifyMerchantInfo.getMerchantCode(), mVerifyMerchantInfo.getOperatorCode());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_merchant_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialiseComponents(view);
        setUpMerchantInfo();
    }

    //set up details from Cache
    private void setUpMerchantInfo() {

        if (mVerifyMerchantInfo == null) {
            return;
        }

        final String defaultTerminal = mPreferredTerminalUtils.getPreferredTerminalName();
        String merchantName = mVerifyMerchantInfo.getMerchantName();
        String merchantInitial = merchantName.substring(0, 1);
        String operatorName = mVerifyMerchantInfo.getOperatorName();

        String currency = mVerifyMerchantInfo.getCurrency().getCurrencyName();
        String email = mVerifyMerchantInfo.getEmailAddress();
        String merchantCode = mVerifyMerchantInfo.getMerchantCode();
        String operatorCode = mVerifyMerchantInfo.getOperatorCode();

        boolean manageOperators = mVerifyMerchantInfo.getProfilePermissions().canManageOperators();
        boolean viewHistory = mVerifyMerchantInfo.getProfilePermissions().canViewAllHistory();
        boolean printReceipt = mVerifyMerchantInfo.getProfilePermissions().canPrintReceipt();

        mHistoryCb.setChecked(viewHistory);
        mManageCb.setChecked(manageOperators);
        mPrintCb.setChecked(printReceipt);

        changePinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                ChangePinDialog changePinDialog = new ChangePinDialog();
                changePinDialog.setCancelable(false);
                changePinDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
                changePinDialog.show(fragmentManager, "change_pin_dialog");
            }
        });

        if (!TextUtils.isEmpty(defaultTerminal)) {
            defaultTerminalLl.setVisibility(View.VISIBLE);
            defaultTerminalNameTv.setText(defaultTerminal);
            removeDefaultBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeDefaultTerminal(defaultTerminal);
                }
            });
        }

        if (!TextUtils.isEmpty(currency)) {
            currencyTV.setText(mContext.getString(R.string.currency, currency));
        } else {
            currencyTV.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(merchantCode)) {
            merchantCodeTV.setText(mContext.getString(R.string.merchant_code_label, merchantCode));
        } else {
            merchantCodeTV.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(email)) {
            emailTV.setText(mContext.getString(R.string.email_label, email));
        } else {
            emailTV.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(merchantName)) {
            merchantNameTV.setText(merchantName);
        } else {
            merchantNameTV.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(merchantInitial)) {
            merchantInitialTV.setText(merchantInitial);
        }

        if (!TextUtils.isEmpty(operatorName)) {
            operatorNameTv.setText(mContext.getString(R.string.operator_name_label, operatorName));
        } else {
            operatorNameTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(operatorCode)) {
            operatorCodeTv.setText(mContext.getString(R.string.operator_code_label, operatorCode));
        } else {
            operatorCodeTv.setVisibility(View.GONE);
        }

        serviceManager.get().getMerchantInfo(new GetMerchantInfoCallback() {
            @Override
            public void onSuccess(Merchant merchant) {
                if (null == getActivity()) return;

                addressTV.setVisibility(View.VISIBLE);
                addressLabelTV.setVisibility(View.VISIBLE);
                phoneTV.setVisibility(View.VISIBLE);
                regionTV.setVisibility(View.VISIBLE);

                StringBuilder builder = new StringBuilder();
                concat(builder, merchant.getBillingAddressStreet());
                concat(builder, merchant.getBillingAddressCity());
                concat(builder, merchant.getBillingAddressState());
                concat(builder, merchant.getBillingAddressCountry());
                concat(builder, merchant.getBillingAddressPostalcode());

                if (!TextUtils.isEmpty(builder.toString())) {
                    addressTV.setText(builder.toString());
                } else {
                    addressLabelTV.setVisibility(View.GONE);
                    addressTV.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(merchant.getPhoneOffice())) {
                    phoneTV.setText(mContext.getString(R.string.phone_label, merchant.getPhoneOffice()));
                } else {
                    phoneTV.setVisibility(View.GONE);
                }

                if (!TextUtils.isEmpty(merchant.getBillingAddressState())) {
                    regionTV.setText(mContext.getString(R.string.region_label, merchant.getBillingAddressState()));
                } else {
                    regionTV.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(SpException exception) {
                if (null == getActivity()) return;

                if (exception instanceof SpSessionException) {
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    UIHelper.showErrorDialog(mContext, getString(R.string.app_name), exception.getMessage());
                }
            }
        });
    }

    private void removeDefaultTerminal(String defaultTerminal) {
        UIHelper.showMessageDialogWithTitleTwoButtonCallback(getActivity(),
                getString(R.string.app_name),
                String.format(getString(R.string.remove_device_confirmation), defaultTerminal),
                getString(R.string.yes),
                getString(R.string.no),
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        mPreferredTerminalUtils.clearTerminal();
                        defaultTerminalLl.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                });
    }

    private void concat(StringBuilder builder, @Nullable String toConcat) {
        if (!TextUtils.isEmpty(toConcat)) {
            if (builder.length() != 0) {
                builder.append("\n");
            }
            builder.append(toConcat);
        }
    }

    private void initialiseComponents(View view) {
        ButterKnife.bind(this, view);
        addressTV.setVisibility(View.GONE);
        addressLabelTV.setVisibility(View.GONE);
        phoneTV.setVisibility(View.GONE);
        regionTV.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_merchant_info));
        }
    }
}