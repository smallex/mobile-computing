package au.edu.unimelb.mc.trippal.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import au.edu.unimelb.mc.trippal.R;

import static android.content.ContentValues.TAG;

/**
 * Created by Martin on 09.09.2017.
 */

public class AzureExample {
    private Context context;
    private String url = "https://westus.api.cognitive.microsoft.com/emotion/v1.0/analyze";
    private String key = "b295c375f8824772a6b1796ca32a04b8";

    public AzureExample(Context context) {
        this.context = context;
    }

    private byte[] getBinaryImage() {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.yawning_example);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return byteArray;
    }


    public void sendRequest() {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("TestAzure", response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d("TestAzure", "Error: " + error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/octet-stream");
                headers.put("Ocp-Apim-Subscription-Key", key);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/octet-stream";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return getBinaryImage();
            }

        };
        // set timeout to 20 sec
        strReq.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                5,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);


        //Adding request to the queue
        requestQueue.add(strReq);

    }

}
