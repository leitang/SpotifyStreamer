package org.leitang.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.RetrofitError;

public class TopTracksActivity extends AppCompatActivity {

    @InjectView(R.id.pb_loading) ProgressBar progressBar;

    @InjectView(R.id.lv_tracks) ListView lvTracks;

    private static final String TITLE_NAME = "Top 10 Tracks";

    private static final String BUNDLE_LIST_TRACK = "tracks";

    private Bundle bundle;

    private TopTracksListAdapter topTracksListAdapter;

    private String artistName;

    private ArrayList<ParcelableTrack> mTracks;

    static class ParcelableTrack implements Parcelable {

        String trackName;
        String albumName;
        String url;

        public ParcelableTrack(String trackName, String albumName, String url) {
            this.trackName = trackName;
            this.albumName = albumName;
            this.url = url;
        }

        protected ParcelableTrack(Parcel in) {
            trackName = in.readString();
            albumName = in.readString();
            url = in.readString();
        }

        public static final Creator<ParcelableTrack> CREATOR = new Creator<ParcelableTrack>() {
            @Override
            public ParcelableTrack createFromParcel(Parcel in) {
                return new ParcelableTrack(in);
            }

            @Override
            public ParcelableTrack[] newArray(int size) {
                return new ParcelableTrack[size];
            }
        };

        /**
         * Describe the kinds of special objects contained in this Parcelable's
         * marshalled representation.
         *
         * @return a bitmask indicating the set of special object types marshalled
         * by the Parcelable.
         */
        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * Flatten this object in to a Parcel.
         *
         * @param dest  The Parcel in which the object should be written.
         * @param flags Additional flags about how the object should be written.
         *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(trackName);
            dest.writeString(albumName);
            dest.writeString(url);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        ButterKnife.inject(this);
        initViews();
        bundle = getIntent().getExtras();
        if (bundle != null) {
            artistName = bundle.getString(MainActivity.BUNDLE_ARTIST_NAME);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_LIST_TRACK)) {
            ArrayList<ParcelableTrack> tracks = savedInstanceState.getParcelableArrayList(BUNDLE_LIST_TRACK);
            if (tracks != null) {
                mTracks = tracks;
                topTracksListAdapter.addAll(mTracks);
                topTracksListAdapter.notifyDataSetChanged();
            } else {
                loadData();
            }
        } else {
            loadData();
        }

    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_LIST_TRACK, mTracks);
        super.onSaveInstanceState(outState);
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

    private class TopTracksFetcher extends AsyncTask<String, Integer, ArrayList<ParcelableTrack>> {

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
        protected ArrayList<ParcelableTrack> doInBackground(String... params) {
            if (params[0] == null || params[0].equals("")) {
                return null;
            }
            Pager<Track> trackPager;
            try {
                SpotifyApi spotifyApi = new SpotifyApi();
                SpotifyService spotifyService = spotifyApi.getService();
                TracksPager pager = spotifyService.searchTracks(params[0]);
                trackPager = pager.tracks;
            } catch (RetrofitError e) {
                e.printStackTrace();
                return null;
            }

            return serializeTrack(trackPager.items);
        }

        /**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);

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
        protected void onPostExecute(ArrayList<ParcelableTrack> tracks) {
            super.onPostExecute(tracks);
            progressBar.setVisibility(View.GONE);
            mTracks = tracks;
            topTracksListAdapter.clear();
            if (tracks != null) {
                topTracksListAdapter.addAll(tracks);
            }
            topTracksListAdapter.notifyDataSetChanged();
        }
    }

    private ArrayList<ParcelableTrack> serializeTrack(List<Track> tracks) {
        ArrayList<ParcelableTrack> parcelableTracks = new ArrayList<>();

        String trackName;
        String albumName;
        String url;

        for (Track track : tracks) {
            trackName = track.name;
            albumName = track.album.name;
            if (track.album.images.size() > 0) {
                url = track.album.images.get(0).url;
            } else {
                url = null;
            }

            ParcelableTrack parcelableTrack = new ParcelableTrack(trackName, albumName, url);
            parcelableTracks.add(parcelableTrack);
        }

        return parcelableTracks;
    }
}
