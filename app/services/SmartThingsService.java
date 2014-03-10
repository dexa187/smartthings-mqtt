package services;

import play.libs.Akka;
import services.MqttService.RecievedMessage;
import services.MqttService.SubscriptionMessage;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class SmartThingsService extends UntypedActor {

	@Override
	public void preStart() {
		final ActorRef mqttService = Akka.system().actorOf(Props.create(MqttService.class));
		SubscriptionMessage msg = new SubscriptionMessage("+/+/control");
		mqttService.tell(msg, this.getSelf());
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		String topic = ((RecievedMessage)message).topic;
		Object payload = ((RecievedMessage)message).payload;
		System.out.println("Got a Control Message: "+payload+" on topic: "+topic );	
	}
	
	

}
