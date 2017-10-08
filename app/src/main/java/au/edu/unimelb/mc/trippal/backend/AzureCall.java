package au.edu.unimelb.mc.trippal.backend;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

/**
 * Wraps Azure LUIS requests.
 */
public class AzureCall {
    public static String url = "https://westus.api.cognitive.microsoft" +
            ".com/luis/v2.0/apps/66c255da-44f7-4de0-8e11-794cb4eae71f?subscription-key" +
            "=ecf89be881564c038529aff371d675c7&verbose=true&timezoneOffset=0";
    private Context context;

    public AzureCall(Context context) {
        this.context = context;
    }

    public void sendRequest(JsonObjectRequest jsObjRequest, String request) {

        // set timeout to 20 sec
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        //Adding request to the queue
        requestQueue.add(jsObjRequest);
    }
}
