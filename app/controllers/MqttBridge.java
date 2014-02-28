package controllers;

import actions.BasicAuth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import play.libs.Json;
import play.mvc.*;
import services.MqttService;
import views.html.*;

public class MqttBridge extends Controller {
	
	@BasicAuth
	public static Result listTopics() throws Exception {
		MqttService mqtt = new MqttService();
		return ok(mqtt.subscribe("#"));
	}
	
	@BasicAuth
	public static Result getTopic(String topic) throws Exception{
		MqttService mqtt = new MqttService();
		return ok(mqtt.subscribe(topic));
	}
	
	@BasicAuth
	public static Result publish(String topic) throws Exception{
		MqttService mqtt = new MqttService();
		String payload = request().body().asText();
		return (mqtt.publish(topic, payload, true)) ? ok() : internalServerError();
	}
	
	@BasicAuth
	public static Result delete(String topic) throws Exception {
		MqttService mqtt = new MqttService();
		return (mqtt.delete(topic)) ? ok() : internalServerError();
	}
}
