package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Trigger;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.TriggerActivity;

public class TriggerRecyclerViewAdapter extends RecyclerView.Adapter<TriggerRecyclerViewAdapter.ViewHolder>{

    private List<Trigger> triggers;
    private View view;
    private Context context;

    public TriggerRecyclerViewAdapter(List<Trigger> triggers, Context context) {
        this.triggers = triggers;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_trigger_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Trigger selectedTrigger = triggers.get(position);
        String remoteName = selectedTrigger.getTitle();

        holder.triggerName.setText(remoteName);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectedTrigger.getTime()/60);
        calendar.set(Calendar.MINUTE, selectedTrigger.getTime()%60);

        DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(context);
        holder.time.setText(dateFormat.format(new Date(calendar.getTimeInMillis())));


        setTextViewValue(holder.mon, selectedTrigger.isEnabledAtDay(0));
        setTextViewValue(holder.tue, selectedTrigger.isEnabledAtDay(1));
        setTextViewValue(holder.wed, selectedTrigger.isEnabledAtDay(2));
        setTextViewValue(holder.thur, selectedTrigger.isEnabledAtDay(3));
        setTextViewValue(holder.fri, selectedTrigger.isEnabledAtDay(4));
        setTextViewValue(holder.sat, selectedTrigger.isEnabledAtDay(5));
        setTextViewValue(holder.sun, selectedTrigger.isEnabledAtDay(6));

        Drawable d =ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_baseline_check_circle_outline_24, null);
        if(!selectedTrigger.isEnabled()){
            d =ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_baseline_block_24, null);
        }
        holder.triggerIcon.setImageDrawable(d);

        holder.trigger_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileMenu(v, selectedTrigger);
            }
        });

    }

    private void setTextViewValue(TextView view, boolean disabled){
        if(!disabled){
            view.setPaintFlags(view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    public void addTrigger(Trigger data) {
        triggers.add(data);
        notifyDataSetChanged();
    }

    public void setList(ArrayList<Trigger> data) {
        triggers=data;
        notifyDataSetChanged();
    }

    private void editTrigger(Trigger trigger){
        Intent intent = new Intent(context, TriggerActivity.class);
        intent.putExtra(TriggerActivity.ID_EXTRA, trigger.getId());
        context.startActivity(intent);
    }


    public void removeItem(Trigger trigger) {
        int index = triggers.indexOf(trigger);
        if (index >= 0) {
            triggers.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public int getItemCount() {
        if (triggers == null) {
            return 0;
        }
        return triggers.size();
    }

    private void showFileMenu(View view, final Trigger trigger) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.trigger_item_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit_task:
                        editTrigger(trigger);
                        break;
                    case R.id.action_delete_task:
                        new DatabaseHandler(context).deleteTrigger(trigger.getId());
                        notifyDataSetChanged();
                        removeItem(trigger);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView triggerIcon;
        final ImageButton trigger_options;
        final TextView triggerName;
        final TextView time;


        final TextView mon;
        final TextView tue;
        final TextView wed;
        final TextView thur;
        final TextView fri;
        final TextView sat;
        final TextView sun;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.triggerIcon = view.findViewById(R.id.triggerIcon);
            this.triggerName = view.findViewById(R.id.triggerName);
            this.trigger_options = view.findViewById(R.id.trigger_options);
            this.time = view.findViewById(R.id.time_starttime);

            this.mon = view.findViewById(R.id.trigger_enabled_mon);
            this.tue = view.findViewById(R.id.trigger_enabled_tue);
            this.wed = view.findViewById(R.id.trigger_enabled_wed);
            this.thur = view.findViewById(R.id.trigger_enabled_thu);
            this.fri = view.findViewById(R.id.trigger_enabled_fri);
            this.sat = view.findViewById(R.id.trigger_enabled_sat);
            this.sun = view.findViewById(R.id.trigger_enabled_sun);
        }
    }
}
