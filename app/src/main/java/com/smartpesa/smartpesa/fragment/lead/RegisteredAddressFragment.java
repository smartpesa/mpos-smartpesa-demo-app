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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RegisteredAddressFragment extends Fragment {

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
    @Bind(R.id.nextBtn) Button nextBtn;
    @Bind(R.id.addressCb) CheckBox addressCb;

    UIHelper font;
    HashMap<String, Object> leadHash;

    public static RegisteredAddressFragment newInstance(HashMap<String, Object> leadHash) {
        RegisteredAddressFragment fragment = new RegisteredAddressFragment();
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
        View view = inflater.inflate(R.layout.fragment_registered_address, container, false);
        ButterKnife.bind(this, view);
        font = new UIHelper(getActivity());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle b = this.getArguments();
        if (b.getSerializable(HASHMAP) != null) {
            leadHash = (HashMap<String, Object>) b.getSerializable(HASHMAP);
        }

        title.setTypeface(font.boldFont);
        primaryCityLabelTv.setTypeface(font.boldFont);
        primaryStreetLabelTv.setTypeface(font.boldFont);
        primaryStateLabelTv.setTypeface(font.boldFont);
        primaryPostalCodeLabelEt.setTypeface(font.boldFont);
        primaryCityEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        primaryStreetEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        primaryStateEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        primaryPostalCodeEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));

        addressCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    String primaryStreet = (String) leadHash.get(LeadActivity.primaryStreet);
                    String primaryCity = (String) leadHash.get(LeadActivity.primaryCity);
                    String primaryState = (String) leadHash.get(LeadActivity.primaryState);
                    String primaryPostalCode = (String) leadHash.get(LeadActivity.primaryPostal);
                    primaryStreetEt.setText(primaryStreet);
                    primaryCityEt.setText(primaryCity);
                    primaryStateEt.setText(primaryState);
                    primaryPostalCodeEt.setText(primaryPostalCode);
                } else {
                    primaryStreetEt.setText("");
                    primaryCityEt.setText("");
                    primaryStateEt.setText("");
                    primaryPostalCodeEt.setText("");
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    String primaryStreet = primaryStreetEt.getText().toString();
                    String primaryCity = primaryCityEt.getText().toString();
                    String primaryState = primaryStateEt.getText().toString();
                    String primaryPostalCode = primaryPostalCodeEt.getText().toString();
                    leadHash.put(LeadActivity.altStreet, primaryStreet);
                    leadHash.put(LeadActivity.altCity, primaryCity);
                    leadHash.put(LeadActivity.altState, primaryState);
                    leadHash.put(LeadActivity.altPostal, primaryPostalCode);

                    //do the lead capture here
                    ((LeadActivity) getActivity()).captureLead(leadHash);
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
