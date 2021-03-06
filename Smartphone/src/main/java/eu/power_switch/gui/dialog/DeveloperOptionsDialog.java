/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import java.util.Locale;

import eu.power_switch.R;
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.settings.DeveloperPreferencesHandler;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Hidden Dialog Menu to access developer options
 */
public class DeveloperOptionsDialog extends DialogFragment {

    private View rootView;
    private GeofenceApiHandler geofenceApiHandler;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        rootView = inflater.inflate(R.layout.dialog_developer_options, null);

        geofenceApiHandler = new GeofenceApiHandler(getActivity());

        CheckBox checkBox_playStoreMode = (CheckBox) rootView.findViewById(R.id.checkBox_playStoreMode);
        checkBox_playStoreMode.setChecked(DeveloperPreferencesHandler.getPlayStoreMode());
        checkBox_playStoreMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeveloperPreferencesHandler.setPlayStoreMode(isChecked);
            }
        });

        Button resetShowcasesButton = (Button) rootView.findViewById(R.id.button_resetShowcases);
        resetShowcasesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialShowcaseView.resetAll(getContext());
            }
        });

        Button removeAllGeofences = (Button) rootView.findViewById(R.id.button_removeAllGeofences);
        removeAllGeofences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geofenceApiHandler.removeAllGeofences();
            }
        });

        Button forceUnknownExceptionDialog = (Button) rootView.findViewById(R.id.button_forceUnknownExceptionDialog);
        forceUnknownExceptionDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusMessageHandler.showErrorDialog(getContext(), new Exception("Unknown error during runtime!"));
            }
        });

        Button forceUnhandledException = (Button) rootView.findViewById(R.id.button_forceUnhandledException);
        forceUnhandledException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new RuntimeException("Unhandled Exception");
            }
        });

        final Spinner spinnerLanguage = (Spinner) rootView.findViewById(R.id.spinner_language);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.locales, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.setSelection(getIndex(spinnerLanguage, DeveloperPreferencesHandler.getLocale().toString()));
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String localeString = spinnerLanguage.getItemAtPosition(position).toString();
                DeveloperPreferencesHandler.setLocale(new Locale(localeString));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        CheckBox checkBoxForceLanguage = (CheckBox) rootView.findViewById(R.id.checkBox_forceLanguage);
        checkBoxForceLanguage.setChecked(DeveloperPreferencesHandler.getForceLanguage());
        checkBoxForceLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DeveloperPreferencesHandler.setForceLanguage(isChecked);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);
        builder.setTitle("Developer Options");
        builder.setNeutralButton(android.R.string.ok, null);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.show();

        return dialog;
    }

    //private method of your class
    private int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void onStart() {
        super.onStart();
        geofenceApiHandler.onStart();
    }

    @Override
    public void onStop() {
        geofenceApiHandler.onStop();
        super.onStop();
    }
}
