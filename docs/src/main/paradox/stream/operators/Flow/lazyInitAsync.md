# Flow.lazyInitAsync

Deprecated by @ref:[`Flow.lazyFutureFlow`](lazyFutureFlow.md) in combination with @ref:[`prefixAndTail`](../Source-or-Flow/prefixAndTail.md).

@ref[Simple operators](../index.md#simple-operators)

## Signature

@apidoc[Flow.lazyInitAsync](Flow$) { scala="#lazyInitAsync[I,O,M](flowFactory:()=&gt;scala.concurrent.Future[org.apache.pekko.stream.scaladsl.Flow[I,O,M]]):org.apache.pekko.stream.scaladsl.Flow[I,O,scala.concurrent.Future[Option[M]]]" java="#lazyInitAsync(org.apache.pekko.japi.function.Creator)" }

## Description

`fromCompletionStage` is deprecated, please use @ref:[lazyFutureFlow](lazyFutureFlow.md) in combination with @ref:[`prefixAndTail`](../Source-or-Flow/prefixAndTail.md)) instead.

Defers creation until a first element arrives.

## Reactive Streams semantics

@@@div { .callout }

**emits** when the internal flow is successfully created and it emits

**backpressures** when the internal flow is successfully created and it backpressures

**completes** when upstream completes and all elements have been emitted from the internal flow

**completes** when upstream completes and all futures have been completed and all elements have been emitted

**cancels** when downstream cancels (keep reading)
    The operator's default behavior in case of downstream cancellation before nested flow materialization (future completion) is to cancel immediately.
     This behavior can be controlled by setting the [[org.apache.pekko.stream.Attributes.NestedMaterializationCancellationPolicy.PropagateToNested]] attribute,
    this will delay downstream cancellation until nested flow's materialization which is then immediately cancelled (with the original cancellation cause).
@@@

