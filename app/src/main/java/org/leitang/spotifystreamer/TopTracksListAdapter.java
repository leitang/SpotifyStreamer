package org.leitang.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Lei Tang on 6/15/15.
 */
public class TopTracksListAdapter extends ArrayAdapter<Track> {

    private Context context;

    private int resource;
    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     */
    public TopTracksListAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Track track = getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, null);
            viewHolder = new ViewHolder();
            viewHolder.trackImage = (ImageView) convertView.findViewById(R.id.iv_track);
            viewHolder.trackName = (TextView) convertView.findViewById(R.id.tv_track);
            viewHolder.albumName = (TextView) convertView.findViewById(R.id.tv_artist);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.trackName.setText(track.name);
        viewHolder.albumName.setText(track.album.name);

        if (track.album.images.size() > 0) {
            String imageUrl = track.album.images.get(0).url;
            Picasso.with(context).load(imageUrl).into(viewHolder.trackImage);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView trackImage;
        TextView trackName;
        TextView albumName;
    }
}
