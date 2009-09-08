package edwardslab.util;
import java.util.concurrent.Callable;

public interface QueryableCallable<ResultType> extends Callable<ResultType>{
	public ResultType postResult() throws Exception;
}
