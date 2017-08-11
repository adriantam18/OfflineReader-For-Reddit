package atamayo.offlinereader.Utils.Schedulers;

import io.reactivex.Scheduler;

public interface BaseScheduler {
    Scheduler mainThread();

    Scheduler io();

    Scheduler computation();
}
