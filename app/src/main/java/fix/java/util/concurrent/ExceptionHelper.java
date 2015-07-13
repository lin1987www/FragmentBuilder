package fix.java.util.concurrent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Administrator on 2015/3/6.
 */
public class ExceptionHelper {
    public static void throwException(String taskName, Throwable ex) throws Exception {
        printException(taskName, ex);
        Exception e;
        if (ex instanceof Exception) {
            e = (Exception) ex;
        } else {
            e = new Exception(ex);
        }
        throw e;
    }

    public static void throwRuntimeException(String taskName, Throwable ex) throws RuntimeException {
        printException(taskName, ex);
        RuntimeException e;
        if (ex instanceof RuntimeException) {
            e = (RuntimeException) ex;
        } else {
            e = new RuntimeException(ex);
        }
        throw e;
    }

    public static void printException(String taskName, Throwable ex) {
        Thread thread = Thread.currentThread();
        System.err.println(String.format("Exception in thread \"%s\" %s %s", thread.toString(), ex.toString(), taskName));
        ex.printStackTrace();
    }

    public static Throwable getNestedCause(Throwable throwable) {
        if (throwable.getCause() != null) {
            return getNestedCause(throwable.getCause());
        } else {
            return throwable;
        }
    }

    public static String getPrintStackTraceString(Throwable ex) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        ex.printStackTrace(printWriter);
        String stackTrace = result.toString();
        printWriter.close();
        return stackTrace;
    }
}
