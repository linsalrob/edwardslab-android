package edwardslab.util;

//Task interface written by Matthias Käppler
//http://brainflush.wordpress.com/author/mkaeppler/

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

@SuppressWarnings("unchecked")
public final class Task<ResultType> {

    private volatile TaskListener<ResultType> callback;

    private volatile State state;

    private boolean showProgressDialog;

    private int dialogTitleResourceId, dialogMessageResourceId;

    private ProgressDialog progressDialog;

    private ResultType result;

    private Exception error;

    private Handler finishedHandler = new Handler();
    
    private Handler postedHandler = new Handler();

    private int taskId;

    public static enum State {
        NOT_STARTED, RUNNING, COMPLETED, CANCELED
    }

    private static final HashMap<String, HashMap<Integer, Task>> tasks;

    static {
        tasks = new HashMap<String, HashMap<Integer, Task>>();
    }

    private Task(TaskListener<ResultType> callback, int taskId) {
        this.callback = callback;
        this.taskId = taskId;
        this.state = State.NOT_STARTED;
    }

    /**
     * Create a new task instance with the given task ID, or return an existing
     * task for this ID.
     * 
     * @param <ResultType>
     *            The type of the task's result
     * @param context
     *            The caller
     * @param callback
     *            The receiver (can be the same as the caller)
     * @param taskId
     *            The task's ID. This can be any number.
     * @return An existing or new task instance
     */
    public static <ResultType> Task<ResultType> getOrCreate(Context context,
            TaskListener<ResultType> callback, int taskId) {
        synchronized (tasks) {
            String caller = context.getClass().getCanonicalName();
            Task<ResultType> task = null;
            HashMap<Integer, Task> activeTasks = tasks.get(caller);
            // if the caller is resuming and already started this task,
            // try to retrieve it from the task list
            if (activeTasks != null) {
                task = activeTasks.get(taskId);
            }
            if (task == null) {
                // the caller did not yet start this task
                task = new Task<ResultType>(callback, taskId);
            } else if (task.state == State.COMPLETED) {
                // the task exists, has completed and has not yet been claimed
                // by the caller (because the caller was paused); remove it
                // from the caller's task map
                activeTasks.remove(task);
            }
            // re-attach the caller as a listener, can never hurt
            task.registerCallback(callback);
            return task;
        }
    }

    /**
     * Create a new task instance with the given task ID, or return an existing
     * task for this ID. <b>This overloaded version assumes that the calling
     * context is also the {@link TaskListener}, so use with care, or a class
     * cast exception may be thrown.</b>
     * 
     * @param <ResultType>
     *            The type of the task's result
     * @param context
     *            The caller
     * @param taskId
     *            The task's ID. This can be any number.
     * @return An existing or new task instance
     */
    public static <ResultType> Task<ResultType> getOrCreate(Context context,
            int taskId) {
        TaskListener<ResultType> callback = (TaskListener<ResultType>) context;
        return getOrCreate(context, callback, taskId);
    }

    /**
     * @param context
     *            The caller
     * @return all task instances for the given caller
     */
    public static Collection<Task> getAll(Context context) {
        synchronized (tasks) {
            String caller = context.getClass().getCanonicalName();
            HashMap<Integer, Task> callerTasks = tasks.get(caller);
            if (callerTasks != null) {
                return callerTasks.values();
            } else {
                return new ArrayList<Task>();
            }
        }
    }

    /**
     * @param context
     *            the caller
     * @return the number of tasks registered for the caller
     */
    public static int getTaskCount(Context context) {
        synchronized (tasks) {
            String caller = context.getClass().getCanonicalName();
            Map<Integer, Task> callerTasks = tasks.get(caller);
            if (callerTasks == null) {
                return 0;
            } else {
                return callerTasks.size();
            }
        }
    }

