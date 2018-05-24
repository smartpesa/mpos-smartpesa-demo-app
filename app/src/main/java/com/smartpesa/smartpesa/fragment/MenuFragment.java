package com.smartpesa.smartpesa.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.adapters.MenuAdapter;
import com.smartpesa.smartpesa.helpers.MenuItem;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.persistence.MerchantModule;

import java.util.ArrayList;
import java.util.List;

import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.operator.LogoutCallback;

public class MenuFragment extends BaseFragment {

    public static final String KEY_MENU_TYPE = "menuType";
    public static final int TRANSACTIONS = 1;
    public static final int INFORMATION = 2;
    Lazy<ServiceManager> serviceManager;
    Context mContext;
    int menuType;
    GridView grid;

    public static MenuFragment newInstance(int menuType) {
        MenuFragment fragment = new MenuFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putInt(KEY_MENU_TYPE, menuType);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    //set up the interface for changing the payment fragment
    public interface PaymentTypeListener     {
        public void processMenu(int menuId);
    }

    public PaymentTypeListener mListener;

    //attach the listener to the activity
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (PaymentTypeListener)getActivity();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();
        mContext = getActivity();
        menuType = getArguments().getInt(KEY_MENU_TYPE);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }

        grid = (GridView) view.findViewById(R.id.grid);
        if (menuType == TRANSACTIONS) {
            setTransactionMenu(view);
        } else if (menuType == INFORMATION) {
            setUpInquiryMenu(view);
        }
    }



    private void setTransactionMenu(View view) {
        final List<MenuItem> menuItems = getMerchantComponent(getActivity()).getMenuItems();
        if (menuItems.size() == 0) {
            UIHelper.showMessageDialogWithCallback(getActivity(), "You have no transactions enabled, please contact your service provider.",
                    "Logout", new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onAny(MaterialDialog dialog) {
                            super.onAny(dialog);
                            logout();
                        }
                    });
        } else {
            MenuAdapter adapter = new MenuAdapter(getActivity(), menuItems);
            grid.setAdapter(adapter);
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    mListener.processMenu(menuItems.get(position).id);
                }
            });
        }
    }

    private void logout() {
        final ProgressDialog mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Logging out, please wait..");
        mProgressDialog.show();

        //logout the merchant and show login screen
        serviceManager.get().logout(new LogoutCallback() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (null == getActivity()) return;
                mProgressDialog.dismiss();
                ((SmartPesaApplication) getActivity().getApplication()).releaseMerchant();
                getActivity().finish();
                startActivity(new Intent(mContext, SplashActivity.class));
            }

            @Override
            public void onError(SpException e) {
                if (null == getActivity()) return;
                mProgressDialog.dismiss();
                UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), e.getMessage());
            }
        });
    }

    private void setUpInquiryMenu(View view) {
        final ArrayList<MenuItem> menuItems = new ArrayList<>();

        menuItems.add(new MenuItem(MerchantModule.MENU_ID_LAST_TRANSACTION, R.drawable.ic_history_black_24dp, R.string.last_transaction));
        menuItems.add(new MenuItem(MerchantModule.MENU_ID_HISTORY, R.drawable.ic_history, R.string.history));
        menuItems.add(new MenuItem(MerchantModule.MENU_ID_STATISTICS, R.drawable.ic_statistics, R.string.statistics));
        menuItems.add(new MenuItem(MerchantModule.MENU_ID_DUMMY_MERCHANT_INFO, R.drawable.ic_merchant, R.string.merchant_info));
        menuItems.add(new MenuItem(MerchantModule.MENU_ID_OPERATORS, R.drawable.ic_person_black_24dp, R.string.title_operators));
        menuItems.add(new MenuItem(MerchantModule.MENU_ID_SETTINGS, R.drawable.ic_devices, R.string.title_settings));
        menuItems.add(new MenuItem(MerchantModule.MENU_ID_ABOUT, R.drawable.ic_chat_bubble_outline_black_24dp, R.string.title_about));
        menuItems.add(new MenuItem(MerchantModule.MENU_ID_LOGOUT, R.drawable.ic_exit_to_app_black_24dp, R.string.title_logout));

        MenuAdapter adapter = new MenuAdapter(getActivity(), menuItems);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_menu));
        }
    }

    @Override
    protected boolean isShowHomeAsUp() {
        return false;
    }
}
