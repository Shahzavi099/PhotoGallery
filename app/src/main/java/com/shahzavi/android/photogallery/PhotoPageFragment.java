package com.shahzavi.android.photogallery;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class PhotoPageFragment extends VisibleFragment{
    private static final String ARG_URI="photo_page_url";
    private WebView mWebView;
    private Uri mUri;
    private ProgressBar mProgressBar;
    public static PhotoPageFragment newInstance(Uri uri)
    {
        Bundle arg=new Bundle();
        arg.putParcelable(ARG_URI,uri);
        PhotoPageFragment fragment=new PhotoPageFragment();
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri=getArguments().getParcelable(ARG_URI);

    }

    @SuppressLint({"SetJavaScriptEnabled", "MissingInflatedId"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_page,container,false);
        mProgressBar=v.findViewById(R.id.fragment_photo_page_progress_bar);
        mProgressBar.setMax(100);
       mWebView=v.findViewById(R.id.fragment_photo_page_webview);
       mWebView.getSettings().setJavaScriptEnabled(true);//to execute javascript
       mWebView.setWebViewClient(new WebViewClient(){
           @Override
           public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
               if(!(Objects.equals(request.getUrl().getScheme(), "http"))||!(Objects.equals(request.getUrl().getScheme(), "http"))){
                   Intent i=new Intent(Intent.ACTION_VIEW,request.getUrl());
               startActivity(i);
               return true;}
               else{
               return false;}
           }
       });
       mWebView.setWebChromeClient(new WebChromeClient(){
           @Override
           public void onProgressChanged(WebView view, int newProgress) {
               if(newProgress==100)
               {
                   mProgressBar.setVisibility(View.GONE);
               }
               else{
                   mProgressBar.setVisibility(View.VISIBLE);
                   mProgressBar.setProgress(newProgress);
               }
           }

           @Override
           public void onReceivedTitle(WebView view, String title) {
               AppCompatActivity activity=(AppCompatActivity) getActivity();
               activity.getSupportActionBar().setTitle(title);
           }
       });
       mWebView.loadUrl(mUri.toString());
       mWebView.setOnKeyListener(new View.OnKeyListener() {
           @Override
           public boolean onKey(View view, int i, KeyEvent keyEvent) {
               if(keyEvent.getAction()==KeyEvent.ACTION_DOWN)
                   if(i==KeyEvent.KEYCODE_BACK)
                       if(mWebView.canGoBack()){
                   mWebView.goBack();}
               else{
                   requireActivity().onBackPressed();
                       }

                 return true;
           }
       });
        return v;
    }
}