    @Override
    public int hashCode() {
        return taskId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Task)) {
            return false;
        }
        Task<ResultType> that = (Task<ResultType>) obj;
        return this.taskId == that.taskId;
    }
    
    /**
     * Executes the task. When the task completes and the callback is still
     * registered, the result will be posted to the caller's thread. Otherwise,
     * the result will be kept in memory until claimed by the caller. If a
     * {@link ProgressDialog} has been registered using
     * {@link setProgressDialog}, it will be shown while the QueryableCallable is
     * executing.
     * 
     * @param context
     *            The caller
     * @param QueryableCallable
     *            The QueryableCallable to run for this task
     */
    public void run(Context context, final QueryableCallable<ResultType> QueryableCallable) {

        final String caller = context.getClass().getCanonicalName();
        state = State.RUNNING;

        // publish this task and its new state
        synchronized (tasks) {
            HashMap<Integer, Task> activeTasks = tasks.get(caller);
            if (activeTasks == null) {
                activeTasks = new HashMap<Integer, Task>();
                tasks.put(caller, activeTasks);
            }
            activeTasks.put(taskId, this);
        }

        if (showProgressDialog) {
            spawnProgressDialog(context);
        }

        String threadName = context.getClass().getSimpleName() + "-Task["
                + taskId + "]";
        new Thread(new Runnable() {

            public void run() {
                ResultType result = null;
                Exception error = null;
                try {
                    result = QueryableCallable.call();
                } catch (Exception e) {
                    error = e;
                }

                final ResultType finalResult = result;
                final Exception finalError = error;

                if (state != State.CANCELED) {
                    finishedHandler.post(new Runnable() {

                        public void run() {
                            onTaskFinished(caller, finalResult, finalError);
                        }
                    });
                }
            }
        }, threadName).start();
    }

    public void post(Context context, final QueryableCallable<ResultType> queryableCallable) {
    	final String caller = context.getClass().getCanonicalName();
        try {
			result = queryableCallable.postResult();
		} catch (Exception e) {
			error = e;
		}
		
		final ResultType finalResult = result;
		final Exception finalError = error;
		
		if (state != State.CANCELED) {
            postedHandler.post(new Runnable() {

                public void run() {
                    onTaskPosted(caller, finalResult, finalError);
                }
            });
        }
    }
    
    public void bringToFront(Context context) {
        if (!running()) {
            return;
        }

        if (showProgressDialog) {
            spawnProgressDialog(context);
        }
    }

    private void spawnProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getResources().getString(
                dialogTitleResourceId));
        progressDialog.setMessage(context.getResources().getString(
                dialogMessageResourceId));
        progressDialog.show();
    }

    /**
     * Cancels this task. Regardless of the task's outcome, no result will be
     * posted back to the caller.
     * 
     * @param context
     *            The caller
     */
    public void cancel(Context context) {
        synchronized (tasks) {
            String caller = context.getClass().getCanonicalName();
            this.state = State.CANCELED;
            Map<Integer, Task> callerTasks = tasks.get(caller);
            if (callerTasks != null) {
                callerTasks.remove(taskId);
            }
        }
    }

    /**
     * Cancels all tasks for the given caller.
     * 
     * @see cancel
     * @param context
     *            the caller
     */
    public static void cancelAll(Context context) {
        synchronized (tasks) {
            String caller = context.getClass().getCanonicalName();
            Map<Integer, Task> callerTasks = tasks.get(caller);
            if (callerTasks == null) {
                return;
            }
            Collection<Task> taskList = getAll(context);
            for (Task task : taskList) {
                task.state = State.CANCELED;
                callerTasks.remove(task.taskId);
            }
        }
    }

    private void onTaskFinished(String caller, ResultType result,
            Exception error) {

        // lock the tasks object, so no thread can see a result without the
        // COMPLETED status being set
        synchronized (tasks) {
            this.state = State.COMPLETED;
            this.result = result;
            this.error = error;
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (callback != null) {
            callback.onTaskFinished(this);
            synchronized (tasks) {
                Map<Integer, Task> callerTasks = tasks.get(caller);
                if (callerTasks != null) {
                    callerTasks.remove(taskId);
                }
            }
        }
    }

    private void onTaskPosted(String caller, ResultType result,
            Exception error) {

        // lock the tasks object, so no thread can see a result without the
        // COMPLETED status being set
        synchronized (tasks) {
            this.state = state.RUNNING;
            this.result = result;
            this.error = error;
        }

        if (callback != null) {
            callback.onTaskPosted(this);
        }
    }
    
    /**
     * @return true if the task is still running
     */
    public boolean running() {
        return state == State.RUNNING;
    }

    /**
     * @return true if the task has completed running (either successfully or in
     *         error).
     */
    public boolean completed() {
        return state == State.COMPLETED;
    }

    /**
     * @return true if the task has been canceled.
     */
    public boolean canceled() {
        return state == State.CANCELED;
    }

    /**
     * @return the task's current state
     * @see Task.State
     */
    public State state() {
        return state;
    }

    /**
     * @return the result of the task's operation, if completed successfully
     *         (otherwise null)
     */
    public ResultType getResult() {
        return this.result;
    }

    /**
     * @return the exception that was thrown during the task's operation, if
     *         completed in error (otherwise null)
     */
    public Exception getError() {
        return this.error;
    }

    /**
     * @return whether the task completed in error, i.e. an exception was thrown
     * @see getError
     */
    public boolean failed() {
        return this.error != null;
    }

    /**
     * @return the tasks's ID as provided by the caller when creating it
     */
    public int getTaskId() {
        return this.taskId;
    }

    /**
     * Subscribes the given listener to receive task events such as task
     * completion.
     * 
     * @param callback
     *            the listener
     */
    public void registerCallback(TaskListener<ResultType> callback) {
        this.callback = callback;
    }

    /**
     * Unsubscribes the listener from task events.
     */
    public void unregisterCallback() {
        this.callback = null;
    }

    /**
     * Sets a progress dialog to show while the task is running
     * 
     * @param progressDialog
     *            the dialog
     */
    public void setProgressDialog(int dialogTitleResourceId,
            int dialogMessageResourceId) {
        this.dialogTitleResourceId = dialogTitleResourceId;
        this.dialogMessageResourceId = dialogMessageResourceId;
        this.showProgressDialog = true;
    }
}
