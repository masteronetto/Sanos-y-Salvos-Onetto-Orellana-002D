package com.sanosysalvos.match.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig(
    @Value("\${rabbitmq.exchange}") private val exchange: String,
    @Value("\${rabbitmq.queue.pet-reports}") private val petReportsQueue: String,
    @Value("\${rabbitmq.routing-key.pet}") private val petRoutingKey: String,
) {

    @Bean
    fun directExchange(): DirectExchange = DirectExchange(exchange, true, false)

    @Bean
    fun petReportsQueue(): Queue = Queue(petReportsQueue, true)

    @Bean
    fun petReportsBinding(queue: Queue, exchange: DirectExchange): Binding =
        BindingBuilder.bind(queue)
            .to(exchange)
            .with(petRoutingKey)
}
