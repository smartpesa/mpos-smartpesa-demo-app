package com.smartpesa.smartpesa.fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.OTAProgressActivity;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.adapters.DevicesAdapter;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.device.Device;
import smartpesa.sdk.models.device.GetDevicesCallback;
import smartpesa.sdk.models.operator.ActivateSoftPOSCallback;
import smartpesa.sdk.models.softpos.RegisterSoftPOS;
import smartpesa.sdk.models.softpos.RegisterSoftPOSCallback;

public class SettingsFragment extends BaseFragment {

    Lazy<ServiceManager> serviceManager;
    ListView settingsLV;
    Context mContext;
    ProgressBar mProgressBar;
    Button registerBtn, activateBtn;
    TextView softPosInstruct;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();
        mContext = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeComponents(view);
        getDevices();

        return view;
    }

    private void getDevices() {

        mProgressBar.setVisibility(View.VISIBLE);

        serviceManager.get().getDevices(new GetDevicesCallback() {
            @Override
            public void onSuccess(List<Device> devices) {
                if (null == getActivity()) return;

                mProgressBar.setVisibility(View.GONE);

                if (devices != null && devices.size() != 0) {
                    DevicesAdapter adapter = new DevicesAdapter(mContext, R.layout.row_settings, devices);
                    settingsLV.setAdapter(adapter);
                } else {
                    UIHelper.showToast(mContext, mContext.getResources().getString(R.string.no_devices_found));
                }

            }

            @Override
            public void onError(SpException exception) {
                if (null == getActivity()) return;

                if (exception instanceof SpSessionException) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());
                }
            }
        });
    }

    private void initializeComponents(View view) {
        settingsLV = (ListView) view.findViewById(R.id.settingsLV);
        mProgressBar = (ProgressBar) view.findViewById(R.id.settingsPB);
        registerBtn = (Button) view.findViewById(R.id.registerBtn);
        activateBtn = (Button) view.findViewById(R.id.activateBtn);
        softPosInstruct = (TextView) view.findViewById(R.id.softposInstruct);

        NfcManager manager = (NfcManager) getActivity().getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null ) {
            //Yes NFC available
            registerBtn.setVisibility(View.VISIBLE);
            activateBtn.setVisibility(View.VISIBLE);
            softPosInstruct.setVisibility(View.VISIBLE);
        } else{
            //Your device doesn't support NFC
            registerBtn.setVisibility(View.GONE);
            activateBtn.setVisibility(View.GONE);
            softPosInstruct.setVisibility(View.GONE);
        }

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDevice();
            }
        });

        activateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActivateDevice();
            }

        });
    }

    private void showActivateDevice() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.activate_device)
                .content(R.string.activate_instruct)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .inputRangeRes(4, 12, R.color.md_red_500)
                .input("OTP", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                    }})
                .positiveText("Proceed")
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

    private void showRegisterDevice() {
        UIHelper.showMessageDialogWithTitleTwoButtonCallback(getActivity(),
                getString(R.string.register_device),
                getString(R.string.register_instruct),
                getString(R.string.yes),
                getString(R.string.no),
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        registerDevice();

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                });
    }

    private void registerDevice() {
        serviceManager.get().registerSoftPOS(new RegisterSoftPOSCallback() {
            @Override
            public void onSuccess(RegisterSoftPOS registerSoftPOS) {
                UIHelper.showMessageDialog(getActivity(), getString(R.string.device_register_success));
            }

            @Override
            public void onError(SpException e) {
                UIHelper.showMessageDialog(getActivity(), e.getMessage());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_settings));
        }
    }

    //more fragment dialog box
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_inject:
                startActivity(new Intent(getActivity(), OTAProgressActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}