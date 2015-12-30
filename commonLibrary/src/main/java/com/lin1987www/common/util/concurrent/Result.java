package com.lin1987www.common.util.concurrent;

public class Result<T> {
	public final T result;

	public final Throwable error;

	public boolean isSuccess() {
		return error == null;
	}

	public boolean isSuccessNotNull() {
		return isSuccess() && isNotNull();
	}

	public boolean isNull() {
		return result == null;
	}

	public boolean isNotNull() {
		return !isNull();
	}

	public boolean isError() {
		return !isSuccess();
	}

	public Result(Throwable error) {
		this(null, error);
	}

	public Result(T result) {
		this(result, null);
	}

	public Result(Result<T> result) {
		this(result.result, result.error);
	}

	public Result(T result, Throwable error) {
		this.result = result;
		this.error = error;
	}

	public static <T> Result<T> makeResultIsNullError() {
		Result<T> result = Result.error(new NullPointerException());
		return result;
	}

	public static <T> Result<T> success(T result) {
		return new Result<T>(result);
	}

	public static <T> Result<T> error(Throwable error) {
		return new Result<T>(error);
	}
}