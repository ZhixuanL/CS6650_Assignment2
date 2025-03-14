package consumer;
/**
 * Configuration class for RabbitMQ connection.
 * Stores host information, queue name, and connection details.
 */
public class ConsumerConfig {
  public static final String RABBITMQ_HOST = "34.214.120.223";  // RabbitMQ EC2 public IP
  public static final String QUEUE_NAME = "ski_queue";  // Must match the queue name in the producer
  public static final String USERNAME = "myuser";  // RabbitMQ username
  public static final String PASSWORD = "mypassword";  // RabbitMQ password
}
