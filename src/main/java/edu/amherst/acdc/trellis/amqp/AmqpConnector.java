/*
 * Copyright Amherst College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.amherst.acdc.trellis.amqp;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import edu.amherst.acdc.trellis.api.RuntimeRepositoryException;
import edu.amherst.acdc.trellis.spi.Event;
import edu.amherst.acdc.trellis.spi.EventService;

/**
 * @author acoburn
 */
public class AmqpConnector implements EventService {

    private static final ConnectionFactory factory = new ConnectionFactory();

    private final Connection conn;

    private final Channel channel;

    /**
     * Create a new AMQP Connector
     * @throws IOException when there is an error connecting to the AMQP broker
     * @throws TimeoutException when the connection takes too long to establish itself
     */
    public AmqpConnector() throws IOException, TimeoutException {
        //factor.setUri("amqp://username:password@host:port/vhost");
        conn = factory.newConnection();
        channel = conn.createChannel();
    }

    @Override
    public void emit(final Event event) {
        requireNonNull(event, "Cannot emit a null event!");
        final String message = of(event).flatMap(EventService::serialize).orElseThrow(() ->
                new RuntimeRepositoryException("Unable to serialize event!"));
    }
}
