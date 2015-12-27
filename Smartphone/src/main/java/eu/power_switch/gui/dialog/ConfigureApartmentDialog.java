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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.shared.log.Log;

/**
 * Created by Markus on 27.12.2015.
 */
public class ConfigureApartmentDialog extends ConfigurationDialog {

    /**
     * ID of existing Apartment to Edit
     */
    public static final String APARTMENT_ID_KEY = "ApartmentId";

    private View rootView;
    private TextInputLayout floatingName;
    private EditText name;

    private long apartmentId = -1;

    private List<Apartment> existingApartments;
    private String originalName;

    @Override
    protected View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_configure_apartment_content, null);

        setDeleteAction(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.are_you_sure)
                        .setMessage(R.string.apartment_will_be_gone_forever)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DatabaseHandler.deleteApartment(apartmentId);
                                    ApartmentFragment.sendApartmentChangedBroadcast(getActivity());
                                    StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(),
                                            R.string.apartment_removed, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    Log.e(e);
                                    StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
                                }

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
            }
        });

        try {
            existingApartments = DatabaseHandler.getAllApartments();
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }

        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setModified(true);
                checkValidity();
            }
        };
        floatingName = (TextInputLayout) rootView.findViewById(R.id.apartment_name_text_input_layout);
        name = (EditText) rootView.findViewById(R.id.txt_edit_apartment_name);
        name.addTextChangedListener(textWatcher);

        return rootView;
    }

    @Override
    protected void initExistingData(Bundle args) {
        if (args != null && args.containsKey(APARTMENT_ID_KEY)) {
            apartmentId = args.getLong(APARTMENT_ID_KEY);
            initializeApartmentData(apartmentId);
        } else {
            // hide if new apartment
            imageButtonDelete.setVisibility(View.GONE);

            setSaveButtonState(false);
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_apartment;
    }

    /**
     * Loads existing apartment data into fields
     *
     * @param apartmentId ID of existing Apartment
     */
    private void initializeApartmentData(Long apartmentId) {
        try {
            Apartment apartment = DatabaseHandler.getApartment(apartmentId);
            originalName = apartment.getName();
            name.setText(originalName);

            setModified(false);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
    }

    /**
     * Checks if current configuration is valid and updates views accordingly
     */
    private void checkValidity() {
        boolean nameIsValid;

        try {
            String name = getCurrentName();

            nameIsValid = checkNameValidity(name);

            setSaveButtonState(nameIsValid);
        } catch (Exception e) {
            Log.e(e);
            setSaveButtonState(false);
        }
    }

    /**
     * Checks if current name is valid
     *
     * @param name
     * @return true if valid
     */
    private boolean checkNameValidity(String name) {
        if (name.length() <= 0) {
            floatingName.setError(getString(R.string.please_enter_name));
            floatingName.setErrorEnabled(true);
            return false;
        } else {
            floatingName.setError(null);
            floatingName.setErrorEnabled(false);
            return true;
        }
    }

    /**
     * Gets current name field value
     *
     * @return Name of Apartment
     */
    public String getCurrentName() {
        return this.name.getText().toString().trim();
    }

    /**
     * Saves current configuration to database
     * Either updates an existing Gateway or creates a new one
     */
    @Override
    protected void saveCurrentConfigurationToDatabase() {
        try {
            if (apartmentId == -1) {
                String apartmentName = getCurrentName();

                Apartment newApartment = new Apartment((long) -1, apartmentName);

                try {
                    DatabaseHandler.addApartment(newApartment);
                } catch (Exception e) {
                    StatusMessageHandler.showStatusMessage(rootView.getContext(),
                            R.string.unknown_error, Snackbar.LENGTH_LONG);
                }
            } else {
                DatabaseHandler.updateApartment(apartmentId, getCurrentName());
            }

            ApartmentFragment.sendApartmentChangedBroadcast(getActivity());
            StatusMessageHandler.showStatusMessage((RecyclerViewFragment) getTargetFragment(), R.string.apartment_saved,
                    Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(rootView.getContext(), R.string.unknown_error, Snackbar.LENGTH_LONG);
        }
    }
}
