package org.soaringforecast.rasp.turnpoints.seeyou;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.recycleradapter.GenericRecyclerViewAdapter;
import org.soaringforecast.rasp.databinding.SeeYouFileView;
import org.soaringforecast.rasp.turnpoints.messages.ImportSeeYouFile;
import org.soaringforecast.rasp.turnpoints.json.TurnpointFile;

import org.greenrobot.eventbus.EventBus;

import java.util.List;



public class SeeYouImportRecyclerViewAdapter extends GenericRecyclerViewAdapter<TurnpointFile, SeeYouImportViewHolder> {

    public SeeYouImportRecyclerViewAdapter(List<TurnpointFile> items) {
        super(items);
    }

    @Override
    public SeeYouImportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SeeYouFileView binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.seeyou_import_cup_file, parent, false);
        return new SeeYouImportViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SeeYouImportViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        //TODO get better way to do following
        holder.getViewDataBinding().setFileClickListener(this);
    }

    public void onFileClick(TurnpointFile turnpointFile, Integer position) {
        EventBus.getDefault().post(new ImportSeeYouFile(turnpointFile));

    }

}
