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

package eu.power_switch.backup;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.exception.backup.BackupAlreadyExistsException;
import eu.power_switch.shared.exception.backup.BackupNotFoundException;
import eu.power_switch.shared.exception.backup.CreateBackupException;
import eu.power_switch.shared.exception.backup.RemoveBackupException;
import eu.power_switch.shared.exception.backup.RestoreBackupException;
import eu.power_switch.shared.log.Log;

/**
 * Database Handler to access/modify Backups stored on device or external storage
 */
public class BackupHandler {

    /**
     * Default Folder name for storing Backups (on /sdcard/)
     */
    public static final String MAIN_BACKUP_FOLDERNAME = "PowerSwitch_Backup";

    /**
     * Context
     */
    private Context context;

    /**
     * Constructor
     *
     * @param context any suitable context
     */
    public BackupHandler(@NonNull Context context) {
        this.context = context;
    }

    /**
     * Get all Backups
     *
     * @return List of Backups
     */
    @NonNull
    public ArrayList<Backup> getBackups() {
        ArrayList<Backup> backups = new ArrayList<>();
        File backupDir = new File(SmartphonePreferencesHandler.getBackupPath());

        FileFilter backupFileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.list() == null) {
                    return false;
                }

                List<String> subFolders = Arrays.asList(pathname.list());
                return pathname.isDirectory() && subFolders.contains("shared_prefs") &&
                        subFolders.contains("databases");
            }
        };

        if (backupDir.exists()) {
            for (File file : backupDir.listFiles(backupFileFilter)) {
                backups.add(new Backup(file.getName(), new Date(file.lastModified()),
                        backupDir + File.separator + file.getName(), false));
            }
        }
        return backups;
    }

    /**
     * Creates a new Backup
     *
     * @param useExternalStorage use external storage path instead of internal?
     * @param name               name of backup
     * @param force              overwrite existing folders?
     * @throws CreateBackupException
     * @throws BackupAlreadyExistsException
     */
    public void createBackup(boolean useExternalStorage, @NonNull String name, boolean force) throws
            CreateBackupException, BackupAlreadyExistsException {
        if (useExternalStorage) {
            // TODO: kp wie man internen und externen speicher unterscheidet
        } else {
            File src;
            File dst;

            dst = new File(SmartphonePreferencesHandler.getBackupPath() +
                    File.separator + name);
            if (!dst.exists()) {
                dst.mkdirs();
            } else {
                if (force) {
                    // remove existing backup
                    try {
                        if (!deleteRecursive(dst)) {
                            throw new CreateBackupException("Error deleting existing Backup");
                        }
                    } catch (Exception e) {
                        Log.e(e);
                        throw new CreateBackupException(e);
                    }
                    dst.mkdirs();
                } else {
                    throw new BackupAlreadyExistsException();
                }
            }

            try {
                // copy database
                src = new File(context.getFilesDir().getParent() + File.separator + "databases");
                dst = new File(SmartphonePreferencesHandler.getBackupPath() + File.separator
                        + name + File.separator + "databases");
                if (src.exists()) {
                    copyDirectory(src, dst);
                }

                // copy preferences
                src = new File(context.getFilesDir().getParent() + File.separator + "shared_prefs");
                dst = new File(SmartphonePreferencesHandler.getBackupPath() + File.separator
                        + name + File.separator + "shared_prefs");
                if (src.exists()) {
                    copyDirectory(src, dst);
                }
            } catch (Exception e) {
                Log.e(e);
                throw new CreateBackupException(e);
            }
        }
    }

    /**
     * Remove a Backup
     *
     * @param name name of backup
     * @throws BackupNotFoundException
     * @throws RemoveBackupException
     */
    public void removeBackup(@NonNull String name) throws BackupNotFoundException, RemoveBackupException {
        try {
            File backupFolder = new File(SmartphonePreferencesHandler.getBackupPath()
                    + File.separator + name);
            if (!backupFolder.exists()) {
                throw new BackupNotFoundException();
            }
            deleteRecursive(backupFolder);
        } catch (Exception e) {
            Log.e(e);
            throw new RemoveBackupException(e);
        }
    }

    /**
     * Rename existing Backup
     *
     * @param oldName old backup name
     * @param newName new backup name
     * @throws BackupNotFoundException
     * @throws BackupAlreadyExistsException
     */
    public void renameBackup(@NonNull String oldName, @NonNull String newName) throws BackupNotFoundException, BackupAlreadyExistsException {
        File oldFolder = new File(SmartphonePreferencesHandler.getBackupPath()
                + File.separator + oldName);
        File newFolder = new File(SmartphonePreferencesHandler.getBackupPath()
                + File.separator + newName);

        if (!oldFolder.exists()) {
            throw new BackupNotFoundException();
        }
        if (newFolder.exists()) {
            throw new BackupAlreadyExistsException();
        }
        oldFolder.renameTo(newFolder);
    }

    /**
     * Restore Backup
     *
     * @param name name of backup
     * @throws BackupNotFoundException
     * @throws RestoreBackupException
     */
    public void restoreBackup(@NonNull String name) throws BackupNotFoundException, RestoreBackupException {
        // create source path object
        File src = new File(SmartphonePreferencesHandler.getBackupPath() + File.separator + name);
        if (src.exists()) {
            try {
                // create destination path object
                File dst = new File(context.getFilesDir().getParent());

                // delete existing files
                for (File fileOrFolder : dst.listFiles()) {
                    if (fileOrFolder.getPath().equals(context.getFilesDir().getParent() + File.separator
                            + "shared_prefs")
                            || fileOrFolder.getPath().equals(context.getFilesDir().getParent() + File.separator +
                            "databases")) {
                        deleteRecursive(fileOrFolder);
                    }
                }
                // copy directory to system folder
                copyDirectory(src, dst);
            } catch (Exception e) {
                Log.e(e);
                throw new RestoreBackupException(e);
            }
        } else {
            throw new BackupNotFoundException();
        }
    }

    private void copyFile(@NonNull File src, @NonNull File dst) throws IOException {
        if (src.exists()) {
            InputStream in = new FileInputStream(src);
            if (dst.exists()) {
                dst.delete();
            }
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    // If targetLocation does not exist, it will be created.
    private void copyDirectory(@NonNull File sourceLocation, @NonNull File targetLocation) throws Exception {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (String child : children) {
                copyDirectory(new File(sourceLocation, child), new File(targetLocation, child));
            }
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    private boolean deleteRecursive(@NonNull File fileOrDirectory) throws Exception {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }
}
