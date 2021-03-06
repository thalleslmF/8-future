// Copyright © FunctionalKotlin.com 2017. All rights reserved.

import kotlinx.coroutines.experimental.*

typealias FutureTask<A> = Deferred<A>

class Future<out A>(val task: FutureTask<A>) {
    companion object
}

fun <A> Future.Companion.pure(a: A): Future<A> =
    Future(async(CommonPool) { a })

fun <A> asyncFuture(getValue: () -> A): Future<A> =
    Future(async(CommonPool) {
        getValue()
    })

fun <A> Future<A>.runAsync(onComplete: (A) -> Unit) {
    launch(CommonPool) {
        onComplete(task.await())
    }
}

fun <A> Future<A>.runSync(): A =
    runBlocking { this@runSync.task.await() }

fun <A, B> Future<A>.map(transform: (A) -> B): Future<B> =
    flatMap {
        Future(async(CommonPool) {
            transform(it)
        })
    }

fun <A, B> Future<A>.flatMap(transform: (A) -> Future<B>): Future<B> =
    Future(async(CommonPool) {
        transform(this@flatMap.task.await()).task.await()
    })

fun <A, B> Future<A>.apply(futureAB: Future<(A) -> B>): Future<B> =
    Future(async(CommonPool) {
        val a = this@apply.task.await()
        val ab = futureAB.task.await()

        ab(a)
    })

fun main(args: Array<String>) {
    asyncFuture { 23 + 19 }
        .map { it + 3 }
        .runAsync(::println)
}