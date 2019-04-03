package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.util.DateUtils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;

public abstract class AbstractHistoryDialog extends BaseDialogFragment implements View.OnClickListener {

    protected static final String DATE_FORMAT = DateUtils.DATE_FORMAT_DD_MM_YYYY;

    protected Lazy<ServiceManager> serviceManager;

    private Date startDate, endDate;

    @BindView(R.id.fromDateBTN) Button fromDateBTN;
    @BindView(R.id.toDateBTN) Button toDateBTN;
    @BindView(R.id.sendHistorySendBTN) Button sendBTN;
    @BindView(R.id.sendHistoryCancelBTN) Button cancelBTN;

    @BindView(R.id.sendEmailProgress) ProgressBar mProgressBar;

    protected boolean lockCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.setCanceledOnTouchOutside(false);
        d.setTitle(getActivity().getResources().getString(R.string.send_history));

        return d;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents(view);
    }

    private void showToDatePicker() {
        Calendar newCalendar = Calendar.getInstance();
        if (endDate != null) {
            newCalendar.setTime(endDate);
        }
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                endDate = DateUtils.toEndOfDay(c.getTime());

                //validate the dates while selected
                if (startDate != null && endDate.before(startDate)) {
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.select_valid_date));
                } else {
                    toDateBTN.setText(DateUtils.format(endDate, DATE_FORMAT));
                }
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showFromDatePicker() {
        Calendar newCalendar = Calendar.getInstance();
        if (startDate != null) {
            newCalendar.setTime(startDate);
        }
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                startDate = DateUtils.toStartOfDay(c.getTime());

                //validate the dates while selected
                if (endDate != null && startDate.after(endDate)) {
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.select_valid_date));
                } else {
                    fromDateBTN.setText(DateUtils.format(startDate, DATE_FORMAT));
                }
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fromDateBTN:
                showFromDatePicker();
                break;

            case R.id.toDateBTN:
                showToDatePicker();
                break;

            case R.id.sendHistorySendBTN:
                if (startDate == null || endDate == null) {
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.from_and_to_date));
                } else {
                    handleClickPositive(startDate, endDate);
                }
                break;

            case R.id.sendHistoryCancelBTN:
                if (lockCancel) {
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.history_please_wait));
                } else {
                    getDialog().dismiss();
                }
                break;
        }
    }

    protected abstract void handleClickPositive(Date startDate, Date endDate);

    //implement the UI
    private void initializeComponents(View view) {
        ButterKnife.bind(this, view);
        fromDateBTN.setOnClickListener(this);
        toDateBTN.setOnClickListener(this);
        sendBTN.setOnClickListener(this);
        cancelBTN.setOnClickListener(this);
    }

    protected void enableButtons() {
        mProgressBar.setVisibility(View.INVISIBLE);
        lockCancel = false;
        sendBTN.setEnabled(true);
        fromDateBTN.setEnabled(true);
        toDateBTN.setEnabled(true);
    }

    protected void disableButtons() {
        mProgressBar.setVisibility(View.VISIBLE);
        lockCancel = true;
        sendBTN.setEnabled(false);
        fromDateBTN.setEnabled(false);
        toDateBTN.setEnabled(false);
    }

}
