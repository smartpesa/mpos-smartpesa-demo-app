package com.smartpesa.smartpesa.fragment.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.devices.SpPrinterDevice;

public class PrinterDialogFragment extends BluetoothDialogFragment<SpPrinterDevice> {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferredCB.setVisibility(View.GONE);

        bluetoothTitleTv.setText("Select a printer");

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
                ServiceManager.get(getActivity()).stopScan();
            }
        });

    }
}