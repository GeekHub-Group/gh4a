/*
 * Copyright 2011 Azwan Adli Abdullah
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
package com.gh4a.activities;

import java.util.Map;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gh4a.BaseActivity;
import com.gh4a.Constants;
import com.gh4a.R;
import com.gh4a.loader.GistLoader;
import com.gh4a.loader.LoaderCallbacks;
import com.gh4a.loader.LoaderResult;
import com.gh4a.utils.StringUtils;
import com.gh4a.utils.UiUtils;

public class GistActivity extends BaseActivity implements View.OnClickListener {
    private String mGistId;
    private String mUserLogin;
    private Gist mGist;

    private LoaderCallbacks<Gist> mGistCallback = new LoaderCallbacks<Gist>() {
        @Override
        public Loader<LoaderResult<Gist>> onCreateLoader(int id, Bundle args) {
            return new GistLoader(GistActivity.this, mGistId);
        }
        @Override
        public void onResultReady(LoaderResult<Gist> result) {
            boolean success = !result.handleError(GistActivity.this);
            if (success) {
                fillData(result.getData());
            }
            setContentEmpty(!success);
            setContentShown(true);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGistId = getIntent().getExtras().getString(Constants.Gist.ID);
        mUserLogin = getIntent().getExtras().getString(Constants.User.LOGIN);

        if (hasErrorView()) {
            return;
        }

        setContentView(R.layout.gist);
        setContentShown(false);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setTitle(getString(R.string.gist_title, mGistId));
        mActionBar.setSubtitle(mUserLogin);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        getSupportLoaderManager().initLoader(0, null, mGistCallback);
    }

    private void fillData(final Gist gist) {
        mGist = gist;

        TextView tvDesc = (TextView) findViewById(R.id.tv_desc);
        tvDesc.setText(TextUtils.isEmpty(gist.getDescription())
                ? getString(R.string.gist_no_description) : gist.getDescription());

        TextView tvCreatedAt = (TextView) findViewById(R.id.tv_created_at);
        tvCreatedAt.setText(StringUtils.formatRelativeTime(this, gist.getCreatedAt(), true));

        Map<String, GistFile> files = gist.getFiles();
        if (files != null && !files.isEmpty()) {
            ViewGroup container = (ViewGroup) findViewById(R.id.file_container);
            LayoutInflater inflater = getLayoutInflater();

            for (GistFile gistFile : files.values()) {
                TextView rowView = (TextView) inflater.inflate(R.layout.selectable_label,
                        container, false);

                rowView.setText(gistFile.getFilename());
                rowView.setTextColor(UiUtils.resolveColor(this, android.R.attr.textColorPrimary));
                rowView.setOnClickListener(this);
                rowView.setTag(gistFile);
                container.addView(rowView);
            }
        } else {
            findViewById(R.id.file_card).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, GistViewerActivity.class);
        GistFile gist = (GistFile) view.getTag();
        intent.putExtra(Constants.User.LOGIN, mUserLogin);
        intent.putExtra(Constants.Gist.FILENAME, gist.getFilename());
        intent.putExtra(Constants.Gist.ID, mGistId);
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, R.id.share, 0, R.string.share)
                .setIcon(R.drawable.social_share);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.share_gist_subject, mGistId, mUserLogin));
                shareIntent.putExtra(Intent.EXTRA_TEXT,  mGist.getHtmlUrl());
                shareIntent = Intent.createChooser(shareIntent, getString(R.string.share_title));
                startActivity(shareIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Intent navigateUp() {
        Intent intent = new Intent(this, GistListActivity.class);
        intent.putExtra(Constants.User.LOGIN, mUserLogin);
        return intent;
    }
}