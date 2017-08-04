package me.fanwu.fsm

/**
 *  fanwu 编写于 2017/8/3.
 */
class State {
    String name
    Map options

    State(args = [:], String name) {
        options = args
        this.name = name
    }

    boolean equals(def other) {
        if (other.respondsTo('getName')) {
            return name == other.name
        }
        name == other
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    def callAction(action, fsm) {
        def act = options[action]
        switch (act) {
            case Closure:
                fsm.with(act)
                break
            case String:
                fsm.invokeMethod(act, null)
                break
        }
    }
}
