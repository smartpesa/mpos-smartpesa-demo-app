package com.smartpesa.smartpesa.activity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.flavour.MenuHandler;
import com.smartpesa.smartpesa.fragment.payment.AbstractPaymentFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.persistence.MerchantComponent;
import com.smartpesa.smartpesa.persistence.MerchantModule;
import com.smartpesa.smartpesa.util.ColorUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;
import smartpesa.sdk.models.operator.LogoutCallback;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    public static final String GO_TO_HISTORY = MainActivity.class.getName() + ".goToHistory";

    private static MainActivityComponent sMainActivityComponent;

    public Lazy<ServiceManager> serviceManager;

    List<IDrawerItem> drawerItems;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    Context mContext;
    Boolean doubleBackToExitPressedOnce = false;
    Drawer drawer;
    VerifiedMerchantInfo currentMerchant;
    MerchantComponent merchantComponent;

    @Nullable public MenuHandler mMenuHandler;

    @NonNull
    public static MainActivityComponent getComponent(MainActivity activity) {
        return sMainActivityComponent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        sMainActivityComponent = SmartPesaApplication.component(this).plus(new MainActivityModule(this));

        ButterKnife.bind(this);

        serviceManager = SmartPesaApplication.component(this).serviceManager();

        if (getMerchantComponent() != null) {
            currentMerchant = getMerchantComponent().provideMerchant();
        } else {
            logoutUser();
            return;
        }

        merchantComponent = getMerchantComponent();
        currentMerchant = getMerchantComponent().provideMerchant();
        drawerItems = getMerchantComponent().provideAllDrawerItems();


        mMenuHandler = SmartPesaApplication.flavourComponent(this).provideMenuHandler();

        //setup toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //set up the Navigation drawer
        setupDrawer();

        // display the first navigation drawer view on app launch
        if (savedInstanceState == null) {
            // on first time display view for first nav item
            List<IDrawerItem> items = merchantComponent.provideMainMenuItems();

            if (items.size() > 0) {
                displayView(items.get(0).getIdentifier());
            }
        }

    }

    //setup the navigation drawer
    private void setupDrawer() {
        //get the merchant and operator name's from the SDK
        String merchantName = currentMerchant.getMerchantName();
        String operatorName = currentMerchant.getOperatorName();

        //substring and obtain the Initial of the merchant
        String merchantTrim = merchantName.substring(0, 1);

        Drawable d = TextDrawable.builder()
                .beginConfig()
                .withBorder(10)
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(merchantTrim, ColorUtils.getColor(getResources(), R.color.color_primary, getTheme()));

        //create the merchant profile
        final IProfile profile = new ProfileDrawerItem().withName(merchantName).withEmail(operatorName).withIcon(d);

        //add the merchant profile to the header of navigation drawer along with the merchant initial
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.ic_drawer_header)
                .addProfiles(
                        profile
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        displayView(MerchantModule.MENU_ID_DUMMY_MERCHANT_INFO);
                        return false;
                    }
                })
                .withSelectionListEnabledForSingleProfile(false)
                .withHeightDp(160)
                .build();

        //create the navigation drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(headerResult)
                .withActionBarDrawerToggleAnimated(true)
                .withOnDrawerItemClickListener(new OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        displayView(drawerItem.getIdentifier());
                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View view) {

                    }

                    @Override
                    public void onDrawerClosed(View view) {
                    }

                    @Override
                    public void onDrawerSlide(View view, float v) {
                        changeSelectedDrawer();
                    }
                })
                .build();

        IDrawerItem[] items = new IDrawerItem[drawerItems.size()];
        drawerItems.toArray(items);
        drawer.addItems(items);

        changeSelectedDrawer();
    }

    //to display the fragments and change title of actionbar
    private void displayView(int identifier) {

        if (identifier != MerchantModule.MENU_ID_WECHAT) {
            if (identifier == MerchantModule.MENU_ID_LOGOUT) {
                showLogoutDialog();
            } else {
                if (mMenuHandler != null) {
                    Fragment f = mMenuHandler.fragmentForMenuWithId(identifier);
                    getComponent(this).provideNavigator().pushFragment(f);
                }
            }
        }

    }

    //show logout dialog
    private void showLogoutDialog() {
        if (null == MainActivity.this) return;

        final ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Logging out, please wait..");
        UIHelper.showMessageDialogWithCallback(this, getResources().getString(R.string.logout_prompt), getResources().getString(R.string.logout_yes), getResources().getString(R.string.logout_no), new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                if (null == MainActivity.this) return;

                Timber.i("User logs out of the app");
                mProgressDialog.show();
                //logout the merchant and show login screen
                serviceManager.get().logout(new LogoutCallback() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (null == MainActivity.this) return;

                        mProgressDialog.dismiss();
                        ((SmartPesaApplication) getApplication()).releaseMerchant();
                        startActivity(new Intent(mContext, SplashActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(SpException exception) {
                        if (null == MainActivity.this) return;

                        mProgressDialog.dismiss();
                        ((SmartPesaApplication) getApplication()).releaseMerchant();
                        startActivity(new Intent(mContext, SplashActivity.class));
                        finish();
                    }
                });

            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                changeSelectedDrawer();
            }
        });
    }

    //check if it is the PaymentFragment, if so show the double BACK to exit toast
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            if (doubleBackToExitPressedOnce) {
                finish();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.double_back), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

    //highlight selection based on the fragment displayed in the Navigation drawer
    private void changeSelectedDrawer() {

//        String title = mToolbar.getTitle().toString();
//        if (title.equals(getResources().getString(R.string.title_payment))) {
//            drawer.setSelection(MerchantModule.MENU_ID_SALE, false);
//        } else if (title.equals(getResources().getString(R.string.title_history))) {
//            drawer.setSelection(MerchantModule.MENU_ID_HISTORY, false);
//        } else if (title.equals(getResources().getString(R.string.title_refund))) {
//            drawer.setSelection(MerchantModule.MENU_ID_REFUND, false);
//        } else if (title.equals(getResources().getString(R.string.title_cashback))) {
//            drawer.setSelection(MerchantModule.MENU_ID_CASH_BACK, false);
//        } else if (title.equals(getResources().getString(R.string.title_inquiry))) {
//            drawer.setSelection(MerchantModule.MENU_ID_BALANCE_INQUIRY, false);
//        } else if (title.equals(getResources().getString(R.string.title_withdrawal))) {
//            drawer.setSelection(MerchantModule.MENU_ID_WITHDRAWAL, false);
//        } else if (title.equals(getResources().getString(R.string.title_transfer_funds))) {
//            drawer.setSelection(MerchantModule.MENU_ID_FUND_TRANSFER, false);
//        } else if (title.equals(getResources().getString(R.string.title_services))) {
//            drawer.setSelection(MerchantModule.MENU_ID_BILL_PAYMENT, false);
//        } else if (title.equals(getResources().getString(R.string.title_operators))) {
//            drawer.setSelection(MerchantModule.MENU_ID_OPERATORS, false);
//        } else if (title.equals(getResources().getString(R.string.title_settings))) {
//            drawer.setSelection(MerchantModule.MENU_ID_SETTINGS, false);
//        } else if (title.equals(getResources().getString(R.string.title_about))) {
//            drawer.setSelection(MerchantModule.MENU_ID_ABOUT, false);
//        } else if (title.equals(getResources().getString(R.string.title_logout))) {
//            drawer.setSelection(MerchantModule.MENU_ID_LOGOUT, false);
//        } else if (title.equals(getResources().getString(R.string.last_transaction))) {
//            drawer.setSelection(MerchantModule.MENU_ID_LAST_TRANSACTION, false);
//        } else if (title.equals(getResources().getString(R.string.statistics))) {
//            drawer.setSelection(MerchantModule.MENU_ID_STATISTICS, false);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AbstractPaymentFragment.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra(GO_TO_HISTORY, false)) {
                    displayView(MerchantModule.MENU_ID_HISTORY);
                }
            }
        }
    }
}
