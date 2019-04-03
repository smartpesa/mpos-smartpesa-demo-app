package com.smartpesa.smartpesa.fragment;

import com.smartpesa.smartpesa.BuildConfig;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.activity.WebViewActivity;
import com.smartpesa.smartpesa.helpers.PreferenceHelper;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.util.constants.PreferenceConstants;
import com.smartpesa.smartpesa.util.constants.SPConstants;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.device.SendLogCallback;

public class AboutFragment extends Fragment implements View.OnClickListener {

    private static final String BUNDLE_KEY_UPDATE_MANDATORY = AboutFragment.class.getName() + "updateMandatory";
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    @BindView(R.id.app_version_tv) TextView appVersionTv;
    @BindView(R.id.sdk_version_tv) TextView sdkVersionTv;
    @BindView(R.id.build_date) TextView buildDateTv;
    @BindView(R.id.server_version_tv) TextView serverVersionTv;
    @BindView(R.id.copyright_tv) TextView copyrightTv;
    @BindView(R.id.privacy_tv) TextView privacyBtn;
    @BindView(R.id.is_up_to_date_cb) CheckBox updateCb;
    @BindView(R.id.update_btn) Button updateBtn;
    @BindView(R.id.dial_btn) Button dialBtn;
    @BindView(R.id.send_log_btn) Button sendLogBtn;
    @BindView(R.id.powered_by_iv) ImageView poweredByIv;

    Context mContext;
    boolean updateMandatory;
    PreferenceHelper mPreferenceHelper;
    ProgressDialog mProgressDialog;
    Lazy<ServiceManager> serviceManager;

    public static AboutFragment newInstance(boolean updateMandatory) {
        AboutFragment fragment = new AboutFragment();
        Bundle aboutBundle = new Bundle();
        aboutBundle.putBoolean(BUNDLE_KEY_UPDATE_MANDATORY, updateMandatory);
        fragment.setArguments(aboutBundle);
        return fragment;
    }

    public static boolean isUpdateMandatory(Bundle bundle) {
        return bundle.getBoolean(BUNDLE_KEY_UPDATE_MANDATORY, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);
        initializeComponents(view);

        if (getActivity().getPackageName().equals(SPConstants.MAIN_BUILD_PACKAGE_NAME)) {
            poweredByIv.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean isOptional = mPreferenceHelper.getBoolean(PreferenceConstants.KEY_IS_OPTIONAL, false);
        final String url = mPreferenceHelper.getString(PreferenceConstants.KEY_SERVER_APK_URL);
        final String version = mPreferenceHelper.getString(PreferenceConstants.KEY_APP_VERSION);

        if (isOptional) {
            updateCb.setText("A new version of " + getString(R.string.app_name) + " v" + version + " is available.");
            updateBtn.setEnabled(true);
            updateBtn.setVisibility(View.VISIBLE);
            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
        } else {
            updateCb.setText(R.string.up_to_date);
            updateBtn.setVisibility(View.GONE);
        }

        setValues();
    }

    private void setValues() {
        String version = String.format("%1$s.%2$s", BuildConfig.VERSION_NAME, BuildConfig.DEBUG ? getString(R.string.debug_build_suffix) : getString(R.string.release_build_suffix));
        appVersionTv.setText(getString(R.string.app_version, version));
        serverVersionTv.setText(getString(R.string.server_version, mPreferenceHelper.getString(PreferenceConstants.KEY_SERVER_VERSION)));
        sdkVersionTv.setText(getString(R.string.sdk_version, SmartPesa.SDK_VERSION));
        buildDateTv.setText(BuildConfig.BUILD_TIME);
    }

    private void initializeComponents(View view) {
        ButterKnife.bind(this, view);
        mContext = getActivity();
        mPreferenceHelper = SmartPesaApplication.component(getActivity()).preferenceHelper();
        dialBtn.setOnClickListener(this);
        sendLogBtn.setOnClickListener(this);
        privacyBtn.setOnClickListener(this);

        updateBtn.setVisibility(View.GONE);

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getResources().getString(R.string.sending_log));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dial_btn:
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
                } else {
                    callPhone();
                }
                break;
            case R.id.send_log_btn:
                sendLogToServer();
                break;
            case R.id.privacy_tv:
                Intent webViewIntent = new Intent(getActivity(), WebViewActivity.class);
                webViewIntent.putExtra("urlToLoad", getActivity().getString(R.string.eula_url));
                startActivity(webViewIntent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPhone();
                }
            }
        }
    }

    private void callPhone() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getString(R.string.customer_support_number)));
        startActivity(intent);
    }

    //sending the log to server
    private void sendLogToServer() {

        mProgressDialog.setMessage(mContext.getResources().getString(R.string.please_wait));
        mProgressDialog.show();

        serviceManager.get().sendLog(new SendLogCallback() {
                                         @Override
                                         public void onSuccess(Boolean aBoolean) {
                                             if (null == getActivity()) return;

                                             mProgressDialog.dismiss();

                                             if (aBoolean) {
                                                 UIHelper.showToast(mContext, mContext.getResources().getString(R.string.log_sent));
                                             } else {
                                                 UIHelper.showToast(mContext, mContext.getResources().getString(R.string.error_sending_log));
                                             }
                                         }

                                         @Override
                                         public void onError(SpException exception) {
                                             if (null == getActivity()) return;

                                             if (exception instanceof SpSessionException) {
                                                 mProgressDialog.dismiss();
                                                 //show the expired message
                                                 UIHelper.showToast(getActivity(), getString(R.string.session_expired));

                                                 //finish the current activity
                                                 getActivity().finish();

                                                 //start the splash screen
                                                 startActivity(new Intent(getActivity(), SplashActivity.class));
                                             } else {
                                                 mProgressDialog.dismiss();

                                                 UIHelper.showErrorDialog(getActivity(), getResources().getString(R.string.app_name), exception.getMessage());
                                             }
                                         }

                                     }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_about));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}