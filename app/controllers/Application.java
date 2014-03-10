package controllers;

import actions.BasicAuth;
import models.User;
import play.Routes;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(main.render("", null));
    }
	
	@BasicAuth
	public static Result topics() {
		return ok(topics.render());
	}
	
	@BasicAuth
	public static Result switches(){
		return ok(switches.render());
	}
	
	@BasicAuth
	public static Result motions(){
		return ok(motions.render());
	}
	
	@BasicAuth
	public static Result doors(){
		return ok(doors.render());
	}
    
    
    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
            Routes.javascriptRouter("jsRoutes",
                controllers.routes.javascript.MqttBridge.getTopic(),
                controllers.routes.javascript.MqttBridge.listTopics(),
                controllers.routes.javascript.MqttBridge.delete(),
                controllers.routes.javascript.MqttBridge.publish()
            )
        );
    }

}
