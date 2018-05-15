package com.smartpesa.smartpesa.fragment;

import com.smartpesa.smartpesa.R;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

public class MasterPassFragment extends BaseFragment {

    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_CURRENCY = "currency";
    String amount, currency;

    public MasterPassFragment() {
        // Required empty public constructor
    }

    public static MasterPassFragment newInstance(String amount, String currency) {
        MasterPassFragment fragment = new MasterPassFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putString(KEY_AMOUNT, amount);
        paymentBundle.putString(KEY_CURRENCY, currency);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        amount = getArguments().getString(KEY_AMOUNT);
        currency = getArguments().getString(KEY_CURRENCY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_masterpass, container, false);
        TextView amountTv = (TextView) view.findViewById(R.id.amountTv);
        ImageView qrIV = (ImageView) view.findViewById(R.id.qrIV);

        amountTv.setText("Pay " + currency + " " + amount);
        int[] res = {R.drawable.static_qr_code_without_logo, R.drawable.static_qr_code_without_logo1};
        Random rand = new Random();
        int rndInt = rand.nextInt(res.length);
        qrIV.setImageDrawable(getResources().getDrawable(res[rndInt]));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_qr_merchant_payment));
        }
    }

}