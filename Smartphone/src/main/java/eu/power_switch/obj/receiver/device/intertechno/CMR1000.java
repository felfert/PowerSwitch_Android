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

package eu.power_switch.obj.receiver.device.intertechno;

import android.content.Context;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.obj.button.OffButton;
import eu.power_switch.obj.button.OnButton;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;
import eu.power_switch.shared.log.Log;

public class CMR1000 extends Receiver implements MasterSlaveReceiver {

    private static final Brand BRAND = Brand.INTERTECHNO;
    private static final String MODEL = Receiver.getModelName(CMR1000.class.getCanonicalName());

    private String tx433version = "1,";

    private String sSpeedConnAir = "140";
    private String headConnAir = "TXP:0,0,6,11125,89,25,";
    private String headITGW = "0,0,6,11125,89,26,0,";

    private String sSpeedITGW = "125,";
    private String tailConnAir = tx433version + sSpeedConnAir + ";";
    private String tailITGW = tx433version + sSpeedITGW + "0";

    private Character channelMaster;
    private int channelSlave;

    public CMR1000(Context context, Long id, String name, char channelMaster, int channelSlave, Long roomId) {
        super(context, id, name, BRAND, MODEL, Type.MASTER_SLAVE, roomId);
        buttons.add(new OnButton(context, id));
        buttons.add(new OffButton(context, id));
        this.channelMaster = channelMaster;
        this.channelSlave = channelSlave;
    }

    @Override
    public ArrayList<String> getMasterNames() {
        ArrayList<String> m = new ArrayList<>();
        m.add("A");
        m.add("B");
        m.add("C");
        m.add("D");
        m.add("E");
        m.add("F");
        m.add("G");
        m.add("H");
        m.add("I");
        m.add("J");
        m.add("K");
        m.add("L");
        m.add("M");
        m.add("N");
        m.add("O");
        m.add("P");
        return m;
    }

    @Override
    public ArrayList<String> getSlaveNames() {
        ArrayList<String> s = new ArrayList<>();
        s.add("1");
        s.add("2");
        s.add("3");
        s.add("4");
        s.add("5");
        s.add("6");
        s.add("7");
        s.add("8");
        s.add("9");
        s.add("10");
        s.add("11");
        s.add("12");
        s.add("13");
        s.add("14");
        s.add("15");
        s.add("16");
        return s;
    }

    @Override
    public String getSignal(Gateway gateway, String action) throws ActionNotSupportedException, GatewayNotSupportedException {

        String lo = "4,";
        String hi = "12,";
        String seqLo = lo + hi + lo + hi;
        String seqFl = lo + hi + hi + lo;
        String h = seqFl;
        String l = seqLo;
        String on = seqFl + seqFl;
        String off = seqFl + seqLo;
        String additional = seqLo + seqFl;

        // switch channelMaster (character)
        String master = "";
        switch (channelMaster) {
            case 'A':
                master = l + l + l + l;
                break;
            case 'B':
                master = h + l + l + l;
                break;
            case 'C':
                master = l + h + l + l;
                break;
            case 'D':
                master = h + h + l + l;
                break;
            case 'E':
                master = l + l + h + l;
                break;
            case 'F':
                master = h + l + h + l;
                break;
            case 'G':
                master = l + h + h + l;
                break;
            case 'H':
                master = h + h + h + l;
                break;
            case 'I':
                master = l + l + l + h;
                break;
            case 'J':
                master = h + l + l + h;
                break;
            case 'K':
                master = l + h + l + h;
                break;
            case 'L':
                master = h + h + l + h;
                break;
            case 'M':
                master = l + l + h + h;
                break;
            case 'N':
                master = h + l + h + h;
                break;
            case 'O':
                master = l + h + h + h;
                break;
            case 'P':
                master = h + h + h + h;
                break;
            default:
                Log.e("Switch", "No Matching Master");
                break;
        }

        // switch channelSlave (number)
        String slave = "";
        switch (channelSlave) {
            case 1:
                slave = l + l + l + l;
                break;
            case 2:
                slave = h + l + l + l;
                break;
            case 3:
                slave = l + h + l + l;
                break;
            case 4:
                slave = h + h + l + l;
                break;
            case 5:
                slave = l + l + h + l;
                break;
            case 6:
                slave = h + l + h + l;
                break;
            case 7:
                slave = l + h + h + l;
                break;
            case 8:
                slave = h + h + h + l;
                break;
            case 9:
                slave = l + l + l + h;
                break;
            case 10:
                slave = h + l + l + h;
                break;
            case 11:
                slave = l + h + l + h;
                break;
            case 12:
                slave = h + h + l + h;
                break;
            case 13:
                slave = l + l + h + h;
                break;
            case 14:
                slave = h + l + h + h;
                break;
            case 15:
                slave = l + h + h + h;
                break;
            case 16:
                slave = h + h + h + h;
                break;
            default:
                Log.e("Switch", "No Matching Slave");
                break;
        }

        if (gateway instanceof ConnAir) {
            if (action.equals(context.getString(R.string.on))) {
                String ON = headConnAir + master + slave + additional + on + tailConnAir;
                return ON;
            } else if (action.equals(context.getString(R.string.off))) {
                String OFF = headConnAir + master + slave + additional + off + tailConnAir;
                return OFF;
            } else {
                throw new ActionNotSupportedException(action);
            }
        } else if (gateway instanceof BrematicGWY433) {
            if (action.equals(context.getString(R.string.on))) {
                String ON = headConnAir + master + slave + additional + on + tailConnAir;
                return ON;
            } else if (action.equals(context.getString(R.string.off))) {
                String OFF = headConnAir + master + slave + additional + off + tailConnAir;
                return OFF;
            } else {
                throw new ActionNotSupportedException(action);
            }
        } else if (gateway instanceof ITGW433) {
            if (action.equals(context.getString(R.string.on))) {
                String ON = headITGW + master + slave + additional + on + tailITGW;
                return ON;
            } else if (action.equals(context.getString(R.string.off))) {
                String OFF = headITGW + master + slave + additional + off + tailITGW;
                return OFF;
            } else {
                throw new ActionNotSupportedException(action);
            }
        } else {
            throw new GatewayNotSupportedException();
        }
    }

    @Override
    public char getMaster() {
        return channelMaster;
    }

    @Override
    public int getSlave() {
        return channelSlave;
    }

}