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

package eu.power_switch.database.handler;

import android.content.ContentValues;
import android.database.Cursor;

import eu.power_switch.database.table.receiver.AutoPairTable;

/**
 * Provides database methods for managing AutoPairReceivers
 *
 * Created by Markus on 10.09.2015.
 */
public abstract class AutoPairHandler {

    /**
     * Adds the AutoPairReceiver details of a new receiver.
     *
     * @param receiverID The ID of the receiver.
     * @param seed       The seed of the receiver.
     */
    protected static void add(long receiverID, long seed) {
        ContentValues values = new ContentValues();
        values.put(AutoPairTable.COLUMN_SEED, seed);
        values.put(AutoPairTable.COLUMN_RECEIVER_ID, receiverID);
        DatabaseHandler.database.insert(AutoPairTable.TABLE_NAME, null, values);
    }

    /**
     * Deletes the AutoPairReceiver details of a receiver.
     *
     * @param receiverID ID of the deleted receiver
     */
    protected static void delete(long receiverID) {
        DatabaseHandler.database.delete(AutoPairTable.TABLE_NAME, AutoPairTable.COLUMN_RECEIVER_ID + "=" + receiverID, null);
    }

    /**
     * Returns the seed of a AutoPairReceiver.
     *
     * @param receiverID The ID of the receiver.
     * @return The seed of the receiver.
     */
    protected static long getSeed(long receiverID) {
        String[] columns = {AutoPairTable.COLUMN_SEED};
        Cursor cursor = DatabaseHandler.database.query(AutoPairTable.TABLE_NAME, columns,
                AutoPairTable.COLUMN_RECEIVER_ID + "==" + receiverID, null, null, null, null);
        cursor.moveToFirst();
        long master = cursor.getLong(0);
        cursor.close();
        return master;
    }
}