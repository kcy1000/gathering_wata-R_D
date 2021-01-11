package geo2.lbsp.ible.internal;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class ThreadedHandler {
    private final Handler handler;
    private final HandlerThread handlerThread;
    private boolean didQuit;

    public ThreadedHandler(String threadName, int priority) {
        this.handlerThread = new HandlerThread(threadName, priority);
        this.handlerThread.start();
        this.handler = new Handler(this.handlerThread.getLooper());
        this.didQuit = false;
    }

    public void quit() {
        this.handlerThread.quit();
        this.didQuit = true;
    }

    public void post(Runnable runnable) {
        if(!this.didQuit) {
            this.handler.post(runnable);
        }

    }

    public Looper getLooper() {
        return this.handlerThread.getLooper();
    }

    public void removeCallbacks(Runnable runnable) {
        this.handler.removeCallbacks(runnable);
    }
}
