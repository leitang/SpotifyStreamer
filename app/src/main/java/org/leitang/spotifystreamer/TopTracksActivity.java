package org.leitang.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;

public class TopTracksActivity extends AppCompatActivity {

    private static final String TITLE_NAME = "Top 10 Tracks";

    private Bundle bundle;

    private ListView lvTracks;

    private TopTracksListAdapter topTracksListAdapter;

    private String artistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        bundle = getIntent().getExtras();
        if (bundle != null) {
            artistName = bundle.getString(MainActivity.BUNDLE_ARTIST_NAME);
        }
        initViews();
        loadData();
    }

    private void loadData() {
        if (artistName == null) {
            return;
        }

        new TopTracksFetcher().execute(artistName);
    }

    private void initViews() {
        initActionBar();
        initListView();
    }

    private void initListView() {
        lvTracks = (ListView) findViewById(R.id.lv_tracks);
        topTracksListAdapter = new TopTracksListAdapter(this, R.layout.item_top_track);
        lvTracks.setAdapter(topTracksListAdapter);
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar == null) {
            return;
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(TITLE_NAME);

        if (artistName == null) {
            return;
        }

        actionBar.setSubtitle(artistName);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class TopTracksFetcher extends AsyncTask<String, Integer, List<Track>> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<Track> doInBackground(String... params) {
            if (params[0] == null || params[0].equals("")) {
                return null;
            }
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();
            TracksPager pager = spotifyService.searchTracks(params[0]);
            Pager<Track> trackPager = pager.tracks;
            return trackPager.items;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param tracks The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(List<Track> tracks) {
            super.onPostExecute(tracks);
            topTracksListAdapter.clear();
            if (tracks != null) {
                topTracksListAdapter.addAll(tracks);
            }
            topTracksListAdapter.notifyDataSetChanged();
        }
    }
}
