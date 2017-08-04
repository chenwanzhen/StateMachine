package me.fanwu.fsm

import me.fanwu.fsm.exception.UndefinedStateException

/**
 *  fanwu 编写于 2017/8/3.
 */
@SuppressWarnings("GroovyAssignabilityCheck")
class StateMachine {
    def initState
    String currentState
    def states = []  //状态集
    def events = [:]

    def createState(args = [:], String name) {
        if (states.find { it == name }) {
            states << new State(args, name)
        }
    }

    def getCurrentState() {
        if (currentState) {
            return currentState
        }
        def stateName = determineStateName(initState)
        def state = stateObjectForState(stateName)

        state.callAction("before_enter", this)
        state.callAction("enter", this)
        this.currentState = stateName
        state.callAction("after_enter", this)
        return stateName
    }

    def setCurrentState(state) {
        currentState = state
    }

    def determineStateName(state) {
        switch (state) {
            case Closure:
                return this.with(state)
            case String:
                return state
        }
    }

    def fmsInitState(initState) {
        this.initState = initState
    }

    def fsmState(args = [:], String state) {
        createState(args, state)
        if (!initState) {
            initState = state
        }
        StateMachine.metaClass."is${states[0].toUpperCase()}${state.substring(1)}" = {
            currentState = state
        }
    }

    def stateObjectForState(String name) {
        def obj = states.find { it == name }
        if (!obj) {
            throw new UndefinedStateException("Sate ${name} dose not exist".toString())
        }
        obj
    }

    def fsmEvent(options = [:], name, Closure transitions) {
        if (!events[name]) {
            events[name] = new Event(options, name, transitions)
        }
        StateMachine.metaClass."fire${name[0].toUpperCase()}${name.substring(1)}" = { Object... args ->
            fireEvent(name, args)
        }
    }

    def fsmEventFired(eventName, oldStateName, newStateName) {
        //override for post-fire callback on success
    }

    def fsmEventFailed(eventName, oldStateName) {
        //override for post-fire callback on failure
    }

    def fireEvent(name, args) {
        def oldState = stateObjectForState(getCurrentState())
        def event = events[name]

        oldState.callAction("exit", this)

        event.callAction("before", this)

        def newStateName = event.fire(this, null, args)

        if (newStateName) {
            def newState = stateObjectForState(newStateName)

            oldState.callAction("beforeExit", this)
            newState.callAction("beforeEnter", this)
            newState.callAction("enter", this)

            this.currentState = newStateName

            oldState.callAction("afterExit", this)
            newState.callAction("afterEnter", this)

            fsmEventFired(name, oldState.name, getCurrentState())
        } else {
            fsmEventFailed(name, oldState.name)
        }

    }


}
