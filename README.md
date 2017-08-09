# trellis-amqp

[![Build Status](https://travis-ci.org/trellis-ldp/trellis-amqp.png?branch=master)](https://travis-ci.org/trellis-ldp/trellis-amqp)
[![Build status](https://ci.appveyor.com/api/projects/status/q1cdl5g48fiyed26?svg=true)](https://ci.appveyor.com/project/acoburn/trellis-amqp)
[![Coverage Status](https://coveralls.io/repos/github/trellis-ldp/trellis-amqp/badge.svg?branch=master)](https://coveralls.io/github/trellis-ldp/trellis-amqp?branch=master)


An AMQP-based messaging connector. This implements an event bridge to
AMQP-based brokers, such as Qpid or RabbitMQ.

## Building

This code requires Java 8 and can be built with Gradle:

    ./gradlew install
