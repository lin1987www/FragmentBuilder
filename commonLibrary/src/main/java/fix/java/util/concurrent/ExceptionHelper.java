package fix.java.util.concurrent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/3/6.
 */
public class ExceptionHelper {
    private static final ArrayList<ExceptionPrinter> mArray = new ArrayList<>();

    public static void addExceptionPrinter(ExceptionPrinter exceptionPrinter) {
        mArray.add(exceptionPrinter);
    }

    public static void removeExceptionPrinter(ExceptionPrinter exceptionPrinter) {
        mArray.remove(exceptionPrinter);
    }

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

    public static void throwRuntimeException(Throwable ex) throws RuntimeException {
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

        for (ExceptionPrinter handler : mArray) {
            handler.printException(taskName, ex);
        }
    }

    public static Throwable getNestedCause(Throwable throwable) {
        try {
            if (throwable.getCause() != null) {
                return getNestedCause(throwable.getCause());
            } else {
                return throwable;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
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

    public interface ExceptionPrinter {
        void printException(String taskName, Throwable ex);
    }
}
