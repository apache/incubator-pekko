# ActorSink.actorRef

Sends the elements of the stream to the given @java[`ActorRef<T>`]@scala[`ActorRef[T]`] of the new actors API, without considering backpressure.

@ref[Actor interop operators](../index.md#actor-interop-operators)

## Dependency

This operator is included in:

@@dependency[sbt,Maven,Gradle] {
  bomGroup=org.apache.pekko bomArtifact=pekko-bom_$scala.binary.version$ bomVersionSymbols=PekkoVersion
  symbol1=PekkoVersion
  value1="$pekko.version$"
  group="org.apache.pekko"
  artifact="pekko-stream-typed_$scala.binary.version$"
  version=PekkoVersion
}

## Signature

@apidoc[ActorSink.actorRef](ActorSink$) { scala="#actorRef[T](ref:org.apache.pekko.actor.typed.ActorRef[T],onCompleteMessage:T,onFailureMessage:Throwable=&gt;T):org.apache.pekko.stream.scaladsl.Sink[T,org.apache.pekko.NotUsed]" java="#actorRef(org.apache.pekko.actor.typed.ActorRef,java.lang.Object,org.apache.pekko.japi.function.Function)" }

## Description

Sends the elements of the stream to the given `ActorRef`.
If the target actor terminates the stream will be canceled.
When the stream completes successfully the given `onCompleteMessage`
will be sent to the destination actor.
When the stream completes with failure the throwable that was signaled
to the stream is adapted to the Actor's protocol using `onFailureMessage` and
then sent to the destination actor.

It will request at most `maxInputBufferSize` number of elements from
upstream, but there is no back-pressure signal from the destination actor,
i.e. if the actor is not consuming the messages fast enough the mailbox
of the actor will grow. For potentially slow consumer actors it is recommended
to use a bounded mailbox with zero `mailbox-push-timeout-time` or use a rate
limiting operator in front of this `Sink`.

See also:

* @ref[`ActorSink.actorRefWithBackpressure`](../ActorSink/actorRefWithBackpressure.md) Send elements to an actor of the new actors API supporting backpressure
* @ref[`Sink.actorRef`](../Sink/actorRef.md) The corresponding operator for the classic actors API
* @ref[`Sink.actorRefWithBackpressue`](../Sink/actorRefWithBackpressure.md) Send elements to an actor of the classic actors API supporting backpressure

## Reactive Streams semantics

@@@div { .callout }

**cancels** when the actor terminates

**backpressures** never

@@@
