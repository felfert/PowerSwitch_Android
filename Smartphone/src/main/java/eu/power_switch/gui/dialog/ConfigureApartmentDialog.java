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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.gui.fragment.configure_apartment.ConfigureApartmentDialogPage1NameFragment;
import eu.power_switch.gui.fragment.configure_apartment.ConfigureApartmentDialogPage2LocationFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.LocalBroadcastConstants;
import eu.power_switch.shared.log.Log;

/**
 * Dialog to configure (create/edit) an Apartment
 * <p/>
 * Created by Markus on 27.12.2015.
 */
public class ConfigureApartmentDialog extends ConfigurationDialogTabbed {

    /**
     * ID of existing Apartment to Edit
     */
    public static final String APARTMENT_ID_KEY = "ApartmentId";

    private BroadcastReceiver broadcastReceiver;

    private long apartmentId = -1;

    @Override
    protected void init(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifyConfigurationChanged();
            }
        };
    }

    @Override
    protected boolean initializeFromExistingData(Bundle args) {
        if (args != null && args.containsKey(APARTMENT_ID_KEY)) {
            apartmentId = args.getLong(APARTMENT_ID_KEY);

            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment(), apartmentId));
        } else {
            setTabAdapter(new CustomTabAdapter(getActivity(), getChildFragmentManager(),
                    (RecyclerViewFragment) getTargetFragment()));
        }

        try {
            // Disable delete Button if only one apartment is left
            if (DatabaseHandler.getAllApartments().size() <= 1) {
                imageButtonDelete.setColorFilter(ContextCompat.getColor(getActivity(), R.color.inactive_gray));
                imageButtonDelete.setClickable(false);
                return true;
            } else {
                return args != null && args.containsKey(APARTMENT_ID_KEY);
            }
        } catch (Exception e) {
            dismiss();
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
            return false;
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_apartment;
    }

    @Override
    protected boolean isValid() {
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigurationDialogTabbedSummaryFragment setupFragment =
                customTabAdapter.getSetupFragment();
        return setupFragment.checkSetupValidity();
    }

    /**
     * Saves current configuration to database
     * Either updates an existing Gateway or creates a new one
     */
    @Override
    protected void saveCurrentConfigurationToDatabase() {
        Log.d("Saving apartment");
        CustomTabAdapter customTabAdapter = (CustomTabAdapter) getTabAdapter();
        ConfigurationDialogTabbedSummaryFragment setupFragment = customTabAdapter.getSetupFragment();
        try {
            setupFragment.saveCurrentConfigurationToDatabase();
        } catch (Exception e) {
            Log.e(e);
            StatusMessageHandler.showStatusMessage(getContext(), R.string.unknown_error, 5000);
        }
        getDialog().dismiss();
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.are_you_sure)
                .setMessage(R.string.apartment_will_be_gone_forever)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Apartment apartment = DatabaseHandler.getApartment(apartmentId);
                            if (SmartphonePreferencesHandler.getCurrentApartmentId()
                                    .equals(apartment.getId())) {
                                DatabaseHandler.deleteApartment(apartmentId);
                                // update current Apartment selection
                                Apartment firstApartment = DatabaseHandler.getAllApartments().get(0);
                                SmartphonePreferencesHandler.setCurrentApartmentId(firstApartment.getId());
                            } else {
                                DatabaseHandler.deleteApartment(apartmentId);
                            }


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

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcastConstants.INTENT_SETUP_APARTMENT_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    private static class CustomTabAdapter extends FragmentPagerAdapter {

        private Context context;
        private long apartmentId;
        private ConfigurationDialogTabbedSummaryFragment setupFragment;
        private RecyclerViewFragment recyclerViewFragment;

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment) {
            super(fm);
            this.context = context;
            this.apartmentId = -1;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public CustomTabAdapter(Context context, FragmentManager fm, RecyclerViewFragment recyclerViewFragment, long id) {
            super(fm);
            this.context = context;
            this.apartmentId = id;
            this.recyclerViewFragment = recyclerViewFragment;
        }

        public ConfigurationDialogTabbedSummaryFragment getSetupFragment() {
            return setupFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return context.getString(R.string.name);
                case 1:
                    return context.getString(R.string.location);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = new ConfigureApartmentDialogPage1NameFragment();
                    break;
                case 1:
                    fragment = new ConfigureApartmentDialogPage2LocationFragment();
                    fragment.setTargetFragment(recyclerViewFragment, 0);

                    setupFragment = (ConfigurationDialogTabbedSummaryFragment) fragment;
            }

            if (fragment != null && apartmentId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong(APARTMENT_ID_KEY, apartmentId);
                fragment.setArguments(bundle);
            }

            return fragment;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 2;
        }
    }
}
