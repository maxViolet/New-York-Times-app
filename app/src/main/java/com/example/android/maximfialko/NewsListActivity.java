package com.example.android.maximfialko;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.maximfialko.data.Margins;
import com.example.android.maximfialko.data.NewsAdapter;
import com.example.android.maximfialko.data.NewsItem;
import com.example.android.maximfialko.data.NewsItemList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NewsListActivity extends AppCompatActivity {

    @Nullable
    private NewsAdapter adapter;
    @Nullable
    private Disposable disposable;

    //переход на DetailedNewsActivity
    private final NewsAdapter.OnItemClickListener clickListener = position -> openDetailedNewsActivity(position.getItemId());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list_activity);

        RecyclerView rv = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new NewsAdapter(this, new ArrayList<>(), clickListener);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            rv.setLayoutManager(new LinearLayoutManager(this));
        } else {
            rv.setLayoutManager(new GridLayoutManager(this, 2));
        }

        rv.setAdapter(adapter);

        //add Margins to Recycler View
        Margins decoration = new Margins(24, 1);
        rv.addItemDecoration(decoration);
    }

    //асинхронная загрузка списка новостей
    private void loadItems() {
        disposable = Observable.fromCallable(() -> {
            Log.d("addNews", "Generate news: " + Thread.currentThread().getName());
            //асинхронное получение данных из списка новостей
            return new NewListCallable(NewsItemList.generateNews());
        })
                //указываем поток, где будет исполняться код
                .subscribeOn(Schedulers.io())
                //указываем поток, куда приходят данные, все что ниже ObserveOn будет выполняться в потоке, который здесь прописан
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(news -> {
                    Log.d("addNews", "Subscribe: " + Thread.currentThread().getName());
                    getAdapter().add(news.data);
                }, error -> {
                    Log.d("onError", error.toString());
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadItems();
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposable = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter = null;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //метод перехода на активити DetaliedNews
    public void openDetailedNewsActivity(int id) {
        DetailedNewsActivity.start(this, id);
    }

    //Callable обертка для метода NewsAdapter.add
    private static class NewListCallable implements Callable<List<NewsItem>> {
        private final List<NewsItem> data;

        public NewListCallable(List<NewsItem> data) {
            this.data = data;
        }

        @Override
        public List<NewsItem> call() throws Exception {
            return data;
        }
    }

    public NewsAdapter getAdapter() {
        return adapter;
    }
}


