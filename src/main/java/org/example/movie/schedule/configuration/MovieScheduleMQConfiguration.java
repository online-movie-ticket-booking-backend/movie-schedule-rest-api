package org.example.movie.schedule.configuration;

import lombok.Data;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("message")
public class MovieScheduleMQConfiguration {

  private MessageConfiguration messageConfiguration;
  private ExchangeConfiguration movieInventoryExchangeConfiguration;
  private ExchangeConfiguration movieScheduleExchangeConfiguration;

  @Bean
  public ExchangeConfiguration movieInventoryExchange() {
    return movieInventoryExchangeConfiguration;
  }

  @Bean
  public ExchangeConfiguration movieScheduleExchange() {
    return movieScheduleExchangeConfiguration;
  }

  @Bean
  public CachingConnectionFactory connectionFactory() {
    CachingConnectionFactory cachingConnectionFactory =
        new CachingConnectionFactory(messageConfiguration.getHost());
    cachingConnectionFactory.setUsername(messageConfiguration.getUsername());
    cachingConnectionFactory.setPassword(messageConfiguration.getPassword());
    cachingConnectionFactory.setPort(messageConfiguration.getPort());
    cachingConnectionFactory.setVirtualHost(messageConfiguration.getVirtualHost());
    return cachingConnectionFactory;
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    return rabbitTemplate;
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
