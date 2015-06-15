package org.leitang.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by Lei Tang on 6/11/15.
 */
public class ArtistListAdapter extends ArrayAdapter<Artist> {

    private Context context;

    private int resource;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public ArtistListAdapter(Context context, int resource, List<Artist> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     */
    public ArtistListAdapter(Context context, int resource) {
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
        Artist artist = getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, null);
            viewHolder = new ViewHolder();
            viewHolder.ivArtist = (ImageView) convertView.findViewById(R.id.iv_artist);
            viewHolder.tvArtist = (TextView) convertView.findViewById(R.id.tv_artist);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (artist.images.size() > 0) {
            String imageUrl = artist.images.get(0).url;
            Picasso.with(context).load(imageUrl).into(viewHolder.ivArtist);
        }

        viewHolder.tvArtist.setText(artist.name);

        return convertView;
    }

    static class ViewHolder {
        ImageView ivArtist;
        TextView tvArtist;
    }
}
