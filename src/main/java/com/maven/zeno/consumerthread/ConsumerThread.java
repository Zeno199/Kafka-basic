package com.maven.zeno.consumerthread;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConsumerThread {

	private final KafkaConsumer<String, String> consumer;
    private final String topic;
    
    // Threadpool
    private ExecutorService executor;
    
    public ConsumerThread(String brokers, String groupId, String topic){
    
    	Properties properties = buildKafkaProperty(brokers,groupId);
    	
    	this.topic = topic;
    	this.consumer = new KafkaConsumer<String, String>(properties);
        this.consumer.subscribe(Arrays.asList(this.topic));
    }

	private static Properties buildKafkaProperty(String brokers, String groupId) {
		
		Properties properties = new Properties();
        properties.put("bootstrap.servers", brokers);
        properties.put("group.id", groupId);
        properties.put("enable.auto.commit", "true");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("session.timeout.ms", "30000");
        properties.put("auto.offset.reset", "earliest");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return properties;
	}
	
	public void start(int threadNumber) {
		 
		executor = new ThreadPoolExecutor(threadNumber, threadNumber, 0L, TimeUnit.MILLISECONDS,
		        new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
	
		while (true){
            ConsumerRecords<String,String> consumerRecords = consumer.poll(100);
            
            for (final ConsumerRecord record : consumerRecords) {
                executor.submit(new ConsumerThreadHandler(record));
            }
        }
	}
    
}
