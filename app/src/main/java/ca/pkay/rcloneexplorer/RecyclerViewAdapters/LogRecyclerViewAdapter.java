package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import ca.pkay.rcloneexplorer.R;

public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder>{

    private JSONObject[] entries;

    public LogRecyclerViewAdapter(JSONObject[] entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_log_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final JSONObject selectedTrigger = entries[position];
        try {
            holder.logdetails.setText(selectedTrigger.get("content").toString());
            holder.logdate.setText(selectedTrigger.get("timestamp").toString());
            long dv = Long.parseLong(selectedTrigger.get("timestamp").toString());// its need to be in milisecond
            Date df = new java.util.Date(dv);
            String vv = new SimpleDateFormat("MM dd, yyyy HH:mm").format(df);
            holder.logtitle.setText(vv);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setList(JSONObject[] entries) {
        this.entries=entries;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (entries == null) {
            return 0;
        }
        return entries.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View view;

        final TextView logtitle;
        final TextView logdate;
        final TextView logdetails;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;

            this.logtitle = view.findViewById(R.id.logtitle);
            this.logdate = view.findViewById(R.id.logdate);
            this.logdetails = view.findViewById(R.id.logDetails);
        }
    }
}
