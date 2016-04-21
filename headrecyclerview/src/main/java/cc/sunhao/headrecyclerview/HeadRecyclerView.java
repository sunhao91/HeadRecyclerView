package cc.sunhao.headrecyclerview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sunhao on 16/4/20.
 */
public class HeadRecyclerView extends RelativeLayout {

    private RecyclerView recyclerView;
    private int headerLayout = R.layout.item_header;
    private Context context;
    private View headViewLayout;

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    private HeadAdapter adapter;

    public HeadRecyclerView(Context context) {
        super(context);
        initView(context);
    }

    public HeadRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public HeadRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeadRecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private int curIndex = 0;

    private void initView(Context context) {
        this.context = context;
        recyclerView = new RecyclerView(context);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        recyclerView.setLayoutParams(layoutParams);
        this.addView(recyclerView);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = linearLayoutManager.findFirstVisibleItemPosition();
                if (position + 1 >= adapter.getItemCount()) return;

                String head;
                //如果当前有head
                if (adapter.containHeader(position)) {
                    curIndex = position;
                    head = adapter.getHeader(position);
                } else {
                    if (curIndex < position) {
                        head = adapter.getHeader(curIndex);
                    } else {
                        int lastIndex = adapter.headArray.indexOf(curIndex) - 1;
                        head = adapter.getHeader(adapter.headArray.get(lastIndex));
                    }
                }
                ((TextView) headViewLayout.findViewById(R.id.header)).setText(head);

                //如果下一个有header的话
                if (adapter.containHeader(position + 1)) {
                    ItemViewHolder viewHolder = (ItemViewHolder) recyclerView.findViewHolderForAdapterPosition(position + 1);
                    int distance = viewHolder.itemView.getTop();

                    int height = headViewLayout.getHeight();

                    // 小于的时候开始处理
                    if (distance <= height) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) headViewLayout.getLayoutParams();
                        params.setMargins(0, distance - height, 0, 0);
                        headViewLayout.setLayoutParams(params);
                        return;
                    }
                }
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) headViewLayout.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                headViewLayout.setLayoutParams(params);
            }
        });

        headViewLayout = LayoutInflater.from(context).inflate(headerLayout, this, false);
        this.addView(headViewLayout);
    }


    public void setHeaderView(int headerLayout) {
        this.headerLayout = headerLayout;
        if (headViewLayout != null) {
            this.removeView(headViewLayout);
        }

        headViewLayout = LayoutInflater.from(context).inflate(headerLayout, this, false);
        this.addView(headViewLayout);
        if (adapter != null) adapter.setHeaderLayout(headerLayout);
    }

    public void setAdapter(HeadAdapter adapter) {
        this.adapter = adapter;
        adapter.setHeaderLayout(headerLayout);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(mItemClickListener);
        adapter.setOnItemLongClickListener(mItemLongClickListener);
    }


    public static abstract class HeadAdapter extends RecyclerView.Adapter {
        private Map<Integer, String> headerMap = new ConcurrentHashMap<>();
        List<Integer> headArray = new ArrayList<>();

        private OnItemLongClickListener onItemLongClickListener;
        private OnItemClickListener onItemClickListener;
        private int headerLayout;

        public String getHeader(Integer position) {
            return headerMap.get(position);
        }

        public boolean containHeader(Integer position) {
            return headerMap.containsKey(position);
        }

        public void resetHeader() {
            this.headerMap.clear();
            headArray.clear();
        }

        public void addHeaderMap(Integer position, String header) {
            this.headerMap.put(position, header);
            headArray.add(position);
            Collections.sort(headArray);
        }


        void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        void setOnItemLongClickListener(final OnItemLongClickListener onItemLongClickListener) {
            this.onItemLongClickListener = onItemLongClickListener;
        }

        void setHeaderLayout(int headerLayout) {
            this.headerLayout = headerLayout;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            try {
                onBindData((ItemViewHolder) holder, position);
                TextView textView = ((ItemViewHolder) holder).findViewById(R.id.header);
                if (headerMap.containsKey(position)) {
                    textView.setVisibility(VISIBLE);
                    textView.setText(headerMap.get(position));
                } else {
                    textView.setVisibility(GONE);
                }


                ((ItemViewHolder) holder).setOnItemClickListener(onItemClickListener, position);
                ((ItemViewHolder) holder).setOnItemLongClickListener(onItemLongClickListener, position);
            } catch (Exception e) {
                //拦截掉所有因为绑定数据参数的运行时异常，不让程序崩溃
                e.printStackTrace();
            }
        }


        public abstract int getLayout(int viewType);


        /**
         * view 绑定数据
         *
         * @param position   position
         * @param viewHolder viewHolder
         */
        public abstract void onBindData(ItemViewHolder viewHolder, int position);

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout rootView = new LinearLayout(parent.getContext());
            rootView.setOrientation(LinearLayout.VERTICAL);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            rootView.setLayoutParams(layoutParams);

            rootView.addView(LayoutInflater.from(parent.getContext()).inflate(headerLayout, parent, false));
            rootView.addView(LayoutInflater.from(parent.getContext()).inflate(getLayout(viewType), parent, false));

            return new ItemViewHolder(rootView);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final SparseArray<View> views;
        private View convertView;

        public ItemViewHolder(View convertView) {
            super(convertView);
            this.views = new SparseArray<>();
            this.convertView = convertView;
        }

        public void setOnItemClickListener(final OnItemClickListener onItemClickListener, final int position) {
            if (onItemClickListener == null) {
                convertView.setOnClickListener(null);
            } else {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemClick(v, position);
                    }
                });
            }
        }

        public void setOnItemLongClickListener(final OnItemLongClickListener onItemLongClickListener, final int position) {
            if (onItemLongClickListener == null) {
                convertView.setOnLongClickListener(null);
            } else {
                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return onItemLongClickListener.onItemLongClick(v, position);
                    }

                });
            }
        }

        public <T extends View> T findViewById(int viewId) {
            View view = views.get(viewId);
            if (view == null) {
                view = convertView.findViewById(viewId);
                views.put(viewId, view);
            }
            return (T) view;
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View parentView, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View parentView, int position);
    }

    /**
     * 设置点击事件
     *
     * @param onItemClickListener onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mItemClickListener = onItemClickListener;
        if (adapter != null) adapter.setOnItemClickListener(onItemClickListener);

    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mItemLongClickListener = onItemLongClickListener;
        if (adapter != null) adapter.setOnItemLongClickListener(mItemLongClickListener);
    }
}

