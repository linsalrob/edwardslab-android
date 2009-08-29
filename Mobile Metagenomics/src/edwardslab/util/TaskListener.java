package edwardslab.util;
// Task interface written by Matthias Käppler
// http://brainflush.wordpress.com/author/mkaeppler/

public interface TaskListener<ResultType> {

    void onTaskFinished(Task<ResultType> task);
}