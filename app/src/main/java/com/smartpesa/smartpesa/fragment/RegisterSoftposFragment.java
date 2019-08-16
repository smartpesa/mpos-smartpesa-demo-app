package com.smartpesa.smartpesa.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.helpers.UIHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.operator.ActivateSoftPOSCallback;
import smartpesa.sdk.models.softpos.RegisterSoftPOS;
import smartpesa.sdk.models.softpos.RegisterSoftPOSCallback;

public class RegisterSoftposFragment extends BaseFragment {

    Lazy<ServiceManager> serviceManager;

    @BindView(R.id.registerSoftposButton)
    Button registerSoftposButton;

    @BindView(R.id.activateTv)
    TextView activateTv;

    @BindView(R.id.androidIdTv)
    TextView androidIdTv;

    public static RegisterSoftposFragment newInstance() {
        RegisterSoftposFragment fragment = new RegisterSoftposFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_softpos, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }

        registerSoftposButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle(R.string.app_name);
                progressDialog.setMessage(getString(R.string.registering_device));
                progressDialog.show();

                serviceManager.get().registerSoftPOS(new RegisterSoftPOSCallback() {
                    @Override
                    public void onSuccess(RegisterSoftPOS registerSoftPOS) {
                        if (null == getActivity()) return;

                        progressDialog.dismiss();
                        showActivateDevice();
                    }

                    @Override
                    public void onError(SpException exception) {
                        if (null == getActivity()) return;

                        progressDialog.dismiss();
                        UIHelper.showToast(getActivity(), exception.getMessage());

                    }
                });
            }
        });

        activateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActivateDevice();
            }
        });


        if (getActivity() != null) {
            String androidId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
            if (!TextUtils.isEmpty(androidId)) {
                androidIdTv.setText("ANDROID ID: "+ androidId);
            }
        }
    }

    private void showActivateDevice() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.title_activate_softpos)
                .content(R.string.activate_instruct)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .inputRangeRes(4, 12, R.color.md_red_500)
                .input("OTP", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                    }})
                .positiveText("Activate")
                .negativeText("Cancel")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        if (dialog.getInputEditText() != null && dialog.getInputEditText().getText() != null) {
                            String otp = dialog.getInputEditText().getText().toString();
                            if (!TextUtils.isEmpty(otp)) {
                                activateDevice(otp);
                            } else {
                                UIHelper.showToast(getActivity(), "OTP cannot be empty, please try again");
                            }
                        } else {
                            UIHelper.showToast(getActivity(), "OTP cannot be empty, please try again");
                        }
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .cancelable(false)
                .show();
    }

    private void activateDevice(String otp) {
        serviceManager.get().activateSoftPOS(otp, new ActivateSoftPOSCallback() {
            @Override
            public void onSuccess(String s) {
                UIHelper.showMessageDialog(getActivity(), s);
            }

            @Override
            public void onError(SpException e) {
                UIHelper.showMessageDialog(getActivity(), e.getMessage());
            }
        });
    }
}