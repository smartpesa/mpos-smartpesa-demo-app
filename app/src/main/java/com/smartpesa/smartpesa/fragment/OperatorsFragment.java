package com.smartpesa.smartpesa.fragment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.OperatorEditActivity;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.adapters.OperatorsAdapter;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.operator.GetOperatorCallback;
import smartpesa.sdk.models.operator.Operator;

public class OperatorsFragment extends BaseFragment {

    Lazy<ServiceManager> serviceManager;
    List<Operator> showAllList = new ArrayList<>();
    Context mContext;
    @Bind(R.id.operatorsLV) ListView operatorsLV;
    @Bind(R.id.operator_PB) ProgressBar progressBar;
    boolean isShowAll, enableEdit;
    public static Operator operatorInfo;
    private OperatorsAdapter mOperatorsAdapter;

    public OperatorsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_operators, container, false);
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mContext = getActivity();

        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();
        boolean canManage = getMerchantComponent(getActivity()).provideMerchant().getProfilePermissions().canManageOperators();

        operatorsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                operatorInfo = mOperatorsAdapter.getItem(position);
                mContext.startActivity(new Intent(mContext, OperatorEditActivity.class));
            }
        });

        if (canManage) {
            enableEdit = true;
        }
    }

    //get the operators list
    private void getOperators() {

        progressBar.setVisibility(View.VISIBLE);

        serviceManager.get().getOperators(new GetOperatorCallback() {
            @Override
            public void onSuccess(List<Operator> operators) {
                if (null == getActivity()) return;

                progressBar.setVisibility(View.GONE);
                showAllList = operators;

                mOperatorsAdapter = new OperatorsAdapter(mContext, R.layout.row_operator, showAllList, enableEdit);
                operatorsLV.setAdapter(mOperatorsAdapter);
                performFilter();
            }

            @Override
            public void onError(SpException exception) {
                if (null == getActivity()) return;

                if (exception instanceof SpSessionException) {
                    progressBar.setVisibility(View.GONE);
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    progressBar.setVisibility(View.GONE);
                    UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());
                }
            }
        });
    }

    //menu button
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_operators, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.operatorCB:
                if (!isShowAll) {
                    item.setTitle("Only Active");
                    isShowAll = true;
                } else {
                    item.setTitle("Show All");
                    isShowAll = false;
                }
                performFilter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void performFilter() {
        mOperatorsAdapter.getFilter().filter(isShowAll ? null : "");
    }

    //to set the Title of fragment
    @Override
    public void onResume() {
        super.onResume();

        getOperators();

        // Update the action bar title with the TypefaceSpan instance
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_operators);
        }

    }
}
