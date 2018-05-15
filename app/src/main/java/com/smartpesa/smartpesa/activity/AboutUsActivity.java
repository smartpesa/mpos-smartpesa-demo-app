package com.smartpesa.smartpesa.activity;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.fragment.AboutFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AboutUsActivity extends BaseActivity {

    private static final String BUNDLE_KEY_UPDATE_MANDATORY = AboutUsActivity.class.getName() + ".updateMandatory";

    @Bind(R.id.toolbar) Toolbar toolbar;

    Boolean mandatoryUpdate;

    public static void putUpdateMandatory(Intent intent, boolean updateMandatory) {
        intent.putExtra(BUNDLE_KEY_UPDATE_MANDATORY, updateMandatory);
    }

    public static boolean hasUpdateMandatory(Intent intent) {
        return intent.hasExtra(BUNDLE_KEY_UPDATE_MANDATORY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        //initialize the UI components
        initializeComponents();

        // Put about us fragment into the activity
        Fragment fragment = AboutFragment.newInstance(mandatoryUpdate);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
    }

    //initialize the UI components
    public void initializeComponents() {

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_activity_about_us);

        if (hasUpdateMandatory(getIntent())) {
            mandatoryUpdate = true;
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            mandatoryUpdate = false;
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mandatoryUpdate) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
