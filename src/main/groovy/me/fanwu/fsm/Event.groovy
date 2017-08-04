package me.fanwu.fsm

import me.fanwu.fsm.exception.InvalidTransitionException

/**
 *  fanwu 编写于 2017/8/3.
 */
@SuppressWarnings("GroovyAssignabilityCheck")
class Event {
    String name
    def success
    Map options
    def transitions = []

    Event(args = [:], String name, Closure c) {
        this.name = name
        success = args.success
        options = args
        if (c) {
            this.with(c)
        }
    }

    def fire(fsm, toState = null, Object... args) {
        def allTrans = transitions.findAll { it.from == fsm.currentState }
        if (!allTrans) {
            throw new InvalidTransitionException("Event ${name} cannot transition from ${fsm.currentState}".toString())
        }

        def nextState = null
        for (transition in allTrans) {
            def tos = new ArrayList()
            tos.add(transitions.to)
            tos = tos.flatten()

            if (toState && !(tos.contains(toState))) {
                continue
            }

            if (transition.canTransition(fsm)) {
                nextState = toState ?: tos.first()
                transition.execute(fsm, *args)
                break
            }
        }
        nextState
    }

    def executeSuccessCallback(fsm, success = null) {
        def callback = success ?: this.success
        switch (callback) {
            case Closure:
                fsm.with(callback)
                break
            case String:
                fsm.invokeMethod(callback, null)
                break
            case List:
                callback.each { executeSuccessCallback(fsm, it) }
                break
        }
    }

    def callAction(action, fsm) {
        def act = options[action]
        switch (act) {
            case Closure:
                fsm.with(act)
                break
            case String:
                fsm.invokeMethod(act, null)
                break
            case List:
                act.each { fsm.invokeMethod(it, null) }
                break
        }
    }

    private void transitions(args) {
        def froms = new ArrayList()
        froms.add(args.from)
        froms.flatten().each {
            args.from = it
            transitions << new Transition(args)
        }
    }

    /**
     * 阻止setter的调用
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    private void setTransitions(transitions) {
    }
}
