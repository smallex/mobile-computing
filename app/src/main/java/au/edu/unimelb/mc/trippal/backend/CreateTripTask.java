package au.edu.unimelb.mc.trippal.backend;

import android.os.AsyncTask;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableResult;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

/**
 * AsyncTask for creating a new TripEntity in Azure Table Storage.
 */
public class CreateTripTask extends AsyncTask<String, Void, Void> {
    private static final String storageConnectionString = "DefaultEndpointsProtocol=https;" +
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
        } catch (URISyntaxException | InvalidKeyException | StorageException e) {
            e.printStackTrace();
        }

        return null;
    }
}
