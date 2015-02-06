/*
 * Copyright 2014 Semyon Proshev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tcity.android.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.tcity.android.R;
import com.tcity.android.Status;

import org.jetbrains.annotations.NotNull;

public class Common {

    @NotNull
    public static final String TEAMCITY_DATE_FORMAT = "yyyyMMdd'T'HHmmssZ";

    private Common() {
    }

    public static void setRefreshing(@NotNull final Context context,
                                     @NotNull final SwipeRefreshLayout layout,
                                     @NotNull final TextView emptyView,
                                     final boolean refreshing) {
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (layout.isRefreshing() ^ refreshing) {
                            layout.setRefreshing(refreshing);

                            if (refreshing) {
                                emptyView.setText(R.string.loading);
                            } else {
                                if (Common.isNetworkAvailable(context)) {
                                    emptyView.setText(R.string.empty);
                                } else {
                                    emptyView.setText(R.string.network_is_unavailable);
                                }
                            }
                        }
                    }
                }, 500
        );  // https://code.google.com/p/android/issues/detail?id=77712
    }

    public static boolean isNetworkAvailable(@NotNull Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static int loadTextColor(@NotNull Status status, @NotNull Context context) {
        return context.getResources().getColor(loadColorResource(status, android.R.color.black));
    }

    public static int loadBackgroundColor(@NotNull Status status, @NotNull Context context) {
        return context.getResources().getColor(loadColorResource(status, android.R.color.transparent));
    }

    private static int loadColorResource(@NotNull Status status, int defaultValue) {
        switch (status) {
            case RUNNING:
                return R.color.blue;
            case SUCCESS:
                return R.color.green;
            case FAILURE:
            case ERROR:
            case WARNING:
                return R.color.red;
            default:
                return defaultValue;
        }
    }

    public static class PopupMenuListener implements PopupMenu.OnMenuItemClickListener {

        @NotNull
        private final Context myContext;

        @NotNull
        private final String myUrl;

        public PopupMenuListener(@NotNull Context context, @NotNull String url) {
            myContext = context;
            myUrl = url;
        }

        @Override
        public boolean onMenuItemClick(@NotNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_share_link:
                    onShareClick();

                    return true;
                case R.id.menu_open_in_browser:
                    onOpenClick();

                    return true;
                default:
                    return false;
            }
        }

        private void onShareClick() {
            Intent intent = new Intent(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    myUrl
            );

            myContext.startActivity(
                    Intent.createChooser(
                            intent,
                            myContext.getResources().getString(R.string.share_link)
                    )
            );
        }

        private void onOpenClick() {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(myUrl));

            myContext.startActivity(intent);
        }
    }
}
