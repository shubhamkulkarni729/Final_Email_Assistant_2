package com.cognition.android.mailboxapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.widget.Toast;

import com.cognition.android.mailboxapp.activities.MainActivity;
import com.cognition.android.mailboxapp.activities.OrganizationActivity;
import com.cognition.android.mailboxapp.activities.PreferenceListActivity;
import com.raizlabs.android.dbflow.sql.language.Operator;

import static com.cognition.android.mailboxapp.activities.MainActivity.AUTO_DELETE_SOCIAL;


public class MySettingsFragment extends PreferenceFragmentCompat {
    SharedPreferences sharedPref;
    public static final String PREF_VIEW_TYPE = "viewType";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.settings);
        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        CheckBoxPreference viewType = (CheckBoxPreference) findPreference("checkBoxViewType");
        String preferredView = sharedPref.getString(PREF_VIEW_TYPE,"Swipe");
        if(preferredView.equals("Swipe")){
            viewType.setChecked(true);
        }
        else viewType.setChecked(false);

        viewType.setOnPreferenceChangeListener((preference, newValue) -> {
            Boolean boolVal = (Boolean)newValue;
            if(boolVal)
            {
                editor.putString(PREF_VIEW_TYPE, "Swipe");
                Toast.makeText(getContext(),"Preferred view set to Swipe.",Toast.LENGTH_SHORT).show();
                editor.apply();
            }
            else
            {
                editor.putString(PREF_VIEW_TYPE, "List");
                editor.apply();
                Toast.makeText(getContext(),"Preferred view set to List.",Toast.LENGTH_SHORT).show();

            }
            return true;
        });

        String autoDeleteSocialValue = sharedPref.getString(AUTO_DELETE_SOCIAL,"false");
        SwitchPreferenceCompat autoDeleteSocial = (SwitchPreferenceCompat)findPreference("keyAutoDeleteSocialMails");
        if(autoDeleteSocialValue.equals("true"))     autoDeleteSocial.setChecked(true);
        else autoDeleteSocial.setChecked(false);

        autoDeleteSocial.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean value = (boolean)newValue;
                if(value == true)
                {
                    editor.putString(AUTO_DELETE_SOCIAL,"true");
                    editor.apply();
                    Toast.makeText(getContext(),"Auto delete enabled",Toast.LENGTH_SHORT).show();
                    getContext().startService(new Intent(getContext(),AutoDeleteSocialMails.class));
                }
                if(value == false)
                {
                    editor.putString(AUTO_DELETE_SOCIAL,"false");
                    editor.apply();
                    Toast.makeText(getContext(),"Auto delete disabled",Toast.LENGTH_SHORT).show();
                    getContext().stopService(new Intent(getContext(),AutoDeleteSocialMails.class));
                }
                return true;
            }
        });

        Preference deleteOld = (Preference) findPreference("deleteOld");
        deleteOld.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(),ActivityDeleteOldMails.class);
                startActivity(intent);
                return false;
            }
        });

        Preference prioritySenderList = (Preference)findPreference("keyPriorityList");
        prioritySenderList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), EmailPreference.class);
                startActivity(intent);
                return false;
            }
        });

        Preference organizationName = (Preference) findPreference("keyEditOrganizationName");
        organizationName.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), OrganizationActivity.class);
                intent.putExtra("callingActivity","Settings");
                startActivity(intent);
                return false;
            }
        });

        Preference categoryPriorityList = (Preference)findPreference("keyPriorityCategoryList");
        categoryPriorityList.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), PreferenceListActivity.class);
                intent.putExtra("callingActivity","Settings");
                startActivity(intent);
                return false;
            }
        });
    }
}