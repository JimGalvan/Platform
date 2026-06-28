package com.platform.common.exception;

import com.platform.domain.enums.accounts.AccountError;

public final class AccountException extends RuntimeException {

    private final AccountError error;

    public AccountException(AccountError error, String message) {
        super(message);
        this.error = error;
    }

    public AccountError error() {
        return error;
    }
}
