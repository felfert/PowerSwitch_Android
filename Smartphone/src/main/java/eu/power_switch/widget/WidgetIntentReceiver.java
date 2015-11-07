package eu.power_switch.widget;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import eu.power_switch.R;
import eu.power_switch.api.IntentReceiver;
import eu.power_switch.log.Log;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.Constants;
import eu.power_switch.shared.haptic_feedback.VibrationHandler;

/**
 * Created by Markus on 07.11.2015.
 */
public class WidgetIntentReceiver extends BroadcastReceiver {

    private static String KEY_BUTTON = "Button";
    private static String KEY_RECEIVER = "Receiver";
    private static String KEY_ROOM = "Room";
    private static String KEY_SCENE = "Scene";

    /**
     * Generates a unique PendingIntent for actions on receiver widgets
     *
     * @param context           any suitable context
     * @param room              Room
     * @param receiver          Receiver
     * @param button            Button
     * @param uniqueRequestCode unique identifier for different combinations of rooms, receivers and buttons
     * @return PendingIntent
     */
    public static PendingIntent buildReceiverWidgetActionPendingIntent(Context context, Room room, Receiver receiver,
                                                                       Button button, int uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createReceiverButtonIntent(room.getName(),
                receiver.getName(), button.getName()), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent createReceiverButtonIntent(String roomName, String receiverName, String buttonName) {
        Intent intent = new Intent();
        intent.setAction(Constants.WIDGET_ACTION_INTENT);
        intent.putExtra(KEY_ROOM, roomName);
        intent.putExtra(KEY_RECEIVER, receiverName);
        intent.putExtra(KEY_BUTTON, buttonName);

        return intent;
    }

    /**
     * Generates a unique PendingIntent for actions on receiver widgets
     *
     * @param context           any suitable context
     * @param room              Room
     * @param buttonName        name of Button
     * @param uniqueRequestCode unique identifier for different combinations of rooms, receivers and buttons
     * @return PendingIntent
     */
    public static PendingIntent buildRoomWidgetButtonPendingIntent(Context context, Room room, String buttonName,
                                                                   int uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createRoomButtonIntent(room.getName(), buttonName),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent createRoomButtonIntent(String roomName, String buttonName) {
        Intent intent = new Intent();
        intent.setAction(Constants.WIDGET_ACTION_INTENT);
        intent.putExtra(KEY_ROOM, roomName);
        intent.putExtra(KEY_BUTTON, buttonName);

        return intent;
    }

    public static PendingIntent buildSceneWidgetPendingIntent(Context context, Scene scene, int
            uniqueRequestCode) {
        return PendingIntent.getBroadcast(context, uniqueRequestCode, createSceneIntent(scene.getName()), PendingIntent
                .FLAG_UPDATE_CURRENT);
    }

    private static Intent createSceneIntent(String sceneName) {
        Intent intent = new Intent();
        intent.setAction(Constants.WIDGET_ACTION_INTENT);
        intent.putExtra(KEY_SCENE, sceneName);

        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String log = "onReceive: Action: ";
            log += intent.getAction();
            Bundle extras = intent.getExtras();
            log += "{ ";
            if (extras != null) {
                for (String extra : extras.keySet()) {
                    log += extra + "[" + extras.get(extra) + "], ";
                }
            }
            log += " }";
            Log.d(this, log);
        } catch (Exception e) {
            Log.e(e);
        }

        try {
            if (intent.getAction().equals(Constants.WIDGET_ACTION_INTENT)) {
                SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(context);
                // vibrate
                if (sharedPreferencesHandler.getVibrateOnButtonPress()) {
                    VibrationHandler.vibrate(context, sharedPreferencesHandler.getVibrationDuration());
                }

                parseWidgetActionIntent(context, intent);
            } else {
                Log.d("Received unknown intent: " + intent.getAction());
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private void parseWidgetActionIntent(Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey(KEY_ROOM) && extras.containsKey(KEY_RECEIVER) && extras.containsKey(KEY_BUTTON)) {
                    String roomName = extras.getString(KEY_ROOM);
                    String receiverName = extras.getString(KEY_RECEIVER);
                    String buttonName = extras.getString(KEY_BUTTON);

                    IntentReceiver.buildReceiverButtonPendingIntent(context, roomName, receiverName,
                            buttonName, 0).send();
                } else if (extras.containsKey(KEY_ROOM) && extras.containsKey(KEY_BUTTON)) {
                    String roomName = extras.getString(KEY_ROOM);
                    String buttonName = extras.getString(KEY_BUTTON);

                    IntentReceiver.buildRoomButtonPendingIntent(context, roomName, buttonName, 0).send();
                } else if (extras.containsKey(KEY_SCENE)) {
                    String sceneName = extras.getString(KEY_SCENE);

                    IntentReceiver.buildSceneButtonPendingIntent(context, sceneName, 0).send();
                }
            } else {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            Log.e("Error parsing intent!", e);
            Toast.makeText(context, context.getString(R.string.error_parsing_intent), Toast.LENGTH_LONG).show();
        }
    }
}