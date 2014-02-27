package services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import play.Play;
import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MqttService {
	MQTT mqtt;
	BlockingConnection connection;
	
	public MqttService() throws Exception{
		mqtt = new MQTT();
		mqtt.setHost(Play.application().configuration().getString("mqtt.host"), Play.application().configuration().getInt("mqtt.port"));
		connection = mqtt.blockingConnection();
		connection.connect();
	}
	
	public boolean publish(String topic, String payload, Boolean retain){
		try{
		connection.publish(topic, payload.getBytes(), QoS.EXACTLY_ONCE, retain);
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	public JsonNode subscribe(String topic) throws Exception{
		Topic[] topics = {new Topic(topic, QoS.AT_LEAST_ONCE)};
		connection.subscribe(topics);
		List messages = new ArrayList();
		//ArrayNode messages = new ArrayNode();
		Message message;
		while ((message = connection.receive(1000, TimeUnit.MILLISECONDS)) != null){
			ObjectNode topicMessage = Json.newObject();
			try{
				topicMessage.put("value", Json.parse((new String(message.getPayload(), "iso-8859-1"))));
			}catch (Exception e){
				topicMessage.put("value", new String(message.getPayload(), "iso-8859-1"));
			}
			topicMessage.put("topicName", Json.toJson(message.getTopic()));
			messages.add(topicMessage);
			message.ack();
		}
		return Json.toJson(messages);
	}
	
	public boolean delete(String topic){
		return publish(topic, "", true);
	}
}
