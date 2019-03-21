package com.smartpesa.smartpesa.fragment.lead;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.LeadActivity;
import com.smartpesa.smartpesa.helpers.CustomMaterialSpinner;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.util.DateUtils;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NameFragment extends Fragment {

    protected static final String DATE_FORMAT = DateUtils.DATE_FORMAT_DD_MM_YYYY;

    @Bind(R.id.salutationSpinner) CustomMaterialSpinner salutationSpinner;
    @Bind(R.id.dobBtn) Button dobBtn;
    @Bind(R.id.firstNameEt) EditText firstNameEt;
    @Bind(R.id.lastNameEt) EditText lastNameEt;
    @Bind(R.id.titleNameTv) TextView title;
    @Bind(R.id.firstNameLabelTv) TextView firstNameLabel;
    @Bind(R.id.dobLabelTv) TextView dobLabelTv;
    @Bind(R.id.nextBtn) Button nextBtn;

    private Date dateOfBirth;

    public static NameFragment newInstance() {
        NameFragment fragment = new NameFragment();
        Bundle aboutBundle = new Bundle();
        fragment.setArguments(aboutBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lead_name, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firstNameEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        lastNameEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));

        //set salutation spinner
        String[] salutationType = getResources().getStringArray(R.array.salutation_array);
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, salutationType);
        salutationSpinner.setAdapter(accountAdapter);
        salutationSpinner.setText("Mr.");

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    String salutation = salutationSpinner.getText().toString();
                    String firstName = firstNameEt.getText().toString();
                    String lastName = lastNameEt.getText().toString();
                    HashMap<String, Object> leadHash = new HashMap<String, Object>();
                    leadHash.put(LeadActivity.salutation, salutation);
                    leadHash.put(LeadActivity.firstName, firstName);
                    leadHash.put(LeadActivity.lastName, lastName);
                    leadHash.put(LeadActivity.dateOfBirth, dateOfBirth);
                    ((LeadActivity) getActivity()).showFragment(BusinessFragment.newInstance(leadHash));
                }
            }
        });

        dobBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
    }

    private boolean validateFields() {
        String firstName = firstNameEt.getText().toString();
        String lastName = lastNameEt.getText().toString();

        if (TextUtils.isEmpty(firstName)) {
            UIHelper.showToast(getActivity(), "Please enter first name");
            firstNameEt.setError("Please enter first name");
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            UIHelper.showToast(getActivity(), "Please enter last name");
            lastNameEt.setError("Please enter last name");
            return false;
        }

        if (dateOfBirth == null) {
            UIHelper.showToast(getActivity(), "Please select date of birth");
            return false;
        }

        return true;
    }

    private void showDatePicker() {
        Calendar newCalendar = Calendar.getInstance();
        if (dateOfBirth != null) {
            newCalendar.setTime(dateOfBirth);
        }

        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, monthOfYear, dayOfMonth);
                dateOfBirth = DateUtils.toStartOfDay(c.getTime());
                dobBtn.setText(DateUtils.format(dateOfBirth, DATE_FORMAT));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}
