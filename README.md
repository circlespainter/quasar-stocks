# Quasar Stocks

This is a porting of [TypeSafe's Reactive Stock](https://github.com/typesafehub/reactive-stocks) example to [Quasar Actors](http://docs.paralleluniverse.co/quasar/#quasars-actor-system) and [Comsat Web Actors](http://docs.paralleluniverse.co/quasar/#quasars-actor-system). Run on Jetty embedded servlet container plus its WebSocket module.

## Getting started

1. `./gradlew run`
2. Open http://localhost:8080 in a browser that supports WebSocket.
3. Add new (fake) stock symbols.

## Notes

- The Java implementation is almost as compact as the half-Scala original one.
  - ...And much easier to understand.
- Quasar Actors are based on Quasar's _lightweight threads_ or **fibers** which make all the code is code straightforward, sequential imperative style.
  - No declarative-functional-monadic-async programming is forced down your throat just to work around JVM threads' heavy footprint.
- Since calls are (fiber-)blocking and full control flow language can be used around them, no tick message is needed: just use the straightfoward blocking `receive` with a timeout.
- By default Web Actors automatically assign a new actor to a request or WebSocket connection, which can be configured for automatic termination upon connection closing with a simple `watch` call.
  - So, supervision isn't really necessary and Quasar doesn't make it mandatory (it is there just to showcase it).
- Actors and Web Actors do just one thing well: everything is conf- and JSON-library agnostic, you're not forced into a full-blown lock-in web framework.
- 100% Servlet compatible, no need to run a non-standard embedded server if you don't want to.

## TODO

- Currently generated web resources are just copied: add build steps from original CoffeeScript and LESS source files.
- Do away with the `comsat-testutils` dep.
- Add [Capsule](https://github.com/puniverse/capsule)-based deployment.
