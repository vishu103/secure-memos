package in.edureal.securememos;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    private List<ListItem> listItems;
    private Context context;

    public MyAdapter(List<ListItem> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_news_horizontal, viewGroup,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        final ListItem listItem = listItems.get(i);

        viewHolder.titleTV.setText(listItem.getTitle());
        viewHolder.dateTimeTV.setText(listItem.getDateTime());
        viewHolder.UIDTV.setText("UID: "+listItem.getUid());
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (v.getContext(), MemoEditor.class);
                intent.putExtra("uid",listItem.getUid());
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView titleTV;
        public TextView dateTimeTV;
        public TextView UIDTV;
        public LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTV= (TextView) itemView.findViewById(R.id.title);
            dateTimeTV= (TextView) itemView.findViewById(R.id.date);
            UIDTV= (TextView) itemView.findViewById(R.id.uid);
            linearLayout= (LinearLayout) itemView.findViewById(R.id.lyt_parent);

        }
    }

}
