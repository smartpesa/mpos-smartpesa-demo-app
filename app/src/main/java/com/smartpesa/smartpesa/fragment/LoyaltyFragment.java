package com.smartpesa.smartpesa.fragment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.adapters.LoyaltyAdapter;
import com.smartpesa.smartpesa.models.UIRedeemable;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoyaltyFragment extends BaseFragment {

    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
    @Bind(R.id.loyaltyBtn) Button loyaltyBtn;
    @Bind(R.id.loyaltyTitle) TextView loyaltyTitle;

    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    ArrayList<UIRedeemable> mRedeemableArrayList;
    boolean isInquiry;

    public static LoyaltyFragment newInstance(ArrayList<UIRedeemable> redeemableArrayList, boolean isInquiry) {
        LoyaltyFragment fragment = new LoyaltyFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putParcelableArrayList("loyalty", redeemableArrayList);
        paymentBundle.putBoolean("isInquiry", isInquiry);
        fragment.setArguments(paymentBundle);
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_loyalty, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mRedeemableArrayList = getArguments().getParcelableArrayList("loyalty");
        isInquiry = getArguments().getBoolean("isInquiry");

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new LoyaltyAdapter(mRedeemableArrayList, getActivity(), isInquiry);
        mRecyclerView.setAdapter(mAdapter);

        //set title
        if (isInquiry) {
            loyaltyBtn.setText("DONE");
            loyaltyTitle.setText("Loyalty Inquiry\nSuccessful");
        }

        loyaltyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }
}
