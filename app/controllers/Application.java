package controllers;

import models.User;
import play.Routes;
import play.data.Form;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

	@Security.Authenticated(Secured.class)
    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result login() {
        return ok(login.render(Form.form(Login.class)));
    }
    
    public static Result authenticate() {
        Form<Login> loginForm = Form.form(Login.class).bindFromRequest();
        if (loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {
            session().clear();
            session("email", loginForm.get().email);
            return redirect(
                routes.Application.index()
            );
        }
    }
    
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(
            routes.Application.login()
        );
    }
    
    public static class Login {

        public String email;
        public String password;
        
        public String validate() {
            if (User.authenticate(email, password) == null) {
              return "Invalid user or password";
            }
            return null;
        }

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
