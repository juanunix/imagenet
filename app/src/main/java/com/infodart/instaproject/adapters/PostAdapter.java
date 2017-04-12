package com.infodart.instaproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.infodart.instaproject.R;
import com.infodart.instaproject.config.Constants;
import com.infodart.instaproject.interfaces.OnLoadMoreListener;
import com.infodart.instaproject.model.Post;
import com.infodart.instaproject.ui.InstaHome;
import com.infodart.instaproject.ui.PostActivity;
import com.infodart.instaproject.utils.Logger;
import com.infodart.instaproject.utils.TimeAgo;
import com.infodart.instaproject.utils.Utils;
import com.infodart.instaproject.utils.VolleySingleton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by navrajsingh on 11/14/16.
 */

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    RecyclerView mRecyclerView;
    public static ArrayList<Post> posts;
    private Context context;
    private ImageLoader mImageLoader;
    public HashMap<String,Integer> likesMap;
    public PostAdapter(Context context) {
        this.context = context;

    }
    public PostAdapter(Context context, ArrayList<Post> posts,RecyclerView recyclerView,HashMap<String,Integer> likesMap) {
        this.context = context;
        this.posts = posts;
        mRecyclerView = recyclerView;
        mImageLoader = VolleySingleton.getInstance(context).getImageLoader();
        this.likesMap = likesMap;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                super.onScrolled(recyclerView, dx, dy);

/*
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
*/
            }
        });
    }

    /*@Override
    public int getItemCount() {
        return (null != posts ? posts.size() : 0);

    }
    */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof PostViewHolder) {
            final Post model = posts.get(position);
            //final Post model = posts.get(posts.size()-position-1);
            PostAdapter.PostViewHolder mainHolder = (PostAdapter.PostViewHolder) holder;
            Gson gson = new Gson();
            final String postJson = gson.toJson(model);
            //mainHolder.name.setText(model.getPublisherName());
            mainHolder.text.setText(Html.fromHtml(model.getText()));
            //mainHolder.text.setText(Utils.TimestampTotDate(Long.parseLong(model.getTimestamp()),""));
            try {
                mainHolder.timestamp.setText(TimeAgo.getTimeAgo(Long.parseLong(model.getTimestamp()), context));
            }
            catch (Exception e){
                e.printStackTrace();
            }
            Linkify.addLinks(mainHolder.text, Linkify.ALL);

            mainHolder.layoutPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, PostActivity.class);
                    intent.putExtra("post",postJson);
                    context.startActivity(intent);

                }
            });

            Uri imageUri = Uri.parse(model.getUrl());
            mainHolder.imgFresco.setImageURI(imageUri);
            mainHolder.imgLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    model.setLiked(!model.getLiked());
                    int like = model.getLiked() ? 1 :0 ;
                    if(model.getLiked())
                        model.setLikesCount(model.getLikesCount()+1);
                    else
                        model.setLikesCount(model.getLikesCount()-1);

                    likesMap.put(model.get_id(),like);
                    SharedPreferences pref = context.getSharedPreferences("likes", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    for (String s : likesMap.keySet()) {
                        editor.putInt(s, likesMap.get(s));
                    }
                    editor.commit();
                    writePostsToStorage();
                    int op = 0;
                    if(model.getLiked())
                        op = 1;
                    executeLike(model.get_id(),op,"");
                    notifyDataSetChanged();
                }
            });
            try {
                Uri imageUriStar ;
                Uri uri = new Uri.Builder()
                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                        .path(String.valueOf(R.drawable.user1))
                        .build();

                mainHolder.imgFrescoStar.setImageURI(uri);
                mainHolder.likesCount.setText(model.getLikesCount() + " Likes");
                mainHolder.commentCount.setText(model.getCommentsCount() + " Comments");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if(model.getLiked()){
                        mainHolder.imgLike.setImageDrawable(context.getResources().getDrawable(R.drawable.likeclicked,
                                context.getApplicationContext().getTheme()));
                    }
                    else
                        mainHolder.imgLike.setImageDrawable(context.getResources().getDrawable(R.drawable.like,
                                context.getApplicationContext().getTheme()));
                } else {
                    if(model.getLiked())
                        mainHolder.imgLike.setImageDrawable(context.getResources().getDrawable(R.drawable.likeclicked));
                    else
                        mainHolder.imgLike.setImageDrawable(context.getResources().getDrawable(R.drawable.like));
                }

                if(model.getPublisherImage()!=null && model.getPublisherImage().length()>0) {
                    imageUriStar = Uri.parse(model.getPublisherImage());
                    mainHolder.imgFrescoStar.setImageURI(imageUriStar);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }


    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // This method will inflate the custom layout and return as viewholder
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup ;
        if (viewType == VIEW_TYPE_ITEM) {
            //View view = LayoutInflater.from(context).inflate(R.layout.postrow, parent, false);
            mainGroup = (ViewGroup) mInflater.inflate(
                    R.layout.postrow, viewGroup, false);;
            PostAdapter.PostViewHolder listHolder = new PostAdapter.PostViewHolder(mainGroup);
            return listHolder;
            //return new UserViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.load_more, viewGroup, false);
            return new LoadingViewHolder(view);
            //View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_loading_item, parent, false);
            //return new LoadingViewHolder(view);
        }




        return null;

    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);
        }
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView text;
        public TextView likesCount;
        public TextView commentCount;
        public TextView timestamp;
        public LinearLayout layoutPost;
        public NetworkImageView imgStar;
        public NetworkImageView imgUrl;
        public SimpleDraweeView imgFresco;
        public SimpleDraweeView imgFrescoStar;
        public ImageView imgLike;
        public PostViewHolder(View view) {
            super(view);
            // Find all views ids

            //this.imgUrl = (NetworkImageView) view.findViewById(R.id.imgUrl);
            this.imgFresco = (SimpleDraweeView) view.findViewById(R.id.sdvImage);
            this.imgFrescoStar = (SimpleDraweeView) view.findViewById(R.id.sdvStar);
            this.layoutPost = (LinearLayout) view.findViewById(R.id.layoutPost);
            this.imgLike = (ImageView) view.findViewById(R.id.imgLike);

            this.name = (TextView) view
                    .findViewById(R.id.lblStarName);
            this.timestamp = (TextView) view
                    .findViewById(R.id.timestamp);

            this.text = (TextView) view
                    .findViewById(R.id.text);
            this.text.setMovementMethod(LinkMovementMethod.getInstance());
            this.likesCount = (TextView) view.findViewById(R.id.txtLikes);
            this.commentCount = (TextView) view.findViewById(R.id.txtComments);

        }
    }
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem,
            totalItemCount;
    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }
    @Override public int getItemViewType(int position) {
        return posts.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return posts == null ? 0 : posts.size();
    }
    public void setLoaded() {
        isLoading = false;
    }

    private void writePostsToStorage() {
        Gson gson = new Gson();
        String postsString = gson.toJson(posts);
        Logger.e(postsString);
    }

    private void executeLike(String postId,int op,String userId) {
        String url  = String.format(Constants.GetServerURL() +
                        Constants.URL_POSTS + "/%s/like/?op=%d",postId, op);
        Request request = VolleySingleton.getInstance(context).getStringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Logger.v(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Logger.e(error.getMessage());

            }
        });

        VolleySingleton.getInstance(context).addToRequestQueue(request);

    }
}
