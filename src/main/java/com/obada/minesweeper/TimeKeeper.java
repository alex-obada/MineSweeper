package com.obada.minesweeper;

import java.util.Timer;
import java.util.TimerTask;

public class TimeKeeper {
    private Timer timer = new Timer();
    private int currentTime = 0;
    private boolean stop = false;
    private final ITimeKeeperCallBack callBack;

    TimeKeeper(ITimeKeeperCallBack callBack) {
        this.callBack = callBack;
        resetTimer();
    }

    public int getTime() {
        return currentTime;
    }

    public void stop() {
        if(stop) return;

        stop = true;
        timer.cancel();
    }

    public void resetTimer() {
        currentTime = 0;
        stop = false;
        (timer = new Timer()).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!stop)
                    callBack.run(currentTime++);
            }
        }, 0, 1000);
    }
}
