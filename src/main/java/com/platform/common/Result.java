package com.platform.common;

public final class Result<T> {

    private final T value;
    private final int status;
    private final String code;
    private final String message;

    private Result(T value, int status, String code, String message) {
        this.value = value;
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public static <T> Result<T> ok(T value) {
        return new Result<>(value, 200, null, null);
    }

    public static <T> Result<T> created(T value) {
        return new Result<>(value, 201, null, null);
    }

    public static <T> Result<T> noContent() {
        return new Result<>(null, 204, null, null);
    }

    public static <T> Result<T> badRequest(String message) {
        return badRequest("BAD_REQUEST", message);
    }

    public static <T> Result<T> badRequest(String code, String message) {
        return new Result<>(null, 400, code, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return unauthorized("UNAUTHORIZED", message);
    }

    public static <T> Result<T> unauthorized(String code, String message) {
        return new Result<>(null, 401, code, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return forbidden("FORBIDDEN", message);
    }

    public static <T> Result<T> forbidden(String code, String message) {
        return new Result<>(null, 403, code, message);
    }

    public static <T> Result<T> notFound(String message) {
        return notFound("NOT_FOUND", message);
    }

    public static <T> Result<T> notFound(String code, String message) {
        return new Result<>(null, 404, code, message);
    }

    public static <T> Result<T> conflict(String message) {
        return conflict("CONFLICT", message);
    }

    public static <T> Result<T> conflict(String code, String message) {
        return new Result<>(null, 409, code, message);
    }

    public static <T> Result<T> unprocessableEntity(String message) {
        return unprocessableEntity("UNPROCESSABLE_ENTITY", message);
    }

    public static <T> Result<T> unprocessableEntity(String code, String message) {
        return new Result<>(null, 422, code, message);
    }

    public static <T> Result<T> internalError(String message) {
        return internalError("INTERNAL_ERROR", message);
    }

    public static <T> Result<T> internalError(String code, String message) {
        return new Result<>(null, 500, code, message);
    }

    public <U> Result<U> asError() {
        return new Result<>(null, status, code, message);
    }

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    public T getValue() {
        return value;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
