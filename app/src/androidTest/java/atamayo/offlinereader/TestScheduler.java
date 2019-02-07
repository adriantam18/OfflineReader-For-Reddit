package atamayo.offlinereader;

import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;


public class TestScheduler implements BaseScheduler {

    @Override
    public Scheduler mainThread() {
        return Schedulers.trampoline();
    }

    @Override
    public Scheduler io() {
        return Schedulers.trampoline();
    }

    @Override
    public Scheduler computation() {
        return Schedulers.trampoline();
    }
}
