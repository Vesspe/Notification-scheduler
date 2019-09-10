package com.example.notificationscheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private RadioGroup networkOptions;
    private JobScheduler jobScheduler;
    private Switch mDeviceIdleSwitch;
    private Switch mDeviceChargingSwitch;
    private SeekBar mSeekBar;
    private Switch mPeriodicSwitch;
    private TextView label;
    private final int JOB_ID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        networkOptions = (RadioGroup)findViewById(R.id.networkOptions);
        mDeviceIdleSwitch = (Switch)findViewById(R.id.idleSwitch);
        mDeviceChargingSwitch = (Switch)findViewById(R.id.chargingSwitch);
        mSeekBar = (SeekBar)findViewById(R.id.seekBar);
        mPeriodicSwitch = (Switch)findViewById(R.id.periodicSwitch);
        label = (TextView)findViewById(R.id.seekBarLabel);
        final TextView seekBarProgress = (TextView)findViewById(R.id.seekBarProgress);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress>0)
                    seekBarProgress.setText(String.valueOf(progress) + "s");
                else
                    seekBarProgress.setText("Not Set");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mPeriodicSwitch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    label.setText(getString(R.string.periodic_interval));
                }else{
                    label.setText(getString(R.string.override_deadline));
                }
            }
        });


    }

    public void scheduleJob(View view) {
        int selectedNetworkOptionID = networkOptions.getCheckedRadioButtonId();
        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        int seekBarInteger = mSeekBar.getProgress();
        boolean seekBarSet = seekBarInteger > 0;
        jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        boolean constrainSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE)
                || mDeviceChargingSwitch.isChecked() || mDeviceIdleSwitch.isChecked()
                || seekBarSet;

        switch(selectedNetworkOptionID) {
            case R.id.noNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.anyNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.wifiNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        ComponentName serviceName = new ComponentName(getPackageName(),
                NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,serviceName);
        builder.setRequiredNetworkType(selectedNetworkOption);
        builder.setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked());
        builder.setRequiresCharging(mDeviceChargingSwitch.isChecked());
        if(mPeriodicSwitch.isChecked()) {


            if (seekBarSet) {
                builder.setPeriodic(seekBarInteger * 1000);
            }else{
                Toast.makeText(MainActivity.this, "Please set a periodic interval", Toast.LENGTH_SHORT).show();
            }
        }else{
            if(seekBarSet){
                builder.setOverrideDeadline(seekBarInteger * 1000);
            }
        }
            if (constrainSet) {
                JobInfo myJobInfo = builder.build();
                jobScheduler.schedule(myJobInfo);
                Toast.makeText(this, "Job scheduled!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "something went wrong ;/", Toast.LENGTH_SHORT).show();
            }



    }

    public void cancelJobs(View view) {
        if(jobScheduler!=null)
        {
            jobScheduler.cancelAll();
            jobScheduler = null;
            Toast.makeText(this,"Jobs are canceled!", Toast.LENGTH_SHORT).show();
        }
    }
}
