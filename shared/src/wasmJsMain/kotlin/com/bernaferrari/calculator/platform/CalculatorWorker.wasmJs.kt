@file:OptIn(ExperimentalWasmJsInterop::class)

package com.bernaferrari.calculator.platform

import com.bernaferrari.calculator.domain.input.CalculatorWork
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

/** Returns true when this entry point is running inside the calculation worker. */
fun initializeCalculatorWorker(): Boolean {
    if (installWorkerHandler { request ->
        runCatching { Json.encodeToString(Json.decodeFromString<CalculatorWork.Request>(request).run()) }.getOrNull()
    }) return true

    val worker = CalculatorWorker()
    CalculatorWork.remote = worker::execute
    return false
}

private class CalculatorWorker {
    private val requests = Mutex()
    private var worker: JsAny? = null

    suspend fun execute(request: CalculatorWork.Request): CalculatorWork.Reply? = requests.withLock {
        withTimeoutOrNull(5_000) {
            suspendCancellableCoroutine { continuation ->
                val active = worker ?: openWorker()?.also { worker = it }
                if (active == null) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }
                continuation.invokeOnCancellation {
                    closeWorker(active)
                    if (worker === active) worker = null
                }
                sendWork(active, Json.encodeToString(request)) { response ->
                    val reply = response?.let { runCatching { Json.decodeFromString<CalculatorWork.Reply>(it) }.getOrNull() }
                    if (reply == null) {
                        closeWorker(active)
                        if (worker === active) worker = null
                    }
                    if (continuation.isActive) continuation.resume(reply)
                }
            }
        }
    }
}

private fun installWorkerHandler(run: (String) -> String?): Boolean = js("""
    {
        if (typeof document !== 'undefined') return false;
        self.onmessage = event => self.postMessage(run(event.data));
        self.postMessage('calcula-worker-ready');
        return true;
    }
""")

private fun openWorker(): JsAny? = js("""
    {
        try {
            const script = document.querySelector('script[src$="calculator.js"]');
            const worker = new Worker(script ? script.src : new URL('calculator.js', document.baseURI));
            const handle = { worker, ready: false, pending: null, failed: false };
            worker.onmessage = event => {
                if (!handle.ready && event.data === 'calcula-worker-ready') {
                    handle.ready = true;
                    if (handle.pending) worker.postMessage(handle.pending.request);
                    return;
                }
                const pending = handle.pending;
                handle.pending = null;
                if (pending) pending.done(event.data);
            };
            worker.onerror = () => {
                handle.failed = true;
                const pending = handle.pending;
                handle.pending = null;
                if (pending) pending.done(null);
            };
            return handle;
        } catch (_) {
            return null;
        }
    }
""")

private fun sendWork(worker: JsAny, request: String, done: (String?) -> Unit): Unit = js("""
    {
        if (worker.failed) {
            done(null);
            return;
        }
        worker.pending = { request, done };
        if (worker.ready) worker.worker.postMessage(request);
    }
""")

private fun closeWorker(worker: JsAny): Unit = js("{ worker.pending = null; worker.worker.terminate(); }")
