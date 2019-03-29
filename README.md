# Simple Example of Quill with Akka Streaming (via Monix)

See `AkkaHttpTestReactive.scala`. 

 - Use Monix's `.toReactivePublisher` to turn an arbitrary Observeable (or Task) into a reactive streams publisher.
 - Take the publisher (it's still typed) and put it into a Akka source via `Source.fromPublisher(yourPublisher)`.
 - Put that into a `ChunkStreamPart` and use `HttpEntity.Chunked` to take care of the lower-level details.
 
That's all folks!
