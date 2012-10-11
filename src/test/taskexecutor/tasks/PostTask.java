package test.taskexecutor.tasks;
import java.io.IOException;
import main.taskexecutor.runnables.Task;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import android.net.http.AndroidHttpClient;
import android.os.SystemClock;
public class PostTask extends Task
{
    public static final String DELAY = "DELAY";
    public static final String URL = "URL";
    @Override
    public void task() throws IOException
    {
	SystemClock.sleep(getBundle().getInt(DELAY));
	HttpGet get = new HttpGet(getBundle().getString(URL));
	AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
	HttpResponse response = client.execute(get);
	client.close();
	int responseCode = response.getStatusLine().getStatusCode();
	getBundle().putString("ResponseCode", "Response Code: " + responseCode);
    }
}