package cn.leaf.leafsftp.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SFTPWork extends Worker {

    public SFTPWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return null;
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }
}
