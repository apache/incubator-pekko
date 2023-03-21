# Source.lazilyAsync

Deprecated by @ref:[`Source.lazyFutureSource`](lazyFutureSource.md).

@ref[Source operators](../index.md#source-operators)

## Signature

## Description

`lazilyAsync` is deprecated, please use @ref:[lazyFutureSource](lazyFutureSource.md) instead.

Defers creation and materialization of a `CompletionStage` until there is demand.

## Reactive Streams semantics

@@@div { .callout }

**emits** the future completes

**completes** after the future has completed

@@@

