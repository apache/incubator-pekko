# Source.fromFuture

Deprecated by @ref[`Source.future`](future.md).

@ref[Source operators](../index.md#source-operators)

## Signature

@apidoc[Source.fromFuture](Source$) { scala="#fromFuture[T](future:scala.concurrent.Future[T]):org.apache.pekko.stream.scaladsl.Source[T,org.apache.pekko.NotUsed]" }


## Description

`fromFuture` was deprecated in Akka 2.6.0, use @ref:[future](future.md) instead.

Send the single value of the `Future` when it completes and there is demand.
If the future fails the stream is failed with that exception.

## Reactive Streams semantics

@@@div { .callout }

**emits** the future completes

**completes** after the future has completed

@@@
