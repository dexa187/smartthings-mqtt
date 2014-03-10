package services;

import java.util.HashMap;
import java.util.Map;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import play.Play;
import play.libs.Json;
import play.mvc.WebSocket;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MqttService extends UntypedActor{
	
	private MQTT mqtt;
	private CallbackConnection connection;
	private ActorRef sender;
	
	/**
	 * The registred clients.
	 */
	Map<String, WebSocket.Out<JsonNode>> registrered = new HashMap<String, WebSocket.Out<JsonNode>>();
	Map<String, String> topics = new HashMap<String,String>();
	
	public MqttService() throws Exception{
		mqtt = new MQTT();
		mqtt.setHost(Play.application().configuration().getString("mqtt.host"), Play.application().configuration().getInt("mqtt.port"));
		connection = mqtt.callbackConnection();
		connection.connect(new Callback<Void>() {
		    public void onFailure(Throwable value) {
		        System.out.println("Couldnt Connect");
		    }

		    // Once we connect..
		    public void onSuccess(Void v) {
		    	System.out.println("Connected to MQTT Broker");
		    }
		});
		
		connection.listener(new Listener() {

		    public void onDisconnected() {
		    }
		    public void onConnected() {
		    }

		    @Override
		    public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
		        // You can now process a received message from a topic.
		        // Once process execute the ack runnable.
		    	ObjectNode topicMessage = Json.newObject();
		    	String messagePayload = payload.utf8().toString();
		    	String topicName = topic.toString();
				System.out.println("Hey, message arrived on topic " + topic.toString() + " : " + payload.utf8());
				try{
					topicMessage.put("value", Json.parse(messagePayload));
				}catch (Exception e){
					topicMessage.put("value", new String(messagePayload));
				}
				topicMessage.put("topicName", Json.toJson(topicName));
				RecievedMessage msg = new RecievedMessage(topicName,topicMessage);
				if (sender != ActorRef.noSender()){
					sender.tell(msg, getSelf());
				}
				for (WebSocket.Out<JsonNode> channel : registrered.values()) {
					channel.write(topicMessage);
				}
				
		        ack.run();
		    }
		    public void onFailure(Throwable value) {
		    	System.out.println("Failed");
		        //connection.close(null); // a connection failure occured.
		    }

		});
	}
	
	
	
	public void publish(final String topic, final String payload, Boolean retain){
		 
	     connection.publish(topic, payload.getBytes(), QoS.AT_LEAST_ONCE, retain, new Callback<Void>() {
	            public void onSuccess(Void v) {
	              // the pubish operation completed successfully.
	            	System.out.println("Published Sucessfull "+payload+" to: "+topic);
	            }
	            public void onFailure(Throwable value) {
	                System.out.println("Publish Failed");
	            }
	        });
	}
	
	public void subscribe(final String topic){
		Topic[] topics = {new Topic(topic, QoS.AT_LEAST_ONCE)};
		connection.subscribe(topics, new Callback<byte[]>() {
            public void onSuccess(byte[] qoses) {
                // The result of the subcribe request.
            	System.out.println("Subscribe Success on topic"+ topic);
            }
            public void onFailure(Throwable value) {
                System.out.println("Subscribe Failed"); // subscribe failed.
            }
        });
	}
	
	public void unsubscribe(String topic){
		UTF8Buffer[] topics = {new UTF8Buffer(topic)};
		connection.unsubscribe(topics, new Callback<Void>() {
			@Override
			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(Void arg0) {
				// TODO Auto-generated method stub
				System.out.println("UnSubscribe Success");
			}
        });
	}
	
	public void delete(String topic){
		publish(topic, "", true);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof PublishMessage){
			publish(((PublishMessage)message).topic,((PublishMessage)message).payload.toString(),true);
		}else if (message instanceof ConnectionMessage){
			this.sender = getSender();
			String topic = ((ConnectionMessage)message).topic;
			String id = ((ConnectionMessage)message).id;
			registrered.put(id, ((ConnectionMessage)message).out);
			topics.put(id,topic);
			subscribe(topic);
		}else if (message instanceof SubscriptionMessage){
			this.sender = getSender();
			String topic = ((SubscriptionMessage)message).topic;
			subscribe(topic);
		}else if (message instanceof DisconnectMessage){
			unsubscribe(topics.get(((DisconnectMessage)message).id));
			registrered.remove(((DisconnectMessage)message).id);
			topics.remove(((DisconnectMessage)message).id);
			getContext().stop(getSelf());
		}
		
	}
	
	public static class PublishMessage {
		  String topic;
		  Object payload;
		  public PublishMessage(String topic,Object payload){
			  this.topic=topic;
			  this.payload=payload;
		  }
	}
	
	public static class ConnectionMessage{
		private WebSocket.In<JsonNode> in;
		private WebSocket.Out<JsonNode> out;
		private String topic;
		private String id;
		public ConnectionMessage(String id, String topic, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
			this.in=in;
			this.out=out;
			this.topic=topic;
			this.id=id;
		}	
	}
	
	public static class DisconnectMessage {
		public String id;
		public DisconnectMessage(String id) {
			this.id=id;
		}	
	}
	
	public static class SubscriptionMessage{
		public String topic;
		public String id;
		public SubscriptionMessage(String topic){
			this.topic=topic;
		}
	}
	
	public static class RecievedMessage{
		public String topic;
		public Object payload;
		public RecievedMessage(String topic, Object payload){
			this.topic = topic;
			this.payload = payload;
		}
	}
	
}
