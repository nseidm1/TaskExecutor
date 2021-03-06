package test.taskexecutor.tasks;

import java.io.IOException;

import main.taskexecutor.core.Task;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.net.http.AndroidHttpClient;

public class GetTask extends Task {
    public static final String DELAY = "DELAY";
    public static final String URL = "URL";

    @Override
    public void task() throws IOException, InterruptedException {
	AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
	try {
	    Thread.sleep(getMainBundle().getInt(DELAY));
	    HttpGet get = new HttpGet(getMainBundle().getString(URL));
	    HttpResponse response = client.execute(get);
	    client.close();
	    int responseCode = response.getStatusLine().getStatusCode();
	    getMainBundle().putString("ResponseCode", "Response Code: " + responseCode);
	} catch (IOException e) {
	    client.close();
	    throw e;
	} catch (InterruptedException e) {
	    client.close();
	    throw e;
	}
    }

    @Override
    public boolean queueResultsIfNoActivity() {
	return false;
    }
}
