package me.fanwu.fsm.exception

/**
 *  fanwu 编写于 2017/8/3.
 */
class InvalidTransitionException extends Exception {
    InvalidTransitionException(String cause) {
        super(cause)
    }
}
