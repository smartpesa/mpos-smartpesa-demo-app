package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.fragment.payment.AbstractPaymentFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;

public class MoreDialog extends BaseDialogFragment {

    @Bind(R.id.done) Button doneBtn;
    @Bind(R.id.reference_description) EditText referenceET;
    @Bind(R.id.account_spinner) MaterialBetterSpinner mAccountSpin;

    Context mContext;
    Lazy<ServiceManager> serviceManager;
    public int fromAccount;
    public String description = null;
    private AbstractPaymentFragment.MyDialogCloseListener closeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_more, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mContext = getActivity();
        getDialog().setCanceledOnTouchOutside(false);

        getDialog().setTitle(getActivity().getResources().getString(R.string.more_title));
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();

        //set the account types
        final String[] accountType = getActivity().getResources().getStringArray(R.array.account_array);
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, accountType);
        mAccountSpin.setAdapter(accountAdapter);

        //set the default from account of the merchant
        String accountName = UIHelper.getAccountNameFromType(serviceManager.get().getCachedMerchant().getPlatformPermissions().getDefaultFromAccountType());
        fromAccount = UIHelper.getAccountPositionFromEnum(serviceManager.get().getCachedMerchant().getPlatformPermissions().getDefaultFromAccountType());
        mAccountSpin.setText(accountName);

        //onItem select listener for the Account Type dropdown
        mAccountSpin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromAccount = position;
            }
        });

        //set the text input watcher and save the description
        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                description = s.toString();
            }
        };

        referenceET.addTextChangedListener(inputWatcher);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void DismissListener(AbstractPaymentFragment.MyDialogCloseListener closeListener) {
        this.closeListener = closeListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (closeListener != null) {
            closeListener.handleDialogClose(null, fromAccount, description);
        }
    }
}