import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import models.User;

import com.avaje.ebean.Ebean;

import play.Application;
import play.GlobalSettings;
import play.libs.Yaml;


public class Global extends GlobalSettings {
	
	@Override
    public void onStart(Application app) {
        // Check if the database is empty
    	final ExecutorService exec = Executors.newFixedThreadPool(5);
        if (User.find.findRowCount() == 0) {
        	Map<String,List<Object>> all = (Map<String,List<Object>>)Yaml.load("initial-data.yml");

            // Insert users first
            Ebean.save(all.get("users"));
        }
	}
}
