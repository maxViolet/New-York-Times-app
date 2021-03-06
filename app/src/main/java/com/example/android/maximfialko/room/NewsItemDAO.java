package com.example.android.maximfialko.room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Observable;
import io.reactivex.Single;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface NewsItemDAO {

    @Query("SELECT * FROM newsItemDB")
    List<NewsItemDB> getAll();

    @Query("SELECT * FROM newsItemDB")
    Single<List<NewsItemDB>> getAllSingle();

    @Query("SELECT * FROM newsItemDB")
    Observable<List<NewsItemDB>> getAllObservable();

    @Insert(onConflict = REPLACE)
    void insertAll(NewsItemDB... newsItemDBs);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateNews(NewsItemDB newsItemDB);

    @Delete
    void delete(NewsItemDB newsItemDB);

    @Query("DELETE FROM NewsItemDB")
    void deleteAll();

    @Query("SELECT * FROM NewsItemDB WHERE id = :id")
    NewsItemDB findNewsById(int id);

    @Query("SELECT * FROM NewsItemDB WHERE title LIKE :title LIMIT 1")
    NewsItemDB findNewsByTitle(String title);

    @Query("SELECT * FROM NewsItemDB WHERE title IN (:titles)")
    List<NewsItemDB> loadAllByTitles(String[] titles);

}
