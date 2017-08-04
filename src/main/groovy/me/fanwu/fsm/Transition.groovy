package me.fanwu.fsm

/**
 *  fanwu 编写于 2017/8/3.
 */
@SuppressWarnings("GroovyAssignabilityCheck")
class Transition {
    def from
    def to
    def guard
    def onTransition
    def opts

    Transition(args) {
        (from, to, guard, onTransition) = [args.from, args.to, args.guard, args.onTransition]
        opts = args
    }

    boolean equals(other) {
        (from == other.from) && (to == other.to)
    }

    boolean canTransition(fsm) {
        switch (guard) {
            case Closure:
                return fsm.with(guard)
            case String:
                return fsm.invokeMethod(guard, null)
            default:
                return true
        }
    }

    def execute(fsm, Object... args) {
        switch (onTransition) {
            case Closure:
                onTransition.delegate = fsm
                onTransition.call(fsm, *args)
                break
            case String:
                fsm.invokeMethod(onTransition, args)
                break
        }
    }
}
