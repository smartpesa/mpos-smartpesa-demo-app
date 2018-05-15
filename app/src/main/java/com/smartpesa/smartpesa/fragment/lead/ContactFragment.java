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
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ContactFragment extends Fragment {

    public static final String HASHMAP = "hashmap";
    @Bind(R.id.emailEt) EditText emailEt;
    @Bind(R.id.mobilePhoneEt) EditText mobilePhoneEt;
    @Bind(R.id.workPhoneEt) EditText workPhoneEt;
    @Bind(R.id.websiteEt) EditText websiteEt;
    @Bind(R.id.emailLabelTv) TextView emailLabelTv;
    @Bind(R.id.mobileNumberLabelTv) TextView mobileNumberLabelTv;
    @Bind(R.id.workPhoneLabelTv) TextView workPhoneLabelTv;
    @Bind(R.id.websiteLabeltv) TextView websiteLabeltv;
    @Bind(R.id.titleNameTv) TextView title;
    @Bind(R.id.nextBtn) Button nextBtn;

    UIHelper font;
    HashMap<String, Object> leadHash;

    public static ContactFragment newInstance(HashMap<String, Object> leadHash) {
        ContactFragment fragment = new ContactFragment();
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
        View view = inflater.inflate(R.layout.fragment_lead_contact, container, false);
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
        mobileNumberLabelTv.setTypeface(font.boldFont);
        emailLabelTv.setTypeface(font.boldFont);
        workPhoneLabelTv.setTypeface(font.boldFont);
        websiteLabeltv.setTypeface(font.boldFont);
        mobilePhoneEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        emailEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        workPhoneEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        websiteEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    String email = emailEt.getText().toString();
                    String mobilePhone = mobilePhoneEt.getText().toString();
                    String phoneWork = workPhoneEt.getText().toString();
                    String website = websiteEt.getText().toString();
                    leadHash.put(LeadActivity.email, email);
                    leadHash.put(LeadActivity.mobilePhone, mobilePhone);
                    leadHash.put(LeadActivity.workPhone, phoneWork);
                    leadHash.put(LeadActivity.website, website);
                    ((LeadActivity) getActivity()).showFragment(AddressFragment.newInstance(leadHash));
                }
            }
        });
    }

    private boolean validateFields() {
        String email = emailEt.getText().toString();
        String mobilePhone = mobilePhoneEt.getText().toString();
        String phoneWork = workPhoneEt.getText().toString();
        String website = websiteEt.getText().toString();

        if (TextUtils.isEmpty(email)) {
            UIHelper.showToast(getActivity(), "Please enter email address");
            emailEt.setError("Please enter email address");
            return false;
        }

        if (TextUtils.isEmpty(mobilePhone)) {
            UIHelper.showToast(getActivity(), "Please enter mobile phone number");
            mobilePhoneEt.setError("Please enter mobile phone number");
            return false;
        }

        if (TextUtils.isEmpty(phoneWork)) {
            UIHelper.showToast(getActivity(), "Please enter work phone number");
            workPhoneEt.setError("Please enter work phone number");
            return false;
        }

        if (TextUtils.isEmpty(website)) {
            UIHelper.showToast(getActivity(), "Please enter website");
            websiteEt.setError("Please enter website");
            return false;
        }

        return true;
    }
}
