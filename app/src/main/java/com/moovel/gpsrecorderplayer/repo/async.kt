package com.moovel.gpsrecorderplayer.repo

import android.os.Looper
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private class AsyncExecutor : ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, WORK_QUEUE, THREAD_FACTORY) {
    companion object {

        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private val CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4))
        private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
        private const val KEEP_ALIVE_SECONDS = 30L
        private val WORK_QUEUE = LinkedBlockingQueue<Runnable>(128)

        private val THREAD_FACTORY = object : ThreadFactory {
            private val mCount = AtomicInteger(1)

            override fun newThread(r: Runnable): Thread {
                return Thread(r, "AsyncThread #" + mCount.getAndIncrement())
            }
        }
    }
}

private val executor = AsyncExecutor()

internal fun async(block: () -> Unit) = executor.execute(block)

internal fun isMainThread(): Boolean = Looper.getMainLooper().thread == Thread.currentThread()
internal fun Thread.isCurrentThread(): Boolean = this == Thread.currentThread()
