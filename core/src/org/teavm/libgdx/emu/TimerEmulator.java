package org.teavm.libgdx.emu;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import org.teavm.classlib.java.util.TTimer;
import org.teavm.classlib.java.util.TTimerTask;
import org.teavm.jso.browser.TimerHandler;
import org.teavm.jso.browser.Window;
import org.teavm.libgdx.plugin.Annotations.Emulate;
import org.teavm.libgdx.plugin.Annotations.Replace;

/** Executes tasks in the future on the main loop thread.
 * @author Nathan Sweet */
@Replace(Timer.class)
public class TimerEmulator {
    static private final int CANCELLED = -1;
    static private final int FOREVER = -2;
    static TTimer thread = new TTimer();

    /** Timer instance for general application wide usage. Static methods on Timer make convenient use of this instance. */
    static TimerEmulator instance = new TimerEmulator();

    static public TimerEmulator instance () {
        if (instance == null) {
            instance = new TimerEmulator();
        }
        return instance;
    }

    private final Array<Task> tasks = new Array<>(false, 8);

    public TimerEmulator () {

    }

    /** Schedules a task to occur once as soon as possible, but not sooner than the start of the next frame. */
    public static Timer.Task postTask (Timer.Task task) {
        return scheduleTask(task, 0, 0, 0);
    }

    /** Schedules a task to occur once after the specified delay. */
    public static Timer.Task scheduleTask (Timer.Task task, float delaySeconds) {
        return scheduleTask(task, delaySeconds, 0, 0);
    }

    /** Schedules a task to occur once after the specified delay and then repeatedly at the specified interval until cancelled. */
    public static Timer.Task scheduleTask (Timer.Task task, float delaySeconds, float intervalSeconds) {
        return scheduleTask(task, delaySeconds, intervalSeconds, FOREVER);
    }

    /** Schedules a task to occur once after the specified delay and then a number of additional times at the specified interval. */
    public static Timer.Task scheduleTask (Timer.Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
        //if (task.repeatCount != CANCELLED) throw new IllegalArgumentException("The same task may not be scheduled twice.");

        int[] iid = {0};

        iid[0] = Window.setTimeout(task::run, (int) (delaySeconds * 1000));

        return task;
    }

    /** Stops the timer, tasks will not be executed and time that passes will not be applied to the task delays. */
    public void stop () {

    }

    /** Starts the timer if it was stopped. */
    public void start () {

    }

    /** Cancels all tasks. */
    public void clear () {
        synchronized (tasks) {
            for (int i = 0, n = tasks.size; i < n; i++)
                tasks.get(i).cancel();
            tasks.clear();
        }
    }

    long update (long timeMillis, long waitMillis) {
        return 0;
    }

    /** Adds the specified delay to all tasks. */
    public void delay (long delayMillis) {

    }

    static void wake () {

    }

    static public Timer.Task post (Timer.Task task) {
        return instance().postTask(task);
    }

    static public Timer.Task schedule (Timer.Task task, float delaySeconds) {
        return instance().scheduleTask(task, delaySeconds);
    }

    static public Timer.Task schedule (Timer.Task task, float delaySeconds, float intervalSeconds) {
        return instance().scheduleTask(task, delaySeconds, intervalSeconds);
    }

    static public Timer.Task schedule (Timer.Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
        return instance().scheduleTask(task, delaySeconds, intervalSeconds, repeatCount);
    }
}
