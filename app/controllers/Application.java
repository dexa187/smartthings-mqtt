package controllers;

import play.Routes;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
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
