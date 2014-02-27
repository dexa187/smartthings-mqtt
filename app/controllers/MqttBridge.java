package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import play.libs.Json;
import play.mvc.*;
import services.MqttService;
import views.html.*;

public class MqttBridge extends Controller {
	public static Result listTopics() throws Exception {
		JsonNode json = request().body().asJson();
		MqttService mqtt = new MqttService();
		return ok(mqtt.subscribe("#"));
	}
	
	public static Result getTopic(String topic) throws Exception{
		JsonNode json = request().body().asJson();
		MqttService mqtt = new MqttService();
		return ok(mqtt.subscribe(topic));
	}
	
	public static Result publish(String topic) throws Exception{
		MqttService mqtt = new MqttService();
		String payload = request().body().asText();
		System.out.println("this is the payload" +request().toString());
		if(mqtt.publish(topic, payload, true)){
			return ok();
		}else{
			return internalServerError();
		}
	}
	
	public static Result delete(String topic) throws Exception {
		MqttService mqtt = new MqttService();
		if(mqtt.delete(topic)){
			return ok();
		}else{
			return internalServerError();
		}
	}
}
