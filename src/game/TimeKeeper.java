package game;

import java.util.Timer;
import java.util.TimerTask;

public class TimeKeeper {
    private final Timer timer = new Timer();
    private int currentTime = 0;
    private boolean stop = false;
    private final ITimeKeeperCallBack callBack;

    TimeKeeper(ITimeKeeperCallBack callBack) {
        this.callBack = callBack;
        resetTimer();
    }

    public void stop() {
        if(!stop)
            stop = true;
    }

    public void resetTimer() {
        currentTime = 0;
        stop = false;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!stop)
                    callBack.run(currentTime++);
                else
                    this.cancel();
            }
        }, 0, 1000);
    }
}
