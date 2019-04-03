package com.smartpesa.smartpesa.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.badoualy.stepperindicator.StepperIndicator;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.fragment.lead.AddressFragment;
import com.smartpesa.smartpesa.fragment.lead.BusinessFragment;
import com.smartpesa.smartpesa.fragment.lead.ContactFragment;
import com.smartpesa.smartpesa.fragment.lead.NameFragment;
import com.smartpesa.smartpesa.fragment.lead.RegisteredAddressFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;

import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.merchant.CaptureLeadCallback;
import smartpesa.sdk.models.merchant.Lead;
import timber.log.Timber;

public class LeadActivity extends AppCompatActivity {

    public static final String salutation = "salutation";
    public static final String firstName = "firstName";
    public static final String lastName = "lastName";
    public static final String dateOfBirth = "dateOfBirth";
    public static final String merchantName = "merchantName";
    public static final String description = "description";
    public static final String email = "email";
    public static final String mobilePhone = "mobilePhone";
    public static final String workPhone = "workPhone";
    public static final String website = "website";
    public static final String primaryCity = "primaryCity";
    public static final String primaryStreet = "primaryStreet";
    public static final String primaryState = "primaryState";
    public static final String primaryPostal = "primaryPostal";
    public static final String altCity = "altCity";
    public static final String altStreet = "altStreet";
    public static final String altState = "altState";
    public static final String altPostal = "altPostal";

    @BindView(R.id.indicator) StepperIndicator indicator;
    public Lazy<ServiceManager> serviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_lead);
        ButterKnife.bind(this);

        serviceManager = SmartPesaApplication.component(this).serviceManager();

        showFragment(NameFragment.newInstance());
    }

    public void showFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment, fragment.getClass().getName());
        fragmentTransaction.addToBackStack(null);
        try {
            fragmentTransaction.commitAllowingStateLoss();
            showIndictator();
        } catch (Exception e) {
            Timber.e(e, "Failed to commit fragment");
        }
    }

    private void showIndictator() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container_body);
        if (f instanceof NameFragment) {
            indicator.setCurrentStep(1);
        } else if (f instanceof BusinessFragment) {
            indicator.setCurrentStep(2);
        } else if (f instanceof ContactFragment) {
            indicator.setCurrentStep(3);
        } else if (f instanceof AddressFragment) {
            indicator.setCurrentStep(4);
        } else if (f instanceof RegisteredAddressFragment) {
            indicator.setCurrentStep(5);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
            showIndictatorReverse();
        }
    }

    private void showIndictatorReverse() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container_body);
        if (f instanceof NameFragment) {
            indicator.setCurrentStep(0);
        } else if (f instanceof BusinessFragment) {
            indicator.setCurrentStep(0);
        } else if (f instanceof ContactFragment) {
            indicator.setCurrentStep(1);
        } else if (f instanceof AddressFragment) {
            indicator.setCurrentStep(2);
        } else if (f instanceof RegisteredAddressFragment) {
            indicator.setCurrentStep(3);
        }
    }

    public void captureLead(HashMap<String, Object> leadHash) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Signing you up");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        String salutation = (String) leadHash.get(LeadActivity.salutation);
        String firstName = (String) leadHash.get(LeadActivity.firstName);
        String lastName = (String) leadHash.get(LeadActivity.lastName);
        String merchantName = (String) leadHash.get(LeadActivity.merchantName);
        String email = (String) leadHash.get(LeadActivity.email);
        String description = (String) leadHash.get(LeadActivity.description);
        String mobilePhone = (String) leadHash.get(LeadActivity.mobilePhone);
        String phoneWork = (String) leadHash.get(LeadActivity.workPhone);
        String primaryStreet = (String) leadHash.get(LeadActivity.primaryStreet);
        String primaryCity = (String) leadHash.get(LeadActivity.primaryCity);
        String primaryState = (String) leadHash.get(LeadActivity.primaryState);
        String primaryPostalCode = (String) leadHash.get(LeadActivity.primaryPostal);
        String alternateSteet = (String) leadHash.get(LeadActivity.altStreet);
        String alternateCity = (String) leadHash.get(LeadActivity.altCity);
        String alternateState = (String) leadHash.get(LeadActivity.altState);
        String alternatePostal = (String) leadHash.get(LeadActivity.altPostal);
        String website = (String) leadHash.get(LeadActivity.website);
        Date dob = (Date) leadHash.get(LeadActivity.dateOfBirth);

        Lead lead = new Lead.Builder().salutation(salutation)
                .firstName(firstName)
                .lastName(lastName)
                .merchantName(merchantName)
                .email(email)
                .birthDate(dob)
                .description(description)
                .phoneMobile(mobilePhone)
                .phoneWork(phoneWork)
                .primaryAddressStreet(primaryStreet)
                .primaryAddressCity(primaryCity)
                .primaryAddressState(primaryState)
                .primaryAddressPostalCode(primaryPostalCode)
                .altAddressStreet(alternateSteet)
                .altAddressCity(alternateCity)
                .altAddressState(alternateState)
                .altAddressPostalCode(alternatePostal)
                .website(website)
                .build();

        serviceManager.get().captureLead(lead, new CaptureLeadCallback() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                progressDialog.dismiss();
                UIHelper.showMessageDialogWithCallback(LeadActivity.this, "Your request for sign up is successful. You will be contacted shortly", "Ok", new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onAny(MaterialDialog dialog) {
                        super.onAny(dialog);
                        finish();
                    }
                });
            }

            @Override
            public void onError(SpException exception) {
                if (exception instanceof SpSessionException) {
                    progressDialog.dismiss();
                    //show the expired message
                    UIHelper.showToast(LeadActivity.this, getString(R.string.session_expired));

                    //finish the current activity
                    finish();

                    //start the splash screen
                    startActivity(new Intent(LeadActivity.this, SplashActivity.class));

                } else {
                    progressDialog.dismiss();
                    UIHelper.showMessageDialog(LeadActivity.this, exception.getMessage());
                }
            }
        });
    }
}
