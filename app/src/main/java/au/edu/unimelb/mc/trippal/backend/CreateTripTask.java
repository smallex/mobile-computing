package au.edu.unimelb.mc.trippal.backend;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableResult;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

/**
 * Created by Paul Kuznecov on 03.10.2017.
 */

public class CreateTripTask extends AsyncTask<String, Void, Void> {
    public static final String storageConnectionString = "DefaultEndpointsProtocol=https;" +
            "AccountName=trippal;AccountKey=TwfLw2eTaBCJ6gKTRsWOhWZRtFnkSQpjxR6/MCvd+ANNtHMg" +
            "/AyGUaABw6mxvhvIG4Y/4Px1/yxigOkPX3tK8g==;EndpointSuffix=core.windows.net";

    private final TripEntity trip;

    public CreateTripTask(TripEntity trip) {
        this.trip = trip;
    }

    @Override
    protected Void doInBackground(String... strings) {

        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudTableClient tableClient = account
                    .createCloudTableClient();
            CloudTable table = tableClient.getTableReference("trips");
            table.createIfNotExists();

            TableOperation insert = TableOperation.insert(trip);
            TableResult result = table.execute(insert);
            Log.d("AZURE", result.getResult().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        }

        return null;
    }
}
