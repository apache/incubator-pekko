# splitWhen

Split off elements into a new substream whenever a predicate function return `true`.

@ref[Nesting and flattening operators](../index.md#nesting-and-flattening-operators)

## Signature

@apidoc[Source.splitWhen](Source) { scala="#splitWhen(p:Out=&gt;Boolean):org.apache.pekko.stream.scaladsl.SubFlow[Out,Mat,FlowOps.this.Repr,FlowOps.this.Closed]" java="#splitWhen(org.apache.pekko.japi.function.Predicate)" }
@apidoc[Flow.splitWhen](Flow) { scala="#splitWhen(p:Out=&gt;Boolean):org.apache.pekko.stream.scaladsl.SubFlow[Out,Mat,FlowOps.this.Repr,FlowOps.this.Closed]" java="#splitWhen(org.apache.pekko.japi.function.Predicate)" }

The `splitWhen` operator adheres to the ActorAttributes.SupervisionStrategy attribute with the caveat that
`Supervision.restart` behaves the same way as `Supervision.resume` since `Supervision.restart` for `SubFlow`s semantically doesn't make sense.

## Description

Split off elements into a new substream whenever a predicate function return `true`.

## Example

Given some time series data source we would like to split the stream into sub-streams for each second.
We need to compare the timestamp of the previous and current element to decide when to split. This
decision can be implemented in a `statefulMapConcat` operator preceding the `splitWhen`.  

Scala
:  @@snip [Scan.scala](/docs/src/test/scala/docs/stream/operators/sourceorflow/Split.scala) { #splitWhen }

Java
:  @@snip [SourceOrFlow.java](/docs/src/test/java/jdocs/stream/operators/sourceorflow/Split.java) { #splitWhen }

An alternative way of implementing this is shown in @ref:[splitAfter example](splitAfter.md#example).

## Reactive Streams semantics

@@@div { .callout }

**emits** an element for which the provided predicate is true, opening and emitting a new substream for subsequent elements

**backpressures** when there is an element pending for the next substream, but the previous is not fully consumed yet, or the substream backpressures

**completes** when upstream completes (Until the end of stream it is not possible to know whether new substreams will be needed or not)

@@@

