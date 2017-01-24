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

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static com.rabbitmq.client.BuiltinExchangeType.DIRECT;
import static edu.amherst.acdc.trellis.spi.EventService.serialize;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import edu.amherst.acdc.trellis.api.RuntimeRepositoryException;
import edu.amherst.acdc.trellis.spi.Event;
import edu.amherst.acdc.trellis.spi.EventService;
import org.slf4j.Logger;

/**
 * An AMQP message producer capable of publishing messages to an AMQP broker such as
 * RabbitMQ or Qpid.
 *
 * @author acoburn
 */
public class AmqpPublisher implements EventService {

    private static final Logger LOGGER = getLogger(AmqpPublisher.class);

    private static final ConnectionFactory factory = new ConnectionFactory();

    private final Boolean durable;

    private final Boolean exclusive;

    private final Boolean autoDelete;

    private final Boolean mandatory;

    private final Boolean immediate;

    private final Connection conn;

    private final Channel channel;

    private final String exchangeName;

    private final String queueName;

    /**
     * Create a new AMQP Publisher
     * @throws IOException when there is an error connecting to the AMQP broker
     * @throws TimeoutException when the connection takes too long to establish itself
     * @throws URISyntaxException if the connection URI is malformed
     * @throws NoSuchAlgorithmException if the provided algorithm doesn't exist
     * @throws KeyManagementException if there was an error with the key management
     */
    public AmqpPublisher() throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException,
           KeyManagementException {
        this(getProperty("trellis.amqp.uri"), "trellis", "event");
    }

    /**
     * Create a new AMQP Publisher
     * @param uri the connection URI
     * @param exchangeName the name of the exchange
     * @param queueName the name of the queue
     * @throws IOException when there is an error connecting to the AMQP broker
     * @throws TimeoutException when the connection takes too long to establish itself
     * @throws URISyntaxException if the connection URI is malformed
     * @throws NoSuchAlgorithmException if the provided algorithm doesn't exist
     * @throws KeyManagementException if there was an error with the key management
     */
    public AmqpPublisher(final String uri, final String exchangeName, final String queueName)
            throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        requireNonNull(uri);
        requireNonNull(exchangeName);
        requireNonNull(queueName);

        this.durable = parseBoolean(getProperty("trellis.amqp.durable", "true"));
        this.exclusive = parseBoolean(getProperty("trellis.amqp.exclusive", "false"));
        this.autoDelete = parseBoolean(getProperty("trellis.amqp.autoDelete", "false"));
        this.mandatory = parseBoolean(getProperty("trellis.amqp.mandatory", "true"));
        this.immediate = parseBoolean(getProperty("trellis.amqp.immediate", "false"));
        this.exchangeName = exchangeName;
        this.queueName = queueName;

        factory.setUri(uri);

        conn = factory.newConnection();
        channel = conn.createChannel();
        channel.exchangeDeclare(exchangeName, DIRECT, durable);
        channel.queueDeclare(queueName, durable, exclusive, autoDelete, emptyMap());
        channel.queueBind(queueName, exchangeName, queueName);
    }

    @Override
    public void emit(final Event event) {
        requireNonNull(event, "Cannot emit a null event!");

        final BasicProperties props = new BasicProperties().builder()
                .contentType("application/ld+json").contentEncoding("UTF-8").build();

        final String message = serialize(event).orElseThrow(() ->
                new RuntimeRepositoryException("Unable to serialize event!"));

        try {
            channel.basicPublish(exchangeName, queueName, mandatory, immediate, props, message.getBytes());
        } catch (final IOException ex) {
            LOGGER.error("Error writing to broker: " + ex.getMessage());
        }
    }
}
