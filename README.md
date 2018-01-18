# protoroutes

[![Build Status](https://travis-ci.org/hirofumi/protoroutes.svg?branch=master)](https://travis-ci.org/hirofumi/protoroutes)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.hirofumi/sbt-protoroutes/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.hirofumi%22%20protoroutes)

Protoroutes is an sbt plugin which can generate the following from .proto file.

* Play Framework's `Router` which handles Protocol Buffers and JSON requests
* Scala.js client which communicates with the `Router`

```
++---------------++
||               ||  protoroutes   +-----------------+
||               || =============> | Scala.js client |
||               ||                +-----------------+
||               ||                     ^                    +-----------------+
||               ||                     | +----------------->| REST API client |
||               ||                     | |                  +-----------------+
||               ||       REST API (PB) \ / REST API (JSON)
|| service.proto ||                      |
||               ||                      v
||               ||  protoroutes   +----------------+
||               || =============> | Play Router    |
||               ||                +----------------+
||               ||                | Your own       |
||               ||                | implementation |
||               ||                | of `service`   |
||               ||  ScalaPB       +----------------+
||               || =============> | gRPC Interface |
||               ||                +----------------+
++---------------++                      ^
                                    gRPC |                   +-----------------+
                                         +------------------>| gRPC client     |
                                                             +-----------------+
```

The idea of protoroutes is based on [btlines/grpcgateway](https://github.com/btlines/grpcgateway).
