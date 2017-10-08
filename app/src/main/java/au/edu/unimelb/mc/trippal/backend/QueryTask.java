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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import au.edu.unimelb.mc.trippal.trip.TripListActivity;

/**
 * AsyncTask for querying the Azure Table Storage for all trips of the current user.
 */
public class QueryTask extends AsyncTask<String, Void, List<TripEntity>> {
    private final TripListActivity tripListActivity;

    public QueryTask(TripListActivity tripListActivity) {
        this.tripListActivity = tripListActivity;
    }

    @Override
    protected List<TripEntity> doInBackground(String... strings) {
        ArrayList<TripEntity> tripEntities = new ArrayList<>();
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(AzureStorageConfiguration
                    .storageConnectionString);
            CloudTableClient tableClient = account
                    .createCloudTableClient();
            CloudTable table = tableClient.getTableReference("trips");
            table.createIfNotExists();

            String userId = UserUtils.getUserId(tripListActivity);
            String partitionFilter = TableQuery.generateFilterCondition(
                    "PartitionKey", TableQuery.QueryComparisons.EQUAL, userId);
            TableQuery<TripEntity> query = TableQuery.from(TripEntity.class).where(partitionFilter);
            for (TripEntity tripEntity : table.execute(query)) {
                tripEntities.add(tripEntity);
            }
            Collections.sort(tripEntities, new Comparator<TripEntity>() {
                @Override
                public int compare(TripEntity t1, TripEntity t2) {
                    return t2.getTripDate().compareTo(t1.getTripDate());
                }
            });
        } catch (URISyntaxException | InvalidKeyException | StorageException e) {
            e.printStackTrace();
        }

        return tripEntities;
    }

    @Override
    protected void onPostExecute(List<TripEntity> tripEntities) {
        tripListActivity.loadTrips(tripEntities);
    }
}
