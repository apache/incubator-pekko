# mapWithResource

Map elements with the help of a resource that can be opened, transform each element (in a blocking way) and closed.

@ref[Simple operators](../index.md#simple-operators)

## Signature

@apidoc[Flow.mapWithResource](Flow) { scala="#mapWithResource[S,T](create:()=%3ES)(f:(S,Out)=%3ET,close:S=%3EOption[T]):Repr[T]" java="#mapWithResource(org.apache.pekko.japi.function.Creator,org.apache.pekko.japi.function.Function2,org.apache.pekko.japi.function.Function)" }
1. `create`: Open or Create the resource.
2. `f`: Transform each element inputs with the help of resource.
3. `close`: Close the resource, invoked on end of stream or if the stream fails, optionally outputting a last element.

@apidoc[Flow.mapWithResource](Flow) { scala="#mapWithResource[S%3C:AutoCloseable,T](create:()=%3ES,f:(S,Out)=%3ET):Repr[T]" java="#mapWithResource(org.apache.pekko.japi.function.Creator,org.apache.pekko.japi.function.Function2)" }
1. `create`: Open or Create the autocloseable resource.
2. `f`: Transform each element inputs with the help of resource.

## Description

Transform each stream element with the help of a resource.
The functions are by default called on Pekko's dispatcher for blocking IO to avoid interfering with other stream operations.
See @ref:[Blocking Needs Careful Management](../../../typed/dispatchers.md#blocking-needs-careful-management) for an explanation on why this is important.
The resource creation function is invoked once when the stream is materialized and the returned resource is passed to the mapping function for mapping the first element. The mapping function returns a mapped element to emit downstream. The returned T MUST NOT be null as it is illegal as stream element - according to the Reactive Streams specification.

The close function is called when upstream or downstream completes normally or exceptionally, and will be called only once.
- upstream completes or fails, the optional value returns by `close` will be emitted to downstream if defined.
- downstream cancels or fails, the optional value returns by `close` will be ignored.
- shutdowns abruptly, the optional value returns by `close` will be ignored.  
  You can do some clean-up here.

Early completion can be done with combination of the @apidoc[Flow.takeWhile](Flow) operator.

See also @ref:[unfoldResource](../Source/unfoldResource.md), @ref:[unfoldResourceAsync](../Source/unfoldResourceAsync.md).

You can configure the default dispatcher for this Source by changing the `org.apache.pekko.stream.materializer.blocking-io-dispatcher`
or set it for a given Source by using ActorAttributes.

## Examples

Imagine we have a database API which may potentially block when we perform a query,
and the database connection can be reused for each query.

Scala
:   @@snip [UnfoldResource.scala](/docs/src/test/scala/docs/stream/operators/sourceorflow/MapWithResource.scala) { #mapWithResource-blocking-api }

Java
:   @@snip [UnfoldResource.java](/docs/src/test/java/jdocs/stream/operators/sourceorflow/MapWithResource.java) { #mapWithResource-blocking-api }

Let's see how we make use of the API above safely through `mapWithResource`:

Scala
:   @@snip [UnfoldResource.scala](/docs/src/test/scala/docs/stream/operators/sourceorflow/MapWithResource.scala) { #mapWithResource }

Java
:   @@snip [UnfoldResource.java](/docs/src/test/java/jdocs/stream/operators/sourceorflow/MapWithResource.java) { #mapWithResource }

In this example we retrieve data form two tables with the same shared connection, and transform the results
to individual records with @scala[`mapConcat(identity)`]@java[`mapConcat(elems -> elems)`], once done the connection is closed.


## Reactive Streams semantics

@@@div { .callout }

**emits** the mapping function returns an element and downstream is ready to consume it

**backpressures** downstream backpressures

**completes** upstream completes

**cancels** downstream cancels

@@@