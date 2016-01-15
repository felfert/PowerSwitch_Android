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

package eu.power_switch.obj.receiver.device.vivanco;

import android.content.Context;

import java.util.LinkedList;

import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.Receiver;

public class FSS33600W extends FSS31000W implements DipReceiver {
    // dips are 12345 ABCDE

    private static final String MODEL = Receiver.getModelName(FSS33600W.class.getCanonicalName());

    public FSS33600W(Context context, Long id, String name, LinkedList<Boolean> dips, Long roomId) {
        super(context, id, name, dips, roomId);
        model = MODEL;
    }
}