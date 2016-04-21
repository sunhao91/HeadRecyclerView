package cc.sunhao.headrecyclerviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import cc.sunhao.headrecyclerview.HeadRecyclerView;


public class MainActivity extends AppCompatActivity implements HeadRecyclerView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HeadRecyclerView headerView = (HeadRecyclerView) findViewById(R.id.recyclerView);

        HeadRecyclerView.HeadAdapter adapter = new HeadRecyclerView.HeadAdapter() {
            @Override
            public int getItemCount() {
                return 30;
            }

            @Override
            public int getLayout(int viewType) {
                return R.layout.item_demo;
            }

            @Override
            public void onBindData(HeadRecyclerView.ItemViewHolder viewHolder, int position) {
                TextView textView = viewHolder.findViewById(R.id.position_name);
                textView.setText(String.valueOf(position));
            }
        };

        adapter.addHeaderMap(0, "测试0");
        adapter.addHeaderMap(3, "测试3");
        adapter.addHeaderMap(5, "测试5");
        adapter.addHeaderMap(10, "测试10");
        headerView.setAdapter(adapter);
        headerView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(View parentView, int position) {
        Log.d("tag", "position:" + position);
    }
}
