package au.edu.unimelb.mc.trippal.backend;

import android.os.AsyncTask;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableQuery;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import au.edu.unimelb.mc.trippal.trip.TripListActivity;

public class QueryTask extends AsyncTask<String, Void, List<TripEntity>> {
    private final TripListActivity tripListActivity;

    public QueryTask(TripListActivity tripListActivity) {
        this.tripListActivity = tripListActivity;
    }

    @Override
    protected List<TripEntity> doInBackground(String... strings) {
        ArrayList<TripEntity> tripEntities = new ArrayList<>();
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(StorageConfiguration
                    .storageConnectionString);
            CloudTableClient tableClient = account
                    .createCloudTableClient();
            CloudTable table = tableClient.getTableReference("trips");
            table.createIfNotExists();

            TableQuery<TripEntity> query = TableQuery.from(TripEntity.class);
            for (TripEntity tripEntity : table.execute(query)) {
                tripEntities.add(tripEntity);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (StorageException e) {
            e.printStackTrace();
        }

        return tripEntities;
    }

    @Override
    protected void onPostExecute(List<TripEntity> tripEntities) {
        tripListActivity.loadTrips(tripEntities);
    }
}
