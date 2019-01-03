package in.devco.tinsta.fragment;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

import in.devco.tinsta.R;
import in.devco.tinsta.lib.ServerConnection;
import in.devco.tinsta.lib.Util;
import in.devco.tinsta.lib.ViewAnimation;

//TODO: add video post
//TODO: add gif post
//TODO: add embed video
//TODO: add loading to post load
//TODO: add swipe down to reload

public class FragmentTimeline extends Fragment implements View.OnClickListener {

    private boolean rotate = false;

    private View back_drop;
    private View layoutImage;
    private View layoutVideo;
    private View layoutEmbedVideo;

    private FloatingActionButton fabAdd;

    public static FragmentTimeline newInstance(Bundle data) {
        FragmentTimeline fragmentTimeline = new FragmentTimeline();
        fragmentTimeline.setArguments(data);

        return fragmentTimeline;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_timeline, container, false);

        back_drop = root.findViewById(R.id.fragment_back_drop);

        fabAdd = root.findViewById(R.id.fragment_timeline_fab_add);
        FloatingActionButton fabImage = root.findViewById(R.id.fragment_timeline_fab_upload_image);
        FloatingActionButton fabVideo = root.findViewById(R.id.fragment_timeline_fab_upload_video);
        FloatingActionButton fabEmbedVideo = root.findViewById(R.id.fragment_timeline_fab_embed_video);

        layoutImage = root.findViewById(R.id.fragment_timeline_fab_upload_image_ll);
        layoutVideo = root.findViewById(R.id.fragment_timeline_fab_upload_video_ll);
        layoutEmbedVideo = root.findViewById(R.id.fragment_timeline_fab_embed_video_ll);

        ViewAnimation.initShowOut(layoutImage);
        ViewAnimation.initShowOut(layoutVideo);
        ViewAnimation.initShowOut(layoutEmbedVideo);

        back_drop.setVisibility(View.GONE);

        fabAdd.setOnClickListener(this);
        fabImage.setOnClickListener(this);
        fabVideo.setOnClickListener(this);
        fabEmbedVideo.setOnClickListener(this);
        back_drop.setOnClickListener(this);

        LinearLayout linearLayout = root.findViewById(R.id.fragment_timeline_ll);

        new TimelinePostsTask(Objects.requireNonNull(getArguments()).getInt("userId"), linearLayout, inflater, container).execute(getResources().getString(R.string.server_address) + getResources().getString(R.string.server_home));

        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_timeline_fab_add:
                toggleFabMode(v);
                break;
            case R.id.fragment_back_drop:
                toggleFabMode(fabAdd);
                break;
            case R.id.fragment_timeline_fab_upload_image:
                break;
            case R.id.fragment_timeline_fab_upload_video:
                break;
            case R.id.fragment_timeline_fab_embed_video:
                break;
        }
    }

    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(layoutImage);
            ViewAnimation.showIn(layoutVideo);
            ViewAnimation.showIn(layoutEmbedVideo);

            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.initShowOut(layoutImage);
            ViewAnimation.initShowOut(layoutVideo);
            ViewAnimation.initShowOut(layoutEmbedVideo);

            back_drop.setVisibility(View.GONE);
        }
    }

    //Timeline posts async task
    private static class TimelinePostsTask extends AsyncTask<String, String, String> implements View.OnClickListener {
        private Integer userId;
        @SuppressLint("StaticFieldLeak")
        private LinearLayout linearLayout;
        @SuppressLint("StaticFieldLeak")
        private ViewGroup container;
        private LayoutInflater inflater;
        @SuppressLint("StaticFieldLeak")
        private ImageView likeIcon;

        TimelinePostsTask(Integer userId, LinearLayout linearLayout, LayoutInflater inflater, ViewGroup container) {
            this.userId = userId;
            this.linearLayout = linearLayout;
            this.inflater = inflater;
            this.container = container;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                HashMap<String, String> hm = new HashMap<>();
                hm.put("action", "getTimelinePosts");
                hm.put("user_id", String.valueOf(userId));

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(ServerConnection.getPostDataString(hm));

                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();

                InputStream stream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();

                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                return buffer.toString();
            } catch (Exception e) {
                Log.e("Url", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject result = new JSONObject(s);

                Integer status = result.getInt("status");
                JSONArray data = result.getJSONArray("data");

                if (status == 200) {
                    if (data != null) {
                        for (int i=0; i<data.length(); i++) {
                            JSONObject post = data.getJSONObject(i);

                            View v = inflater.inflate(R.layout.content_post, container, false);

                            if (post.getString("type").equals("image")) {
                                TextView authorName         = v.findViewById(R.id.post_author_name);
                                TextView postTime           = v.findViewById(R.id.post_time);
                                TextView postDescription    = v.findViewById(R.id.post_description);
                                TextView postLikesCount     = v.findViewById(R.id.post_likes_count);
                                TextView postCommentsCount  = v.findViewById(R.id.post_comments_count);

                                ImageView authorAvatar      = v.findViewById(R.id.post_author_avatar);
                                ImageView postImage         = v.findViewById(R.id.post_image);

                                LinearLayout likeButton     = v.findViewById(R.id.post_like_click);

                                likeIcon                    = v.findViewById(R.id.post_like_icon);

                                authorName.setText(post.getString("username"));
                                postTime.setText(post.getString("time"));
                                postDescription.setText(post.getString("description"));
                                postLikesCount.setText(Util.plural(post.getInt("likes"), "like"));
                                postCommentsCount.setText(Util.plural(post.getJSONArray("comments").length(), "comment"));

                                Picasso.get().load(container.getResources().getString(R.string.server_address_main) + post.getString("avatar")).into(authorAvatar);
                                Picasso.get().load(container.getResources().getString(R.string.server_address_main) + post.getJSONArray("media_set").getJSONObject(0).getString("file")).into(postImage);

                                if (post.getBoolean("is_liked"))
                                    ImageViewCompat.setImageTintList(likeIcon, ColorStateList.valueOf(ContextCompat.getColor(container.getContext(), R.color.light_green_300)));

                                likeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.d("test123", "clicked");
                                        ImageViewCompat.setImageTintList(likeIcon, ColorStateList.valueOf(ContextCompat.getColor(container.getContext(), R.color.light_green_300)));
                                    }
                                });
                            }
                            linearLayout.addView(v);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Url", e.toString());
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.post_like_click:

                    break;
            }
        }
    }
}