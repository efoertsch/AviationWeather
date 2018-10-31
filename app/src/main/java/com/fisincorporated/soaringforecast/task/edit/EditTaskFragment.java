package com.fisincorporated.soaringforecast.task.edit;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.databinding.EditTaskView;
import com.fisincorporated.soaringforecast.messages.AddTurnpointsToTask;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.touchhelper.OnStartDragListener;
import com.fisincorporated.soaringforecast.touchhelper.SimpleItemTouchHelperCallback;

import org.greenrobot.eventbus.EventBus;

public class EditTaskFragment extends Fragment implements OnStartDragListener {

    private AppRepository appRepository;
    private long taskId;

    private TaskAndTurnpointsViewModel taskAndTurnpointsViewModel;
    private TaskTurnpointsRecyclerViewAdapter recyclerViewAdapter;
    private FloatingActionButton saveFab;
    private ItemTouchHelper itemTouchHelper;
    private EditTaskView editTaskView;


    public static EditTaskFragment newInstance(AppRepository appRepository, long taskId) {
        EditTaskFragment editTaskFragment = new EditTaskFragment();
        editTaskFragment.appRepository = appRepository;
        editTaskFragment.taskId = taskId;
        return editTaskFragment;
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        editTaskView = DataBindingUtil.inflate(inflater, R.layout.task_edit_layout, container, false);

        taskAndTurnpointsViewModel = ViewModelProviders.of(getActivity())
                .get(TaskAndTurnpointsViewModel.class)
                .setAppRepository(appRepository)
                .setTaskId(taskId);

        editTaskView.setTaskAndTurnpointsViewModel(taskAndTurnpointsViewModel);

        RecyclerView recyclerView = editTaskView.editTaskRecyclerView;
        recyclerViewAdapter = new TaskTurnpointsRecyclerViewAdapter(taskAndTurnpointsViewModel);

        //TODO DRY
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext()
                , linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(recyclerViewAdapter);

        taskAndTurnpointsViewModel.getTaskTurnpoints().observe(this, taskTurnpoints -> {
            recyclerViewAdapter.setItems(taskTurnpoints);
            recyclerViewAdapter.notifyDataSetChanged();
        });

        // Could not get taskDistance in xml to update with new distance automagically on task distance change
        // so set text this way
        taskAndTurnpointsViewModel.getTaskDistance().observe(this, taskDistance ->{
            editTaskView.editTaskDistance.setText(getString(R.string.distance_km,taskDistance));
        });


        //TODO DRY
        //DragListener
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(recyclerViewAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        editTaskView.editTaskAddTurnpoints.setOnClickListener(v -> goToAddTaskTurnpoints());

        saveFab = editTaskView.editTaskSaveTask;
        saveFab.setOnClickListener(v -> taskAndTurnpointsViewModel.saveTask());


        taskAndTurnpointsViewModel.getNeedToSaveUpdates().observe(this, needToSaveUpdates -> {
            saveFab.setVisibility(needToSaveUpdates ? View.VISIBLE : View.GONE);
        });

        return editTaskView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.edit_task);
        // if just added all turnpoints via search, the task distance will not be updated by observer in createView
        // ( as this fragment paused while adding turnpoints, so make sure ui update occurs.
        editTaskView.editTaskDistance.setText(getString(R.string.distance_km,taskAndTurnpointsViewModel.getTaskDistance().getValue()));
    }

    private void goToAddTaskTurnpoints() {
        EventBus.getDefault().post(new AddTurnpointsToTask());
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}