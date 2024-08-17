package com.shahzavi.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    public static int page_no=1;
    private final static String TAG="PhotoGalleryFragment";
    PhotoAdapter mPhotoAdapter;
    List<GalleryItem> mItems=new ArrayList<>();
    RecyclerView mRecyclerView;
    ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    boolean isLoading=true;
    public static Fragment newInstance()
    {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //MENU
        updateItems();
      //  Intent i=PollService.newIntent(getActivity());
       // getActivity().startService(i);
        Handler responseHandler=new Handler();
        mThumbnailDownloader=new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloaderListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownload(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable=new BitmapDrawable(getResources(),thumbnail);
                target.bindGalleryItems(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
       getActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_photo_gallery,menu);
                MenuItem searchItem=menu.findItem(R.id.menu_item_search);
                SearchView searchView=(SearchView) searchItem.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Log.d(TAG,"QueryTextSubmited: "+query);
                        QueryPreferences.setStoredQuery(getActivity(),query);
                        SingletonClass.newInstance(getActivity()).getRequestQueue().cancelAll(TAG);
                        page_no=1;
                        mItems.clear();
                        searchView.clearFocus();
                        updateItems();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        Log.d(TAG,"QueryTextChanged");
                        return false;
                    }
                });
                searchView.setOnSearchClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String query=QueryPreferences.getStoredQuery(getActivity());
                        searchView.setQuery(query,false);
                    }
                });
            }

           @Override
           public void onPrepareMenu(@NonNull Menu menu) {
               MenuProvider.super.onPrepareMenu(menu);
               MenuItem menuItem=menu.findItem(R.id.menu_item_toggle_polling);
               Log.d(TAG,"Toggling working"+PollService1.isJobSchedulerOn(getActivity()));
               if(PollService1.isJobSchedulerOn(getActivity()))
               {
                   menuItem.setTitle(R.string.stop_polling);
               }
               else{
                   menuItem.setTitle(R.string.start_polling);}
           }

           @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.menu_item_clear)
                {
                    QueryPreferences.setStoredQuery(getActivity(),null);
                    mItems.clear();
                    updateItems();
                    return true;
                }
                else if(menuItem.getItemId()==R.id.menu_item_toggle_polling)
                {
                    Log.d(TAG,"ServiceOnOFF");
                    boolean shouldStartAlarm=!(PollService1.isJobSchedulerOn(getActivity()));
                    Log.d(TAG,"alarm: "+shouldStartAlarm);
                    PollService1.setJobScheduler(getActivity(),shouldStartAlarm);
                    getActivity().invalidateMenu();
                    return true;
                }
                return false;
            }
        });
    }
    public void updateUI()
    {
        if(mPhotoAdapter==null){
            mPhotoAdapter=new PhotoAdapter(mItems);
        mRecyclerView.setAdapter(mPhotoAdapter);}
        else
        {
            //Parcelable Recyclerviewstate=mRecyclerView.getLayoutManager().onSaveInstanceState();
            mPhotoAdapter.notifyDataSetChanged();
            //mRecyclerView.getLayoutManager().onRestoreInstanceState(Recyclerviewstate);
        }

    }
    public  void fetchItems(String url)
    {
        Log.d(TAG,"fetching for url; "+url);
        JsonObjectRequest jsonArrayRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG,"onResponseCalled");//https://jsonplaceholder.typicode.com/todos/1
                try {
                    JSONArray jsonArray=response.getJSONArray("hits");
                    for(int i=0;i<jsonArray.length();i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        GalleryItem galleryItem = new GalleryItem();
                        galleryItem.setCaption(jsonObject.getString("tags"));
                        galleryItem.setId(jsonObject.getString("id"));
                        galleryItem.setUrl(jsonObject.getString("previewURL"));
                       mItems.add(galleryItem);

                    }
                    Log.i(TAG,"item id: "+mItems.get(0).getId());
                    Log.d(TAG,"allitems loaded");
                    updateUI();
                    isLoading=true;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"error occured");
            }
        });
        jsonArrayRequest.setTag(TAG);
        SingletonClass.newInstance(getActivity()).getRequestQueue().add(jsonArrayRequest);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_gallery,container,false);
       mRecyclerView=v.findViewById(R.id.photogalleryfragment_recyclerview);
       mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
       mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
           @Override
           public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
               super.onScrollStateChanged(recyclerView, newState);

           }

           @Override
           public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
               super.onScrolled(recyclerView, dx, dy);
               GridLayoutManager gridLayoutManager=(GridLayoutManager) mRecyclerView.getLayoutManager();
             if(isLoading){
               if(gridLayoutManager!=null&& gridLayoutManager.findLastVisibleItemPosition()==mItems.size()-1)
               { isLoading=false;
                   page_no++;
                   updateItems();
               }
             }

           }
       });
   updateUI();
       return v;
    }
    // IMAGEVIEW VIEWHOLDER
    private  class PhotoHolder extends RecyclerView.ViewHolder
    {
       private ImageView mImageView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mImageView=(ImageView) itemView.findViewById(R.id.fragment_photo_gallery_imageview);
        }
        public  void bindGalleryItems(Drawable drawable)
        {
         mImageView.setImageDrawable(drawable);
        }
    }
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>
    {
        List<GalleryItem> galleryItemList;
        public PhotoAdapter(List<GalleryItem> items)
        {
            galleryItemList=items;
        }
        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v=LayoutInflater.from(getActivity()).inflate(R.layout.gallery_item,parent,false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem=galleryItemList.get(position);
            Drawable resorce= ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_name);
            holder.bindGalleryItems(resorce);
            mThumbnailDownloader.queueThumbnail(holder,galleryItem.getUrl());
        }
        @Override
        public int getItemCount() {
            return galleryItemList.size();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        mThumbnailDownloader.clearQueue();
        SingletonClass.newInstance(getActivity()).getRequestQueue().cancelAll(TAG);
    }
    public void updateItems()
    {
        String query=QueryPreferences.getStoredQuery(getActivity());
        String url;
        if(query==null)
            url=PhotoFetch.getUrlString(""+page_no,null);
        else
           url= PhotoFetch.getUrlString(""+page_no,query);
        fetchItems(url);
    }
}
