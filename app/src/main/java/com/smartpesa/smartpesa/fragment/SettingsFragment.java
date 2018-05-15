package com.smartpesa.smartpesa.fragment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.OTAProgressActivity;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.adapters.DevicesAdapter;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.device.Device;
import smartpesa.sdk.models.device.GetDevicesCallback;

public class SettingsFragment extends BaseFragment {

    Lazy<ServiceManager> serviceManager;
    ListView settingsLV;
    Context mContext;
    ProgressBar mProgressBar;

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