package com.example.android.maximfialko;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.android.maximfialko.Utils.DensityPixelMath;
import com.example.android.maximfialko.network.DTOmodels.MapperDtoToNews;
import com.example.android.maximfialko.Utils.Visibility;
import com.example.android.maximfialko.Utils.Margins;
import com.example.android.maximfialko.data.NewsAdapter;
import com.example.android.maximfialko.data.NewsItem;
import com.example.android.maximfialko.network.RestApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NewsListActivity extends AppCompatActivity {

    private NewsAdapter adapter;
    private ProgressBar progress;
    private RecyclerView rv;
    private View error;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        Log.d("http", "main onCREATE()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list_activity);

        progress = (ProgressBar) findViewById(R.id.progress);
        rv = (RecyclerView) findViewById(R.id.recycler_view);
        error = (View) findViewById(R.id.lt_error);

        //ps to dp //util class
        DensityPixelMath DPmath = new DensityPixelMath(getApplicationContext());
        //add Margins to Recycler View
        Margins decoration = new Margins((int) DPmath.dpFromPx(44), 1);
        rv.addItemDecoration(decoration);

        adapter = new NewsAdapter(this, new ArrayList<>(), clickListener);
        rv.setAdapter(adapter);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            rv.setLayoutManager(new LinearLayoutManager(this));
        } else {
            rv.setLayoutManager(new GridLayoutManager(this, 2));
        }
    }

    //переход на DetailedNewsActivity через WebView
    private final NewsAdapter.OnItemClickListener clickListener = position -> {
//        Log.d("http", "go to DETAILED ACTIVITY");
        openDetailedNewsActivity(position.getTextUrl());};

    //асинхронная загрузка списка новостей
    private void loadItems(@NonNull String category) {
        showProgress(true);
//        Log.d("http", "START showProgress");
//        Log.d("http", "START HTTP QUERY");
        final Disposable searchDisposable = RestApi.getInstance()
                .topStoriesEndpoint()
                .getNews(category)
                .map(response -> MapperDtoToNews.map(response.getNews()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newsItems -> setupNews(newsItems), throwable -> NewsListActivity.this.handleError(throwable));
//        showProgress(false);
//        Log.d("http", "STOP HTTP QUERY");

        compositeDisposable.add(searchDisposable);
        Visibility.setVisible(rv, true);
//        Log.d("http", "SHOW RECYCLER LIST");
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.d("http", "main onSTART()");
        //TODO: SpinnerAdapter for category usage
        loadItems("home");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.d("http", " main onSTOP()");
//        showProgress(false);
        compositeDisposable.dispose();
    }

    private void handleError(Throwable throwable) {
        if (throwable instanceof IOException) {
            showError(true);
            return;
        }
        showError(true);
    }

    private void setupNews(List<NewsItem> newsItems) {
//        Log.d("http", "setupNEWS");
//        showProgress(false);

//        updateItems(newsItems);
        if (adapter != null) adapter.replaceItems(newsItems);
        Visibility.setVisible(rv, true);
        showProgress(false);
//        Log.d("http", "STOP showProgress");
    }

    //метод перехода на активити DetailedNews
    public void openDetailedNewsActivity(String url) {
        DetailedNewsActivity.start(this, url);
    }

    public void showProgress(boolean show) {
        Visibility.setVisible(progress, show);
        Visibility.setVisible(rv, !show);
        Visibility.setVisible(error, !show);
    }

    void showError(boolean show) {
        /*if (show = true) Log.d("http", "showProgress");
            else Log.d("http", "STOP showProgress");*/
//        Log.d("http", "showProgress");
        Visibility.setVisible(progress, !show);
        Visibility.setVisible(rv, !show);
        Visibility.setVisible(error, show);
    }
}


