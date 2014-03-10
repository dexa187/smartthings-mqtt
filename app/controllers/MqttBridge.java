package controllers;

import play.libs.Akka;
import play.libs.F.Callback0;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.MqttService;
import services.MqttService.ConnectionMessage;
import services.MqttService.DisconnectMessage;
import services.MqttService.PublishMessage;
import actions.BasicAuth;
import akka.actor.ActorRef;
import akka.actor.Props;

import com.fasterxml.jackson.databind.JsonNode;


public class MqttBridge extends Controller {
	
	final static ActorRef mqttPublisherService = Akka.system().actorOf(Props.create(MqttService.class));
	
	@BasicAuth
	public static WebSocket<JsonNode> listTopics() throws Exception {
		return getTopic("#");
	}
	
	@BasicAuth
	public static WebSocket<JsonNode> getTopic(String topic) throws Exception{
		final String unscapedTopic = topic.replaceAll("%23", "#");
		return new WebSocket<JsonNode>() {

			// Called when the Websocket Handshake is done.
			@Override
			public void onReady(final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
				final String id = java.util.UUID.randomUUID().toString();
				ConnectionMessage msg = new ConnectionMessage(id,unscapedTopic,in,out);
				final ActorRef mqttService = Akka.system().actorOf(Props.create(MqttService.class));
				mqttService.tell(msg, ActorRef.noSender());
				in.onClose(new Callback0() {
					@Override
					public void invoke() {
						mqttService.tell(new DisconnectMessage(id),ActorRef.noSender());
					}
				});
			}
		};
	}
	
	@BasicAuth
	public static Result publish(String topic) throws Exception{
		PublishMessage message = new PublishMessage(topic,request().body().asText());
		mqttPublisherService.tell(message, ActorRef.noSender());
		return ok();
	}
	
	@BasicAuth
	public static Result delete(String topic) throws Exception {
		PublishMessage message = new PublishMessage(topic,"");
		mqttPublisherService.tell(message, ActorRef.noSender());
		return ok();
	}
}
