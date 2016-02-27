package com.example.minu.voicemailtest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TService extends Service{

    MediaRecorder recorder;
    File audiofile;
    private boolean recordstarted = false;
    private static final String TAG = "CallRecordTest";

    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private CallBr br_call;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroy");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);

        return START_STICKY;
    }

    //Step 3: Wait for receiving broadcast
    public class CallBr extends BroadcastReceiver {
        Bundle bundle;
        String state;
        String inCall, outCall;
        public boolean wasRinging = false;
        boolean shouldRecord = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_IN)) {
                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        inCall = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        wasRinging = true;
                        Toast.makeText(context, "IN : " + inCall, Toast.LENGTH_LONG).show();
                        Log.i(TAG, "Incoming" + inCall);

                        //start(Answering the phone)
                        Context context2 = getBaseContext();

                        // Let the phone ring for a set delay
                        SharedPreferences sharedPref = getSharedPreferences("VoiceMailDelayTime", Context.MODE_PRIVATE);
                        String title = sharedPref.getString("delayTime", "10");
                        String[] delayTime = title.split(" ");
                        Log.i(TAG, "Delay Time: " + delayTime[0]);
                        try {
                            Thread.sleep(Integer.parseInt(delayTime[0]) * 1000);
                            Log.i(TAG, "Delay Starts");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Log.i(TAG, "Delay ends");

                        // Make sure the phone is still ringing
                        TelephonyManager tm = (TelephonyManager) context2.getSystemService(Context.TELEPHONY_SERVICE);
                        if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
                            return;
                        }

                        // Answer the phone
                        try {
                            Log.i(TAG, "Before Answering");
                            answerPhoneAidl(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("AutoAnswer", "Error trying to answer using telephony service.  Falling back to headset.");
                            answerPhoneHeadsethook(context);
                        }
                        //end

                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (wasRinging == true && shouldRecord == true) {
                            Toast.makeText(context, "ANSWERED", Toast.LENGTH_LONG).show();
                            Log.i(TAG, "Answered");

                            Context context3 = getBaseContext();

//                            //Playing Audio
//                            MediaPlayer mp = new MediaPlayer();
//                            try {
//                                AudioManager audioManager = (AudioManager)context3.getSystemService(Context.AUDIO_SERVICE);
//                                audioManager.setMode(AudioManager.MODE_IN_CALL);
//                                audioManager.setSpeakerphoneOn(false);
//                                mp.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
//                                mp.setDataSource(Environment.getExternalStorageDirectory().getPath() + "/test.mp4");
//                                mp.prepare();
//                                mp.start();
//                                Log.i(TAG, "Playing");
//                            } catch (IOException e) {
//                                Log.e(TAG, "prepare() failed");
//                            }
//                            long totalDuration = mp.getDuration();
//
//                            //Delay till message finishes
//                            try {
//                                Thread.sleep(totalDuration);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            mp.release();
//                            mp = null;
//
//                            //Make sure that phone is still receiving
//                            TelephonyManager tm = (TelephonyManager) context3.getSystemService(Context.TELEPHONY_SERVICE);
//                            if (tm.getCallState() != TelephonyManager.CALL_STATE_OFFHOOK) {
//                                return;
//                            }

                            //Recording the call
                            String out = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
                            File sampleDir = new File(Environment.getExternalStorageDirectory(), "/VoiceMailTest");
                            if (!sampleDir.exists()) {
                                sampleDir.mkdirs();
                            }
                            String file_name = "Record";
                            try {
                                audiofile = File.createTempFile(file_name, ".amr", sampleDir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath();

                            recorder = new MediaRecorder();
                            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);

                            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                            recorder.setOutputFile(audiofile.getAbsolutePath());
                            try {
                                recorder.prepare();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            recorder.start();
                            Log.i(TAG, "Recording Started");
                            recordstarted = true;

                        }
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        wasRinging = false;
                        shouldRecord = false;
                        Toast.makeText(context, "REJECTED", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "Rejected");
                        if (recordstarted) {
                            recorder.stop();
                            Log.i(TAG, "Recording Stopped");
                            recordstarted = false;
                        }
                    }
                }
            } else if (intent.getAction().equals(ACTION_OUT)) {
                if ((bundle = intent.getExtras()) != null) {
                    outCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    Toast.makeText(context, "OUT : " + outCall, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Outgoing");
                }
            }
        }

        //start
        private void answerPhoneHeadsethook(Context context) {
            // Simulate a press of the headset button to pick up the call
            Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
            buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
            context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

            // froyo and beyond trigger on buttonUp instead of buttonDown
            Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
            buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
            context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
        }

        @SuppressWarnings("unchecked")
        private void answerPhoneAidl(Context context) throws Exception {
            // Set up communication with the telephony service
            Log.i(TAG, "Entered the telephony service and trying to answer");
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            ITelephony telephonyService;
            telephonyService = (ITelephony)m.invoke(tm);

            shouldRecord = true;

            // Silence the ringer and answer the call
            telephonyService.silenceRinger();
            telephonyService.answerRingingCall();
        }
        //end

    }
}
