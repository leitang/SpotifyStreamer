package org.leitang.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.RetrofitError;


public class MainActivity extends AppCompatActivity implements TextWatcher, AdapterView.OnItemClickListener{

    @InjectView(R.id.et_search) EditText tvSearch;

    @InjectView(R.id.lv_artist) ListView lvArtist;

    @InjectView(R.id.pb_loading) ProgressBar pbLoading;

    private ArtistListAdapter artistListAdapter;

    private ArrayList<ParcelableArtist> parcelableArtistList;

    private String keyword;

    public static final String BUNDLE_ARTIST_NAME = "artist";
    public static final String BUNDLE_LIST_ARTIST = "artists";
    public static final String BUNDLE_KEYWORD = "key";

    static class ParcelableArtist implements Parcelable {

        String name;
        String url;

        public ParcelableArtist(String name, String url) {
            this.name = name;
            this.url = url;
        }


        protected ParcelableArtist(Parcel in) {
            name = in.readString();
            url = in.readString();
        }

        public static final Creator<ParcelableArtist> CREATOR = new Creator<ParcelableArtist>() {
            @Override
            public ParcelableArtist createFromParcel(Parcel in) {
                return new ParcelableArtist(in);
            }

            @Override
            public ParcelableArtist[] newArray(int size) {
                return new ParcelableArtist[size];
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
            dest.writeString(name);
            dest.writeString(url);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initViews();

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_LIST_ARTIST)) {
            ArrayList<ParcelableArtist> artists = savedInstanceState.getParcelableArrayList(BUNDLE_LIST_ARTIST);
            parcelableArtistList = artists;
            if (artists != null) {
                artistListAdapter.addAll(artists);
                artistListAdapter.notifyDataSetChanged();
            }

            String keyword = savedInstanceState.getString(BUNDLE_KEYWORD);
            if (keyword != null) {
                this.keyword = keyword;
            }
        }
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_LIST_ARTIST, parcelableArtistList);
        outState.putString(BUNDLE_KEYWORD, keyword);
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        tvSearch.addTextChangedListener(this);
        artistListAdapter = new ArtistListAdapter(this, R.layout.item_artist_search_result);
        lvArtist.setAdapter(artistListAdapter);
        lvArtist.setOnItemClickListener(this);
    }

    /**
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * are about to be replaced by new text with length <code>after</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     *
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * This method is called to notify you that, somewhere within
     * <code>s</code>, the text has been changed.
     * It is legitimate to make further changes to <code>s</code> from
     * this callback, but be careful not to get yourself into an infinite
     * loop, because any changes you make will cause this method to be
     * called again recursively.
     * (You are not told where the change took place because other
     * afterTextChanged() methods may already have made other changes
     * and invalidated the offsets.  But if you need to know here,
     * you can use {@link Spannable#setSpan} in {@link #onTextChanged}
     * to mark your place and then look up from here where the span
     * ended up.
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().equals(keyword)) {
            new ArtistFetcher().execute(s.toString());
        }

        keyword = s.toString();
    }

    private class ArtistFetcher extends AsyncTask<String, Integer, ArrayList<ParcelableArtist>> {
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
        protected ArrayList<ParcelableArtist> doInBackground(String... params) {
            if (params[0] == null || params[0].equals("")) {
                return null;
            }
            Pager<Artist> artistPager;
            try {
                SpotifyApi spotifyApi = new SpotifyApi();
                SpotifyService spotifyService = spotifyApi.getService();
                ArtistsPager pager = spotifyService.searchArtists(params[0]);
                artistPager = pager.artists;
            } catch (RetrofitError e) {
                e.printStackTrace();
                return null;
            }

            return serializeArtist(artistPager.items);
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
            pbLoading.setVisibility(View.VISIBLE);
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param artists The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(ArrayList<ParcelableArtist> artists) {
            super.onPostExecute(artists);
            pbLoading.setVisibility(View.GONE);
            parcelableArtistList = artists;
            artistListAdapter.clear();
            if (artists != null) {
                artistListAdapter.addAll(artists);
            }
            artistListAdapter.notifyDataSetChanged();

        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String artistName = parcelableArtistList.get(position).name;
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_ARTIST_NAME, artistName);
        intent.setClass(this, TopTracksActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private ArrayList<ParcelableArtist> serializeArtist(List<Artist> artists) {
        ArrayList<ParcelableArtist> parcelableArtists = new ArrayList<>();

        String name;
        String url;

        for (Artist artist : artists) {
            name = artist.name;
            if (artist.images.size() > 0) {
                url = artist.images.get(0).url;
            } else {
                url = null;
            }

            ParcelableArtist parcelableArtist = new ParcelableArtist(name, url);
            parcelableArtists.add(parcelableArtist);
        }

        return parcelableArtists;
    }
}
