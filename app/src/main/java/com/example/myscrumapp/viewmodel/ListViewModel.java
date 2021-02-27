package com.example.myscrumapp.viewmodel;

import android.app.Application;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myscrumapp.model.entity.Task;
import com.example.myscrumapp.model.room.dao.TaskDao;
import com.example.myscrumapp.model.room.db.TaskDatabase;
import com.example.myscrumapp.model.network.TasksApiService;
import com.example.myscrumapp.utils.SharedPreferencesHelper;
import com.example.myscrumapp.utils.TaskRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;

@Getter
public class ListViewModel extends AndroidViewModel {

    private MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private MutableLiveData<Boolean> taskLoadError = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private TasksApiService tasksApiService = new TasksApiService();
    private CompositeDisposable disposable = new CompositeDisposable();

    private final TaskRunner taskRunner = new TaskRunner();
    private final SharedPreferencesHelper preferencesHelper = SharedPreferencesHelper.getInstance(getApplication());
    private final Long refreshTime = 5*60*100*1000*1000L;

    public ListViewModel(@NonNull Application application) {
        super(application);
    }

    public void refresh() {
        Long updateTime = preferencesHelper.getUpdateTime();
        Long currentTime = System.nanoTime();
        if(updateTime != 0 && currentTime -updateTime < refreshTime) {
            fetchFromDatabase();
        }else{
            fetchFromRemote();
        }
    }

    public void refreshBypassCache(){
        fetchFromRemote();
    }

    private void fetchFromDatabase(){
        loading.setValue(true);
        taskRunner.executeAsync(new RetrieveTasksTask(), (data) ->{
            tasksRetrieved(data);
            Toast.makeText(getApplication(),"Tasks retrieved from database",Toast.LENGTH_LONG).show();
        });
    }


    private void fetchFromRemote() {
        loading.setValue(true);
        disposable.add(
                tasksApiService.getTasks()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<Task>>() {
                            @Override
                            public void onSuccess(@io.reactivex.annotations.NonNull List<Task> tasksList) {
                                taskRunner.executeAsync(new InsertTasksTask(tasksList), (data) ->{
                                            tasksRetrieved(data);
                                            preferencesHelper.saveUpdateTime(System.nanoTime());
                                            Toast.makeText(getApplication(),"Tasks retrieved from service",Toast.LENGTH_LONG).show();
                                        });
                            }

                            @Override
                            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                taskLoadError.setValue(true);
                                loading.setValue(false);
                                e.printStackTrace();
                            }
                        })
        );

    }

    private void tasksRetrieved(List<Task> tasksList){
        tasks.setValue(tasksList);
        taskLoadError.setValue(false);
        loading.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }


    private class InsertTasksTask implements Callable<List<Task>>{

        private final List<Task>[] lists;

        private InsertTasksTask(List<Task>... lists) {
            this.lists = lists;
        }

        @Override
        public List<Task> call() throws Exception {
            List<Task> list = lists[0];
            TaskDao dao = TaskDatabase.getInstance(getApplication()).taskDao();
            dao.deleteAllTasks();

            ArrayList<Task> newList = new ArrayList<>(list);
            List<Long> result = dao.insertAll(newList.toArray(new Task[0]));

            int i=0;
            while (i<list.size()) {
                list.get(i).setId(result.get(i).intValue());
                ++i;
            }

            return list;
        }
    }

    private class RetrieveTasksTask implements Callable<List<Task>>{

        @Override
        public List<Task> call() throws Exception {
            return TaskDatabase.getInstance(getApplication()).taskDao().getAllTasks();
        }
    }

}
