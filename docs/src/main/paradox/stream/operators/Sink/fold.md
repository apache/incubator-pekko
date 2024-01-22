# Sink.fold

Fold over emitted elements with a function, where each invocation will get the new element and the result from the previous fold invocation.

@ref[Sink operators](../index.md#sink-operators)

## Signature

@apidoc[Sink.fold](Sink$) { scala="#fold[U,T](zero:U)(f:(U,T)=&gt;U):org.apache.pekko.stream.scaladsl.Sink[T,scala.concurrent.Future[U]]" java="#fold(java.lang.Object,org.apache.pekko.japi.function.Function2)" }

## Description

Fold over emitted elements with a function, where each invocation will get the new element and the result from the
previous fold invocation. The first invocation will be provided the `zero` value.

Materializes into a @scala[`Future`] @java[`CompletionStage`] that will complete with the last state when the stream has completed.

This operator allows combining values into a result without a global mutable state by instead passing the state along
between invocations.

## Example

This example reads the numbers from a source and do some calculation in the flow part and in the end uses Sink.fold and adds the incoming elements.

Scala
:   @@snip [Fold.scala](/docs/src/test/scala/docs/stream/operators/sink/Fold.scala) { #fold }

Java
:   @@snip [SinkDocExamples.java](/docs/src/test/java/jdocs/stream/operators/SinkDocExamples.java) { #fold }

## Reactive Streams semantics

@@@div { .callout }

**cancels** never

**backpressures** when the previous fold function invocation has not yet completed

@@@

