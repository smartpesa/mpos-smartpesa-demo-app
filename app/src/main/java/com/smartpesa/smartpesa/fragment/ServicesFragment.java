package com.smartpesa.smartpesa.fragment;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.MainActivity;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.fragment.payment.BillPaymentFragment;
import com.smartpesa.smartpesa.helpers.CustomMaterialSpinner;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.billing.BillingOrganisation;
import smartpesa.sdk.models.billing.GetBillingOrganisationCallback;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

public class ServicesFragment extends BaseFragment implements View.OnClickListener {

    CustomMaterialSpinner categorySpinner, billSpinner;
    MaterialBetterSpinner accountTypeSpin;
    EditText subscriberNumberET;
    Context mContext;
    Lazy<ServiceManager> serviceManager;
    List<BillingOrganisation> billingOrganisations;
    List<String> category;
    List<String> bills;
    Button billContinueBTN;
    ProgressBar mProgressBar;
    int fromAccount, toAccount;
    UUID billingAccountNo;
    VerifiedMerchantInfo merchantInfo;

    public ServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }

        merchantInfo = getMerchantComponent(getActivity()).provideMerchant();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_service, container, false);

        initializeComponents(view);

        //perform call to SDK and fill in the details
        getDetails();
        //set the account type to default
        accountTypeSpin.setText(UIHelper.getAccountNameFromType(merchantInfo.getPlatformPermissions().getDefaultFromAccountType()));
        fromAccount = UIHelper.getAccountPositionFromEnum(merchantInfo.getPlatformPermissions().getDefaultFromAccountType());

        //handle when category is selected
        categorySpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!categorySpinner.getText().toString().equals(mContext.getResources().getString(R.string.service_please_wait))) {
                    String cat = category.get(position);

                    billSpinner.setText(mContext.getResources().getString(R.string.choose_billing_organisation));

                    bills = new ArrayList<String>();

                    for (int i = 0; i < billingOrganisations.size(); i++) {
                        if (billingOrganisations.get(i).getCategory().replace("_", " ").equals(cat)) {
                            bills.add(billingOrganisations.get(i).getName());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, bills);
                    billSpinner.setAdapter(adapter);
                }
            }
        });

        //handle when billing organisation is selected
        billSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!billSpinner.getText().toString().equals(mContext.getResources().getString(R.string.choose_category))) {

                    String billName = bills.get(position);

                    String description = "";

                    for (int i = 0; i < billingOrganisations.size(); i++) {
                        if (billingOrganisations.get(i).getName().equals(billName)) {
                            description = billingOrganisations.get(i).getDescription();
                            billingAccountNo = billingOrganisations.get(i).getBillingOrgId();
                        }
                    }
                    subscriberNumberET.setVisibility(View.VISIBLE);
                }

            }
        });

        accountTypeSpin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromAccount = position;
            }
        });

        return view;
    }

    //onClick listener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.billContinueBTN:

                //get the subscriber number
                String subsNumber = subscriberNumberET.getText().toString();

                //check if the number is empty
                if (TextUtils.isEmpty(subsNumber)) {
                    UIHelper.showToast(mContext, mContext.getResources().getString(R.string.enter_subscriber_number));
                    YoYo.with(Techniques.Shake)
                            .duration(1000)
                            .playOn(subscriberNumberET);
                } else if (categorySpinner.getText().toString().equals(mContext.getResources().getString(R.string.service_please_wait)) || TextUtils.isEmpty(categorySpinner.getText().toString())) {
                    UIHelper.showToast(mContext, mContext.getResources().getString(R.string.choose_category));
                    YoYo.with(Techniques.Shake)
                            .duration(1000)
                            .playOn(categorySpinner);
                } else if (billSpinner.getText().toString().equals(mContext.getResources().getString(R.string.choose_category_first)) || TextUtils.isEmpty(billSpinner.getText().toString()) || billSpinner.getText().toString().equals(mContext.getResources().getString(R.string.choose_billing_organisation))) {
                    UIHelper.showToast(mContext, mContext.getResources().getString(R.string.choose_billing_organisation));
                    YoYo.with(Techniques.Shake)
                            .duration(1000)
                            .playOn(billSpinner);
                } else {
                    if (getActivity() instanceof MainActivity) {
                        BillPaymentFragment billPaymentFragment = BillPaymentFragment.newInstance(SmartPesaTransactionType.PAYMENT, fromAccount, toAccount, subsNumber + "~" + billingAccountNo);
                        if (getActivity() instanceof MainActivity) {
                            MainActivity.getComponent((MainActivity) getActivity()).provideNavigator().pushFragment(billPaymentFragment);
                        }
                    }
                }

                break;
            default:
                break;
        }
    }

    //get the bill payment details from SDK
    private void getDetails() {

        //get the default account of the merchant from the SDK

        mProgressBar.setVisibility(View.VISIBLE);

        serviceManager.get().getBillingOrganisations(new GetBillingOrganisationCallback() {

            @Override
            public void onSuccess(List<BillingOrganisation> billingOrganisationses) {
                if (null == getActivity()) return;

                mProgressBar.setVisibility(View.GONE);

                billingOrganisations = billingOrganisationses;

                category = new ArrayList<String>();

                for (int i = 0; i < billingOrganisationses.size(); i++) {
                    if (!category.contains(billingOrganisationses.get(i).getCategory().replace("_", " "))) {
                        String categoryString = billingOrganisationses.get(i).getCategory();
                        categoryString = categoryString.replace("_", " ");
                        category.add(categoryString);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, category);

                categorySpinner.setAdapter(adapter);

                categorySpinner.setEnabled(true);
                billSpinner.setEnabled(true);
            }

            @Override
            public void onError(SpException exception) {
                if (null == getActivity()) return;

                if (exception instanceof SpSessionException) {
                    mProgressBar.setVisibility(View.GONE);

                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));

                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());

                }
            }
        });
    }

    //initialize the UI components
    private void initializeComponents(View view) {

        categorySpinner = (CustomMaterialSpinner) view.findViewById(R.id.categorySpin);
        billSpinner = (CustomMaterialSpinner) view.findViewById(R.id.billSpin);
        accountTypeSpin = (MaterialBetterSpinner) view.findViewById(R.id.billAccountTypeSpin);

        subscriberNumberET = (EditText) view.findViewById(R.id.subscriberNumberET);

        billContinueBTN = (Button) view.findViewById(R.id.billContinueBTN);
        billContinueBTN.setOnClickListener(this);

        mProgressBar = (ProgressBar) view.findViewById(R.id.billPaymentPB);

        //set up an empty adapter to the spinners
        String[] list = {mContext.getResources().getString(R.string.service_please_wait)};
        String[] billList = {mContext.getResources().getString(R.string.choose_category)};
        String[] accountType = getActivity().getResources().getStringArray(R.array.account_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, list);
        ArrayAdapter<String> billAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, billList);
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, accountType);
        accountTypeSpin.setAdapter(accountAdapter);
        categorySpinner.setAdapter(adapter);
        billSpinner.setAdapter(billAdapter);

        categorySpinner.setEnabled(false);
        billSpinner.setEnabled(false);

    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_services));
        }
    }
}
