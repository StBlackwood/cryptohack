package com.blackwood.cryptohack;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;



/**
 * Created by blackwood on 1/5/18.
 */


public class JobSchedulerService extends JobService {
    private static final String TAG = "JobSchedulerService";
    private RequestHandling requestHandling;

    @Override
    public void onCreate() {
        super.onCreate();
        requestHandling = new RequestHandling(this);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob:");
        requestHandling.executeTask();
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            scheduleRefresh();

            //Call Job Finished
            jobFinished(params, false );

            return true;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob:");
        return false;
    }

    private void scheduleRefresh() {
        JobScheduler mJobScheduler = (JobScheduler)
                getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1,
                new ComponentName(getPackageName(),
                        JobSchedulerService.class.getName()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMinimumLatency(5000);
        else
            builder.setPeriodic(5000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        if (mJobScheduler.schedule(builder.build()) <= 0) {
            Log.e(TAG, "onCreate: Some error while scheduling the job");
        }
    }
}
