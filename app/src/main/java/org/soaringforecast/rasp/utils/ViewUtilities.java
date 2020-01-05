package org.soaringforecast.rasp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.soaringforecast.rasp.R;

public class ViewUtilities {

    public  static Activity getActivity(View view) {
        Context context = view.getContext();
        return getActivity(context);
    }

    public static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    public static void displayErrorDialog(View view, String title, String message) {

        Activity activity = ViewUtilities.getActivity(view);
        if (activity != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(message).setTitle(title)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            builder.create().show();
        }

    }

    public static void addRecyclerViewDivider(Context context, int linearLayoutOrientation, RecyclerView recyclerView){
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext()
                , linearLayoutOrientation);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }
}
