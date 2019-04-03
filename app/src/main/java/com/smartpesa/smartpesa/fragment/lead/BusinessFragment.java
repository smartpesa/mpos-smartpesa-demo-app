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

import butterknife.BindView;
import butterknife.ButterKnife;

public class BusinessFragment extends Fragment {

    public static final String HASHMAP = "hashmap";
    @BindView(R.id.merchantNameEt) EditText merchantNameEt;
    @BindView(R.id.descriptionEt) EditText descriptionEt;
    @BindView(R.id.merchantNameLabelTv) TextView merchantNameLabelTv;
    @BindView(R.id.descriptionLabelTv) TextView descriptionLabelTv;
    @BindView(R.id.nextBtn) Button nextBtn;
    @BindView(R.id.titleNameTv) TextView title;

    UIHelper font;
    HashMap<String, Object> leadHash;

    public static BusinessFragment newInstance(HashMap<String, Object> leadHash) {
        BusinessFragment fragment = new BusinessFragment();
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
        View view = inflater.inflate(R.layout.fragment_lead_business, container, false);
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
        merchantNameLabelTv.setTypeface(font.boldFont);
        descriptionLabelTv.setTypeface(font.boldFont);
        merchantNameEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));
        descriptionEt.setHintTextColor(getResources().getColor(R.color.md_grey_300));

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    String merchantName = merchantNameEt.getText().toString();
                    String description = descriptionEt.getText().toString();
                    leadHash.put(LeadActivity.merchantName, merchantName);
                    leadHash.put(LeadActivity.description, description);
                    ((LeadActivity) getActivity()).showFragment(ContactFragment.newInstance(leadHash));
                }
            }
        });
    }

    private boolean validateFields() {
        String merchantName = merchantNameEt.getText().toString();
        String description = descriptionEt.getText().toString();

        if (TextUtils.isEmpty(merchantName)) {
            UIHelper.showToast(getActivity(), "Please enter merchant name");
            merchantNameEt.setError("Please enter merchant name");
            return false;
        }

        if (TextUtils.isEmpty(description)) {
            UIHelper.showToast(getActivity(), "Please enter description");
            descriptionEt.setError("Please enter description");
            return false;
        }
        return true;
    }
}
