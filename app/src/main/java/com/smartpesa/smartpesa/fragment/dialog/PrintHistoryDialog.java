package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.devices.SpPrinterDevice;
import smartpesa.sdk.error.SpPrinterException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.interfaces.PrintingCallback;
import smartpesa.sdk.models.printing.AbstractPrintingDefinition;
import smartpesa.sdk.models.receipt.GetReceiptCallback;
import smartpesa.sdk.scanner.PrinterScanningCallback;

public class PrintHistoryDialog extends AbstractHistoryDialog {

    private static final String BLUETOOTH_FRAGMENT_TAG = "bluetooth";

    List<AbstractPrintingDefinition> dataToPrint;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);

        d.setTitle(getActivity().getResources().getString(R.string.print_history));

        return d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_print_history, container, false);
    }

    @Override
    protected void handleClickPositive(Date startDate, Date endDate) {
        printHistory(startDate, endDate);
    }

    private void printHistory(Date startDate, Date endDate) {
        disableButtons();
        serviceManager.get().getHistoryReceipt(startDate, endDate, new GetReceiptCallback() {
            @Override
            public void onSuccess(List<AbstractPrintingDefinition> abstractPrintingDefinitions) {
                if (null == getActivity()) return;
                scanPrinter();
                dataToPrint = abstractPrintingDefinitions;
                enableButtons();
            }

            @Override
            public void onError(SpException exception) {
                if (null == getActivity()) return;
                if (exception instanceof SpSessionException) {
                    enableButtons();
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    enableButtons();
                    UIHelper.showErrorDialog(getActivity(), getString(R.string.app_name), exception.getMessage());
                }

            }
        });
    }

    private void scanPrinter() {
        serviceManager.get().scanPrinter(new PrinterScanningCallback() {
            @Override
            public void onDeviceListRefresh(Collection<SpPrinterDevice> collection) {
                if (null == getActivity()) return;
                displayPrinterDevice(collection);
            }

            @Override
            public void onScanStopped() {
                if (null == getActivity()) return;
                UIHelper.showToast(getActivity(), getString(R.string.sp__bluetooth_scan_stopped));
            }

            @Override
            public void onScanTimeout() {
                if (null == getActivity()) return;
                UIHelper.showToast(getActivity(), getString(R.string.sp__bluetooth_scan_timeout));
            }

            @Override
            public void onEnablingBluetooth(String s) {

            }

            @Override
            public void onBluetoothPermissionDenied(String[] strings) {

            }
        });
    }

    private void performPrint(SpPrinterDevice device) {
        serviceManager.get().performPrint(SmartPesa.PrintingParam.withData(dataToPrint).printerDevice(device).build(), new PrintingCallback() {
            @Override
            public void onPrinterError(SpPrinterException errorMessage) {
                if (null == getActivity()) return;
                UIHelper.showErrorDialog(getActivity(), getActivity().getResources().getString(R.string.app_name), errorMessage.getMessage());
            }

            @Override
            public void onPrinterSuccess() {
                if (null == getActivity()) return;
            }
        });
    }

    private void displayPrinterDevice(Collection<SpPrinterDevice> devices) {
        PrinterDialogFragment dialog;
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (fragment == null) {
            dialog = new PrinterDialogFragment();
            dialog.show(getActivity().getSupportFragmentManager(), BLUETOOTH_FRAGMENT_TAG);
        } else {
            dialog = (PrinterDialogFragment) fragment;
        }
        dialog.setSelectedListener(new PrinterSelectedImpl());
        dialog.updateDevices(devices);
    }

    private class PrinterSelectedImpl implements BluetoothDialogFragment.TerminalSelectedListener<SpPrinterDevice> {
        @Override
        public void onSelected(SpPrinterDevice device) {
            performPrint(device);
        }

        @Override
        public void onCancelled() {

        }
    }
}
