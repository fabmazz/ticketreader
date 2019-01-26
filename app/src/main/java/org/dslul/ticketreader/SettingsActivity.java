package org.dslul.ticketreader;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity {

    public static final String AUTOSTART_SWITCH = "switch_autostart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(AUTOSTART_SWITCH)) {
                    boolean switchAutostart = sharedPreferences.getBoolean
                            (SettingsActivity.AUTOSTART_SWITCH, false);
                    PackageManager pm = getApplicationContext().getPackageManager();
                    ComponentName compName =
                            new ComponentName(getPackageName(), getPackageName() + ".AliasAutoStartMainActivity");
                    pm.setComponentEnabledSetting(
                            compName,
                            switchAutostart ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                }
            }
        };

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

}