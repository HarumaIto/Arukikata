package com.hanzyukukobo.arukikata.util.ml

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("LABEL_NAME_CLASH")
class ScopedExecutor(executor: Executor) : Executor {
    private val executor: Executor
    private val shutdown: AtomicBoolean = AtomicBoolean()

    init {
        this.executor = executor
    }

    override fun execute(command: Runnable) {
        // Return early if this object has been shut down.
        if (shutdown.get()) {
            return
        }
        executor.execute {

            // Check again in case it has been shut down in the mean time.
            if (shutdown.get()) {
                return@execute
            }
            command.run()
        }
    }

    /**
     * After this method is called, no runnables that have been submitted or are subsequently
     * submitted will start to execute, turning this executor into a no-op.
     *
     *
     * Runnables that have already started to execute will continue.
     */
    fun shutdown() {
        shutdown.set(true)
    }
}