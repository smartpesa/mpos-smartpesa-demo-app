package com.smartpesa.smartpesa.activity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.smartpesa.smartpesa.BuildConfig;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.helpers.PreferenceHelper;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.util.constants.PreferenceConstants;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpVersionException;
import smartpesa.sdk.models.version.GetVersionCallback;
import smartpesa.sdk.models.version.Version;

@SuppressWarnings("MissingPermission")
public class SplashActivity extends BaseActivity {

    private static final int REQUEST_PERMISSION = 232;
    public final int UPDATE_MANDATORY = 111;
    public final int UPDATE_OPTIONAL = 122;
    public final int UPDATE_NONE = 133;

    @Inject
    PreferenceHelper mPreferenceHelper;
    @Inject
    Lazy<ServiceManager> serviceManager;

    @Bind(R.id.splash_pb)
    ProgressBar splashPB;

    Context mContext;
    Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);
        SmartPesaApplication.component(this).inject(this);

        mContext = SplashActivity.this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if internet is connected
        if (UIHelper.isOnline(mContext)) {
            //check the UI versioning
            checkMerchantCache();
        } else {
            if (isActivityDestroyed()) return;
            UIHelper.showMessageDialogWithCallback(mContext, getResources().getString(R.string.internet_not_connected), getResources().getString(R.string.ok), new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    finish();
                }
            });
        }
    }

    //UI checkMerchantCache logic
    private void getUIVersion() {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                URL smartPesa = null;
                String inputLine = "";
                try {
                    smartPesa = new URL(SmartPesaApplication.component(SplashActivity.this).versionEndpoint());
                    UIHelper.log(smartPesa.toString());
                    BufferedReader in = new BufferedReader(new InputStreamReader(smartPesa.openStream()));
                    inputLine = in.readLine();
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    showServerError();
                } catch (IOException e) {
                    e.printStackTrace();
                    showServerError();
                }
                return inputLine;
            }

            @Override
            protected void onPostExecute(final String versionDetails) {
                super.onPostExecute(versionDetails);
                if (isActivityDestroyed()) return;
                if (!TextUtils.isEmpty(versionDetails)) {
                    UIHelper.log(versionDetails);
                    final String[] parts = versionDetails.split(",");
                    if (parts != null) {
                        if (parts.length == 4) {
                            String message = parts[3];
                            if (isActivityDestroyed()) return;
                            UIHelper.showMessageDialogWithCallback(SplashActivity.this, message, "Ok", new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onAny(MaterialDialog dialog) {
                                    super.onAny(dialog);
                                    doVersionTxtVersionCheck(parts);
                                }
                            });
                        } else if (parts.length == 3) {
                            doVersionTxtVersionCheck(parts);
                        }
                    } else {
                        showServerError();
                    }
                } else {
                    showServerError();
                }
            }
        }.execute();
    }

    private void doVersionTxtVersionCheck(String[] parts) {
        if (isActivityDestroyed()) return;
        String version = parts[0];
        final String url = parts[1];
        mPreferenceHelper.putString(PreferenceConstants.KEY_PLAY_STORE_URL, url);
        if (!version.equals("")) {
            if (needsUpdate(version) == UPDATE_MANDATORY) {
                if (isActivityDestroyed()) return;
                showUpdateMandatory(version, url);
                mPreferenceHelper.putBoolean(PreferenceConstants.KEY_IS_OPTIONAL, false);
            } else if (needsUpdate(version) == UPDATE_OPTIONAL) {
                if (isActivityDestroyed()) return;
                showUpdateOptional(version, url);
                saveOptionUpdateInPreference(version, url);
            } else if (needsUpdate(version) == UPDATE_NONE) {
                if (isActivityDestroyed()) return;
                mPreferenceHelper.putBoolean(PreferenceConstants.KEY_IS_OPTIONAL, false);
                getVersion();
            }
        } else {
            showServerError();
        }
    }

    private void saveOptionUpdateInPreference(String version, String url) {
        mPreferenceHelper.putString(PreferenceConstants.KEY_SERVER_APK_URL, url);
        mPreferenceHelper.putString(PreferenceConstants.KEY_APP_VERSION, version);
        mPreferenceHelper.putBoolean(PreferenceConstants.KEY_IS_OPTIONAL, true);
    }

    private void showUpdateOptional(final String version, final String url) {
        if (isActivityDestroyed()) return;

        UIHelper.showMessageDialogWithTitleTwoButtonCallback(mContext, getResources().getString(R.string.optional_upgrade), "A new version of " + getString(R.string.app_name) + " v" + version + " is available.\n"
                + getString(R.string.optional_update_app), getResources().getString(R.string.update), getResources().getString(R.string.continue_without_update), new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                finish();
                openPlayStore(url);
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onPositive(dialog);
                getVersion();
            }
        });
    }

    private void showUpdateMandatory(final String version, final String url) {
        if (isActivityDestroyed()) return;

        UIHelper.showMessageDialogWithTitleTwoButtonCallback(mContext, getResources().getString(R.string.mandatory_update), "A new version of " + getString(R.string.app_name) + " v" + version + " is available.\n"
                + getString(R.string.update_app), getResources().getString(R.string.update), getResources().getString(R.string.exit), new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                finish();
                openPlayStore(url);
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onPositive(dialog);
                finish();
            }
        });
    }

    private void openPlayStore(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void showServerError() {
        if (isActivityDestroyed()) return;
        runOnUiThread(new Runnable() {
            public void run() {
                if (isActivityDestroyed()) return;

                UIHelper.showMessageDialogWithCallback(mContext, getResources().getString(R.string.server_error), getResources().getString(R.string.ok), new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        finish();
                    }
                });
            }
        });

    }

    private int needsUpdate(String serverVersion) {
        String appVersion = BuildConfig.VERSION_NAME;
        String[] appParts = appVersion.split("\\.");
        String[] serverParts = serverVersion.split("\\.");

        int localMajor = Integer.parseInt(appParts[0]);
        int serverMajor = Integer.parseInt(serverParts[0]);
        int localMinor = Integer.parseInt(appParts[1]);
        int serverMinor = Integer.parseInt(serverParts[1]);
        int localPatch = Integer.parseInt(appParts[2]);
        int serverPatch = Integer.parseInt(serverParts[2]);

        if (localMajor < serverMajor) {
            return UPDATE_MANDATORY;
        } else if (localMajor == serverMajor) {
            if (localMinor < serverMinor) {
                return UPDATE_MANDATORY;
            } else if (localMinor == serverMinor) {
                if (localPatch < serverPatch) {
                    return UPDATE_OPTIONAL;
                } else {
                    return UPDATE_NONE;
                }
            } else {
                return UPDATE_NONE;
            }
        } else {
            return UPDATE_NONE;
        }
    }

    public void initMerchantModule() {
        ((SmartPesaApplication) getApplication()).createMerchantComponent();
    }

    public void releaseMerchantModule() {
        ((SmartPesaApplication) getApplication()).releaseMerchant();
    }

    //SDK checkMerchantCache
    public void checkMerchantCache() {

        splashPB.setVisibility(View.VISIBLE);

        if (getMerchantComponent() != null && getMerchantComponent().provideMerchant() != null) {
            initMerchantModule();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isActivityDestroyed()) return;
                    startMainActivity();
                }
            }, 1000);

        } else {
            getUIVersion();
        }
    }

    private void getVersion() {
        releaseMerchantModule();
        serviceManager.get().getVersion(new GetVersionCallback() {
            @Override
            public void onSuccess(Version version) {
                if (isActivityDestroyed()) return;

                splashPB.setVisibility(View.INVISIBLE);

                //save the server version to the PreferenceHelper
                String major = String.valueOf(version.getMajor());
                String minor = String.valueOf(version.getMinor());
                String build = String.valueOf(version.getBuild());
                mPreferenceHelper.putString(PreferenceConstants.KEY_SERVER_VERSION, major + "." + minor + "." + build);

                //take user to login screen
                startLoginActivity();
            }

            @Override
            public void onError(final SpException exception) {
                if (isActivityDestroyed()) return;

                if (exception instanceof SpVersionException) {
                    SpVersionException versionException = (SpVersionException) exception;
                    switch (versionException.getReason()) {
                        case UPGRADE_OPTIONAL:
                            UIHelper.showMessageDialogWithTitleTwoButtonCallback(mContext, getResources().getString(R.string.optional_upgrade), exception.getMessage(), getResources().getString(R.string.update), getResources().getString(R.string.cancel), new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    handleVersionResponse(((SpVersionException) exception).getVersion());
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    startLoginActivity();
                                }
                            });
                            break;
                        case UPGRADE_MANDATORY:
                            UIHelper.showMessageDialogWithTitleCallback(mContext, getResources().getString(R.string.mandatory_update), exception.getMessage(), getResources().getString(R.string.update), new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    handleVersionResponse(((SpVersionException) exception).getVersion());
                                }
                            });
                            break;
                        case VERSION_CHECK_ERROR:
                            UIHelper.showMessageDialogWithCallback(mContext, exception.getMessage(), getResources().getString(R.string.ok), new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    finish();
                                }
                            });
                            break;
                    }
                } else {
                    String error = exception.getMessage();
                    UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), error);
                }
            }
        });
    }

    private void startLoginActivity() {
        checkAndroidPermissions();
    }

    private void checkAndroidPermissions() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.CAMERA, "android.permission.BBPOS"};
        ActivityCompat.requestPermissions(SplashActivity.this,
                permissions,
                REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateLocationInfo();
                            finish();
                            startActivity(new Intent(mContext, LoginActivity.class));
                        }
                    }, 300);
                } else {
                    Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void startMainActivity() {
        finish();
        startActivity(new Intent(mContext, MainActivity.class));
    }

    private void handleVersionResponse(Version o) {
        finish();
        Intent aboutIntent = new Intent(mContext, AboutUsActivity.class);
        AboutUsActivity.putUpdateMandatory(aboutIntent, true);


        String major = String.valueOf(o.getMajor());
        String minor = String.valueOf(o.getMinor());
        String build = String.valueOf(o.getBuild());
        mPreferenceHelper.putString(PreferenceConstants.KEY_SERVER_VERSION, major + "." + minor + "." + build);
        startActivity(aboutIntent);
    }


    private double[] getGPS() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l = null;
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }
        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
    }

    private void updateLocationInfo() {
        double[] gps = getGPS();
        if (gps != null) {
            latitude = gps[0];
            longitude = gps[1];
            //set the latitude and longitude if it is not null
            if (latitude != null && longitude != null) {
                UIHelper.log("lat=" + latitude + " long=" + longitude);
                serviceManager.get().setCoordinates(latitude, longitude);
            }
        }
    }

}
