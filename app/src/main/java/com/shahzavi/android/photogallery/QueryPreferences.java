package com.shahzavi.android.photogallery;

import android.content.Context;

public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY="searchQuery";
    private static final String PREF_LAST_RESULT="lastResultId";
            public static  String getStoredQuery(Context context) {
                return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(PREF_SEARCH_QUERY, null);
                //you can call this method from any context of your app
            }
            public static void setStoredQuery(Context context,String query)
            {
                context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE).edit().putString(PREF_SEARCH_QUERY,query).apply();
            }
            public static String getLastResultId(Context context)
            {
                return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(PREF_LAST_RESULT,null);
            }
            public static void setLastResultId(Context context,String lastResultId)
            {
                context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).edit().putString(PREF_LAST_RESULT,lastResultId)
                        .apply();
            }
}
