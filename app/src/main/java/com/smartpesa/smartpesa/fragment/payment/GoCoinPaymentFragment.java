package com.smartpesa.smartpesa.fragment.payment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.payment.GoCoinPaymentProgressActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.currency.Currency;
import smartpesa.sdk.models.currency.GetAvailableCurrenciesCallback;

public class GoCoinPaymentFragment extends AbstractPaymentFragment {

    private static final int GOCOIN_DISPLAY = 1;

    @Bind(R.id.currencyRl) RelativeLayout currencyRl;
    @Bind(R.id.currencySpinner) Spinner currencySpinner;
    @Bind(R.id.cashBackRL) RelativeLayout cashBackRl;

    List<Currency> currencyList;
    List<String> currencyName;
    Currency currencyToPass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static GoCoinPaymentFragment newInstance(SmartPesaTransactionType transactionType, int fromAccount, int toAccount) {
        GoCoinPaymentFragment fragment = new GoCoinPaymentFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putInt(BUNDLE_KEY_TRANSACTION_TYPE, transactionType.getEnumId());
        paymentBundle.putInt(BUNDLE_KEY_FROM_ACCOUNT, fromAccount);
        paymentBundle.putInt(BUNDLE_KEY_TO_ACCOUNT, toAccount);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currencyRl.setVisibility(View.VISIBLE);
        cashBackRl.setVisibility(View.INVISIBLE);

        continueBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currencyToPass != null) {
                    proceedToPayment(GOCOIN_DISPLAY);
                } else {
                    if (getActivity() == null) return;
                    UIHelper.showMessageDialog(getActivity(), "Please select a Cryto to continue");
                }
            }
        });

        currencyList = new ArrayList<>();
        currencyName = new ArrayList<>();
        getCurrency();

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currencyToPass = currencyList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getCurrency() {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage("Getting available crypto...");
        dialog.show();

        serviceManager.get().getAvailableCurrencies(new GetAvailableCurrenciesCallback() {
            @Override
            public void onSuccess(List<Currency> currencies) {
                if (getActivity() == null) return;

                dialog.dismiss();

                if (currencies != null && currencies.size() > 0) {
                    for (int i = 0; i < currencies.size(); i++) {

                        Currency currentCurrency = currencies.get(i);

                        if (currentCurrency != null) {

                            if (currentCurrency.getCurrencyType().equals(Currency.Type.CRYPTO)) {
                                currencyList.add(currentCurrency);
                                currencyName.add(currentCurrency.getCurrencyName());
                            }

                        } else {
                            showNoCurrencyMessage();
                        }
                    }

                    if (currencyName != null && currencyName.size() > 0) {
                        ArrayAdapter<String> accountAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, currencyName);
                        currencySpinner.setAdapter(accountAdapter);
                    } else {
                        showNoCurrencyMessage();
                    }
                } else {
                    showNoCurrencyMessage();
                }
            }

            @Override
            public void onError(SpException e) {
                if (getActivity() == null) return;

                dialog.dismiss();

                UIHelper.showMessageDialog(getActivity(), e.getMessage());
            }
        });
    }

    private void showNoCurrencyMessage() {
        UIHelper.showMessageDialog(getActivity(), "No crypto available");
    }

    private void proceedToPayment(int alipayType) {
        String value = amountPaymentET.getText().toString();

        double cashBackAmount = getCashbackAmount();
        double amount = mMoneyUtils.parseBigDecimal(value).doubleValue();
        //check if amount is 0.00
        if (amount != 0.00) {
            //check for internet connectivity
            if (UIHelper.isOnline(mContext)) {

                Bundle paymentBundle = new Bundle();
                paymentBundle.putDouble("amount", amount);
                paymentBundle.putDouble("cashBackAmount", cashBackAmount);
                paymentBundle.putInt("transactionType", SmartPesaTransactionType.SALE.getEnumId());
                paymentBundle.putInt("fromAccount", mFromAccount);
                paymentBundle.putInt("toAccount", mToAccount);

                if (currencyToPass != null) {
                    paymentBundle.putSerializable("currency", currencyToPass);
                }

                onBuildPaymentDescription(paymentBundle);

                if (alipayType == GOCOIN_DISPLAY) {

                    paymentBundle.putBoolean("isScan", false);
                    Intent paymentIntent = new Intent(getActivity(), GoCoinPaymentProgressActivity.class);
                    paymentIntent.putExtras(paymentBundle);
                    startActivity(paymentIntent);

                }

            } else {
                UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), getResources().getString(R.string.internet_not_connected));
            }
        } else {
            UIHelper.showToast(mContext, mContext.getResources().getString(R.string.please_enter_amount));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_crypto);
        }
    }
}
