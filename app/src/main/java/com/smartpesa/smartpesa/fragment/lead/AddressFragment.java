package com.smartpesa.smartpesa.fragment.lead;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.LeadActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddressFragment extends Fragment {

    public static final String HASHMAP = "hashmap";

    @Bind(R.id.primaryStreetEt) EditText primaryStreetEt;
    @Bind(R.id.primaryCityEt) EditText primaryCityEt;
    @Bind(R.id.primaryStateEt) EditText primaryStateEt;
    @Bind(R.id.primaryPostalCodeEt) EditText primaryPostalCodeEt;
    @Bind(R.id.primaryStreetLabelTv) TextView primaryStreetLabelTv;
    @Bind(R.id.primaryCityLabelTv) TextView primaryCityLabelTv;
    @Bind(R.id.primaryStateLabelTv) TextView primaryStateLabelTv;
    @Bind(R.id.primaryPostalCodeLabelEt) TextView primaryPostalCodeLabelEt;
    @Bind(R.id.titleNameTv) TextView title;
    @Bind(R.id.addressCb) CheckBox addressCb;
    @Bind(R.id.nextBtn) Button nextBtn;

    HashMap<String, Object> leadHash;

    public static AddressFragment newInstance(HashMap<String, Object> leadHash) {
        AddressFragment fragment = new AddressFragment();
        Bundle aboutBundle = new Bundle();
        aboutBundle.putSerializable(HASHMAP, leadHash);
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
        View view = inflater.inflate(R.layout.fragment_lead_address, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle b = this.getArguments();
        if (b.getSerializable(HASHMAP) != null) {
            leadHash = (HashMap<String, Object>) b.getSerializable(HASHMAP);
        }

        primaryCityEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        primaryStreetEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        primaryStateEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        primaryPostalCodeEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    String primaryStreet = primaryStreetEt.getText().toString();
                    String primaryCity = primaryCityEt.getText().toString();
                    String primaryState = primaryStateEt.getText().toString();
                    String primaryPostalCode = primaryPostalCodeEt.getText().toString();
                    leadHash.put(LeadActivity.primaryStreet, primaryStreet);
                    leadHash.put(LeadActivity.primaryCity, primaryCity);
                    leadHash.put(LeadActivity.primaryState, primaryState);
                    leadHash.put(LeadActivity.primaryPostal, primaryPostalCode);

                    if (addressCb.isChecked()) {
                        //set alt address as the same
                        leadHash.put(LeadActivity.altStreet, primaryStreet);
                        leadHash.put(LeadActivity.altCity, primaryCity);
                        leadHash.put(LeadActivity.altState, primaryState);
                        leadHash.put(LeadActivity.altPostal, primaryPostalCode);
                        ((LeadActivity) getActivity()).captureLead(leadHash);
                    } else {
                        ((LeadActivity) getActivity()).showFragment(RegisteredAddressFragment.newInstance(leadHash));
                    }
                }
            }
        });
    }

    private boolean validateFields() {

        String primaryStreet = primaryStreetEt.getText().toString();
        String primaryCity = primaryCityEt.getText().toString();
        String primaryState = primaryStateEt.getText().toString();
        String primaryPostalCode = primaryPostalCodeEt.getText().toString();

        if (TextUtils.isEmpty(primaryStreet)) {
            UIHelper.showToast(getActivity(), "Please enter street address");
            primaryStreetEt.setError("Please enter street address");
            return false;
        }

        if (TextUtils.isEmpty(primaryCity)) {
            UIHelper.showToast(getActivity(), "Please enter city");
            primaryCityEt.setError("Please enter city");
            return false;
        }

        if (TextUtils.isEmpty(primaryState)) {
            UIHelper.showToast(getActivity(), "Please enter state");
            primaryStateEt.setError("Please enter state");
            return false;
        }

        if (TextUtils.isEmpty(primaryPostalCode)) {
            UIHelper.showToast(getActivity(), "Please enter postal code");
            primaryPostalCodeEt.setError("Please enter postal code");
            return false;
        }

        return true;
    }
}
