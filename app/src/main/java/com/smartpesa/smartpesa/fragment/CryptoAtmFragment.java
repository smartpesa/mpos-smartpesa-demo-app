package com.smartpesa.smartpesa.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.adapters.MenuAdapter;
import com.smartpesa.smartpesa.helpers.MenuItem;
import com.smartpesa.smartpesa.persistence.MerchantModule;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CryptoAtmFragment extends BaseFragment {

    @BindView(R.id.grid)
    GridView gridView;

    public CryptoAtmFragment() {
        // Required empty public constructor
    }

    //set up the interface for changing the payment fragment
    public interface PaymentTypeListener     {
        public void processMenu(int menuId);
    }

    public MenuFragment.PaymentTypeListener mListener;

    //attach the listener to the activity
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (MenuFragment.PaymentTypeListener)getActivity();
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
        View view = inflater.inflate(R.layout.fragment_crypto_atm, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setUpCrypto();
    }

    private void setUpCrypto() {
        final ArrayList<MenuItem> menuItems = new ArrayList<>();

        menuItems.add(new MenuItem(MerchantModule.MENU_ID_CRYPTO_ATM_BITCOIN, R.drawable.ic_crypto_menu, R.string.menu_bitcoin));
        menuItems.add(new MenuItem(MerchantModule.MENU_ID_CRYPTO_ATM_LITECOIN, R.drawable.ic_litecoin, R.string.menu_litecoin));

        MenuAdapter adapter = new MenuAdapter(getActivity(), menuItems);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mListener.processMenu(menuItems.get(position).id);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_crypto_atm));
        }
    }
}