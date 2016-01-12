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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import eu.power_switch.R;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.shared.log.Log;

/**
 * Abstract class defining a configuration Dialog
 * <p/>
 * Every configuration Dialog has a bottom bar with 3 Buttons (Delete, Cancel, Save) and a contentView
 * <p/>
 * Created by Markus on 27.12.2015.
 */
public abstract class ConfigurationDialog extends DialogFragment {

    protected ImageButton imageButtonDelete;
    protected ImageButton imageButtonCancel;
    protected ImageButton imageButtonSave;

    private boolean modified;
    private View rootView;
    private View contentView;
    private Runnable deleteAction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.dialog_configuration, null);

        FrameLayout contentViewContainer = (FrameLayout) rootView.findViewById(R.id.contentView);

        contentView = initContentView(inflater, contentViewContainer, savedInstanceState);
        contentViewContainer.addView(contentView);

        imageButtonDelete = (ImageButton) rootView.findViewById(R.id.imageButton_delete);
        imageButtonDelete.setImageDrawable(IconicsHelper.getDeleteIcon(getActivity(), R.color.delete_color));
        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteAction != null) {
                    deleteAction.run();
                }
            }
        });

        imageButtonCancel = (ImageButton) rootView.findViewById(R.id.imageButton_cancel);
        imageButtonCancel.setImageDrawable(IconicsHelper.getCancelIcon(getActivity()));
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modified) {
                    // ask to really close
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getDialog().cancel();
                                }
                            })
                            .setNeutralButton(android.R.string.no, null)
                            .setMessage(R.string.all_changes_will_be_lost)
                            .show();
                } else {
                    getDialog().dismiss();
                }
            }
        });

        imageButtonSave = (ImageButton) rootView.findViewById(R.id.imageButton_save);
        imageButtonSave.setImageDrawable(IconicsHelper.getSaveIcon(getActivity()));
        imageButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!modified) {
                    getDialog().dismiss();
                } else {
                    saveCurrentConfigurationToDatabase();
                    getDialog().dismiss();
                }
            }
        });

        initExistingData(getArguments());

        return rootView;
    }

    /**
     * Initialize the content view of this configuration dialog in here.
     * Inflate your custom layout, find its views and bind their logic
     *
     * @param inflater           Layoutinflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    protected abstract View initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * Initialize your dialog in here using passed in arguments
     *
     * @param arguments arguments passed in via setArguments()
     */
    protected abstract void initExistingData(Bundle arguments);

    /**
     * Set a deleteAction
     *
     * @param runnable Runnable containing delete actions
     */
    protected void setDeleteAction(Runnable runnable) {
        this.deleteAction = runnable;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity()) {
            @Override
            public void onBackPressed() {
                if (modified) {
                    // ask to really close
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getDialog().cancel();
                                }
                            })
                            .setNeutralButton(android.R.string.no, null)
                            .setMessage(R.string.all_changes_will_be_lost)
                            .show();
                } else {
                    getDialog().cancel();
                }
            }
        };
        dialog.setTitle(getDialogTitle());
        dialog.setCanceledOnTouchOutside(false); // prevent close dialog on touch outside window
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager
                .LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        return dialog;
    }

    protected abstract
    @StringRes
    int getDialogTitle();

    /**
     * Get modification state of this Dialog
     *
     * @return true if modifications (by user or system) have been made
     */
    protected boolean isModified() {
        return modified;
    }

    /**
     * Set the state of this Dialog
     *
     * @param modified true if Dialog has been edited
     */
    protected void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Checks if the current dialog configuration is valid
     *
     * @return true if the current configuration is valid, false otherwise
     */
    protected abstract boolean isValid() throws Exception;

    /**
     * Call this method when the configuration of the dialog has changed and UI has to be updated
     * f.ex. bottom bar buttons
     */
    protected void notifyConfigurationChanged() {
        setModified(true);
        try {
            setSaveButtonState(isValid());
        } catch (Exception e) {
            Log.e(e);
            setSaveButtonState(false);
        }
    }

    /**
     * Set the state of the save button in the bottom bar
     *
     * @param enabled true: green and clickable, false: gray and NOT clickable
     */
    protected void setSaveButtonState(boolean enabled) {
        if (enabled) {
            imageButtonSave.setColorFilter(ContextCompat.getColor(getActivity(), R.color.active_green));
            imageButtonSave.setClickable(true);
        } else {
            imageButtonSave.setColorFilter(ContextCompat.getColor(getActivity(), R.color.inactive_gray));
            imageButtonSave.setClickable(false);
        }
    }

    /**
     * This method is called when the user wants to save the current configuration to database and close the dialog
     * Save the current configuration of your object to database in this method
     */
    protected abstract void saveCurrentConfigurationToDatabase();
}
