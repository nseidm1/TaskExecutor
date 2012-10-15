package test.taskexecutor.tasks;

import java.io.IOException;

import main.taskexecutor.Task;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import android.net.http.AndroidHttpClient;
import android.os.SystemClock;

public class GetTask extends Task {
    public static final String DELAY = "DELAY";
    public static final String URL = "URL";

    @Override
    public void task() throws IOException {
	AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
	try {
	    SystemClock.sleep(getBundle().getInt(DELAY));
	    HttpGet get = new HttpGet(getBundle().getString(URL));
	    HttpResponse response = client.execute(get);
	    client.close();
	    int responseCode = response.getStatusLine().getStatusCode();
	    getBundle().putString("ResponseCode", "Response Code: "
		    + responseCode);
	} catch (IOException e) {
	    client.close();
	    throw e;
	}
    }
}