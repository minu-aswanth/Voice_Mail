package com.example.minu.voicemailtest;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    private static final String TAG = "CallRecordTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File sampleDir = new File(Environment.getExternalStorageDirectory(), "/VoiceMailTest");
        if(sampleDir.exists()) {
            ArrayList<String> FilesInFolder = GetFiles(Environment.getExternalStorageDirectory() + "/VoiceMailTest");
            if(FilesInFolder != null) {
                ListView lv = (ListView) findViewById(R.id.minusListView);
                ListAdapter la = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, FilesInFolder);

                lv.setAdapter(la);

                lv.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                String path = Environment.getExternalStorageDirectory().getPath() + "/VoiceMailTest/" + String.valueOf(parent.getItemAtPosition(position));
                                Intent intent2 = new Intent();
                                intent2.setAction(android.content.Intent.ACTION_VIEW);
                                File file = new File(path);
                                intent2.setDataAndType(Uri.fromFile(file), "audio/*");
                                startActivity(intent2);
                            }
                        }
                );

                lv.setOnItemLongClickListener(
                        new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                String path = Environment.getExternalStorageDirectory().getPath() + "/VoiceMailTest/" + String.valueOf(adapterView.getItemAtPosition(i));
                                File file = new File(path);
                                file.delete();
                                Context context = view.getContext();
                                ArrayList<String> FilesInFolder = GetFiles(Environment.getExternalStorageDirectory() + "/VoiceMailTest");
                                if(FilesInFolder != null) {
                                    ListView lv = (ListView) findViewById(R.id.minusListView);
                                    ListAdapter la = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, FilesInFolder);
                                    lv.setAdapter(la);
                                    Toast.makeText(context, "Successfully deleted voice message", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    ListView lv = (ListView) findViewById(R.id.minusListView);
                                    String[] DefaultMessage = {"Your Voice Messages will be displayed here"};
                                    ListAdapter la = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, DefaultMessage);
                                    lv.setAdapter(la);
                                }
                                return true;
                            }
                        }
                );
            }
            else{
                ListView lv = (ListView) findViewById(R.id.minusListView);
                String[] DefaultMessage = {"Your Voice Messages will be displayed here"};
                ListAdapter la = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, DefaultMessage);
                lv.setAdapter(la);
            }
        }

//        //Step 1: Make device as Admin Device
//        try {
//            // Initiate DevicePolicyManager.
//            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//            mAdminName = new ComponentName(this, DeviceAdminDemo.class);
//
//            if (!mDPM.isAdminActive(mAdminName)) {
//                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
//                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
//                startActivityForResult(intent, REQUEST_CODE);
//            } else {
//                Intent intent = new Intent(MainActivity.this,TService.class);
//                startService(intent);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        Intent intent = new Intent(MainActivity.this,TService.class);
        startService(intent);
    }
//
//    //Step 2: When done, start the service
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (REQUEST_CODE == requestCode) {
//            Intent intent = new Intent(MainActivity.this, TService.class);
//            startService(intent);
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SharedPreferences sharedPref = getSharedPreferences("VoiceMailDelayTime", Context.MODE_PRIVATE);
        String title = sharedPref.getString("delayTime", "none");
        //For the first time
        if(title.equals("none")){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("delayTime", "10 seconds");
            editor.apply();
            title = sharedPref.getString("delayTime", "none");
        }
        for(int i=0; i<4; i++){
            if(menu.getItem(i).getTitle().toString().equals(title)){
                menu.getItem(i).setChecked(true);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sharedPref = getSharedPreferences("VoiceMailDelayTime", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("delayTime", item.getTitle().toString());
        editor.apply();

        switch(item.getItemId()) {
            case R.id.menu_six:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                return true;
            case R.id.menu_eight:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                return true;
            case R.id.menu_ten:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                return true;
            case R.id.menu_twelve:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length == 0)
            return null;
        else {
            for (int i=0; i<files.length; i++)
                MyFiles.add(files[i].getName());
        }

        return MyFiles;
    }

}