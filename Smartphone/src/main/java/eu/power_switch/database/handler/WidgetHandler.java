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

import eu.power_switch.database.table.widget.ReceiverWidgetTable;
import eu.power_switch.database.table.widget.RoomWidgetTable;
import eu.power_switch.database.table.widget.SceneWidgetTable;
import eu.power_switch.widget.ReceiverWidget;
import eu.power_switch.widget.RoomWidget;
import eu.power_switch.widget.SceneWidget;

/**
 * Provides database methods for managing Widgets
 */
public abstract class WidgetHandler {

    /**
     * Adds Receiver Widget to Database
     *
     * @param receiverWidget ReceiverWidget
     */
    protected static void addReceiverWidget(ReceiverWidget receiverWidget) {
        ContentValues values = new ContentValues();
        values.put(ReceiverWidgetTable.COLUMN_WIDGET_APP_ID, receiverWidget.getWidgetId());
        values.put(ReceiverWidgetTable.COLUMN_ROOM_ID, receiverWidget.getRoomId());
        values.put(ReceiverWidgetTable.COLUMN_RECEIVER_ID, receiverWidget.getReceiverId());
        DatabaseHandler.database.insert(ReceiverWidgetTable.TABLE_NAME, null, values);
    }

    /**
     * Adds Room Widget to Database
     *
     * @param roomWidget RoomWidget
     */
    protected static void addRoomWidget(RoomWidget roomWidget) {
        ContentValues values = new ContentValues();
        values.put(RoomWidgetTable.COLUMN_WIDGET_APP_ID, roomWidget.getWidgetId());
        values.put(RoomWidgetTable.COLUMN_ROOM_ID, roomWidget.getRoomId());
        DatabaseHandler.database.insert(RoomWidgetTable.TABLE_NAME, null, values);
    }

    /**
     * Adds Scene Widget to Database
     *
     * @param sceneWidget SceneWidget
     */
    protected static void addSceneWidget(SceneWidget sceneWidget) {
        ContentValues values = new ContentValues();
        values.put(SceneWidgetTable.COLUMN_WIDGET_APP_ID, sceneWidget.getWidgetId());
        values.put(SceneWidgetTable.COLUMN_SCENE_ID, sceneWidget.getSceneId());
        DatabaseHandler.database.insert(SceneWidgetTable.TABLE_NAME, null, values);
    }

    /**
     * Deletes Receiver Widget from Database
     *
     * @param widgetId ID of ReceiverWidget
     */
    protected static void deleteReceiverWidget(int widgetId) {
        DatabaseHandler.database.delete(ReceiverWidgetTable.TABLE_NAME, ReceiverWidgetTable.COLUMN_WIDGET_APP_ID +
                "=" + widgetId, null);
    }

    /**
     * Deletes Room Widget from Database
     *
     * @param widgetId ID of RoomWidget
     */
    protected static void deleteRoomWidget(int widgetId) {
        DatabaseHandler.database.delete(RoomWidgetTable.TABLE_NAME, RoomWidgetTable.COLUMN_WIDGET_APP_ID +
                "=" + widgetId, null);
    }

    /**
     * Deletes Scene Widget from Database
     *
     * @param widgetId ID of SceneWidget
     */
    protected static void deleteSceneWidget(int widgetId) {
        DatabaseHandler.database.delete(SceneWidgetTable.TABLE_NAME, SceneWidgetTable.COLUMN_WIDGET_APP_ID +
                "=" + widgetId, null);
    }

    /**
     * Returns a ReceiverWidget container using the matching widget_id used to identify widgets (NOT database ID!)
     *
     * @param widgetId
     * @return
     */
    protected static ReceiverWidget getReceiverWidget(int widgetId) {
        String[] widgetColumns = {ReceiverWidgetTable.COLUMN_WIDGET_APP_ID, ReceiverWidgetTable.COLUMN_ROOM_ID,
                ReceiverWidgetTable.COLUMN_RECEIVER_ID};
        Cursor cursor = DatabaseHandler.database.query(ReceiverWidgetTable.TABLE_NAME, widgetColumns,
                ReceiverWidgetTable.COLUMN_WIDGET_APP_ID + "=" + widgetId, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.isAfterLast()) {
            cursor.close();
            return null;
        }

        ReceiverWidget receiverWidget = dbToReceiverWidgetInfo(cursor);
        cursor.close();
        return receiverWidget;
    }

    /**
     * Returns a RoomWidget container using the matching widget_id used to identify widgets (NOT database ID!)
     *
     * @param widgetId
     * @return
     */
    protected static RoomWidget getRoomWidget(int widgetId) {
        String[] widgetColumns = {RoomWidgetTable.COLUMN_WIDGET_APP_ID, RoomWidgetTable.COLUMN_ROOM_ID};
        Cursor cursor = DatabaseHandler.database.query(RoomWidgetTable.TABLE_NAME, widgetColumns,
                RoomWidgetTable.COLUMN_WIDGET_APP_ID + "=" + widgetId, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.isAfterLast()) {
            cursor.close();
            return null;
        }

        RoomWidget roomWidget = dbToRoomWidgetInfo(cursor);
        cursor.close();
        return roomWidget;
    }

    /**
     * Returns a SceneWidget container using the matching widget_id used to identify widgets (NOT database ID!)
     *
     * @param widgetId
     * @return
     */
    protected static SceneWidget getSceneWidget(int widgetId) {
        String[] widgetColumns = {SceneWidgetTable.COLUMN_WIDGET_APP_ID, SceneWidgetTable.COLUMN_SCENE_ID};
        Cursor cursor = DatabaseHandler.database.query(SceneWidgetTable.TABLE_NAME, widgetColumns,
                SceneWidgetTable.COLUMN_WIDGET_APP_ID + "=" + widgetId, null, null, null, null);
        cursor.moveToFirst();

        if (cursor.isAfterLast()) {
            cursor.close();
            return null;
        }

        SceneWidget sceneWidget = dbToSceneWidgetInfo(cursor);
        cursor.close();
        return sceneWidget;
    }

    /**
     * Creates a ReceiverWidget Object out of Database information
     *
     * @param c cursor pointing to a ReceiverWidget database entry
     * @return ReceiverWidget
     */
    private static ReceiverWidget dbToReceiverWidgetInfo(Cursor c) {
        int widgetId = c.getInt(0);
        long roomId = c.getLong(1);
        long receiverId = c.getLong(2);

        return new ReceiverWidget(widgetId, roomId, receiverId);
    }

    /**
     * Creates a RoomWidget Object out of Database information
     *
     * @param c cursor pointing to a RoomWidget database entry
     * @return RoomWidget
     */
    private static RoomWidget dbToRoomWidgetInfo(Cursor c) {
        int widgetId = c.getInt(0);
        long roomId = c.getLong(1);

        return new RoomWidget(widgetId, roomId);
    }

    /**
     * Creates a SceneWidget Object out of Database information
     *
     * @param c cursor pointing to a SceneWidget database entry
     * @return SceneWidget
     */
    private static SceneWidget dbToSceneWidgetInfo(Cursor c) {
        int widgetId = c.getInt(0);
        long sceneId = c.getLong(1);

        return new SceneWidget(widgetId, sceneId);
    }
}