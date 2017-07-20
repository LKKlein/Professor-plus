package cn.edu.swufe.fife.professor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private List<String> professors;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private LinearLayout no_face;

    private OnFragmentInteractionListener mListener;

    public MainActivityFragment() {
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(boolean up);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inner_view = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) inner_view.findViewById(R.id.main_recycler);
        no_face = (LinearLayout) inner_view.findViewById(R.id.no_face_linear);
        int spanCount = 2;

        RecyclerView.LayoutManager mLayoutManager = new StaggeredGridLayoutManager(
                spanCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        SharedPreferences ss = getActivity().getSharedPreferences("recent", Context.MODE_PRIVATE);
        Set<String> recent = ss.getStringSet("recent faces", new LinkedHashSet<String>());
        professors = new ArrayList<>();
        professors.addAll(recent);
        if (professors.size() == 0){
            no_face.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            no_face.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        mAdapter = new MyAdapter(professors);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, 24, false));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 100) {
                    // Scroll Down
                    mListener.onFragmentInteraction(true);
                }
                else if (dy < -25) {
                    // Scroll Up
                    mListener.onFragmentInteraction(false);
                }
            }
        });
        return inner_view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences ss = getActivity().getSharedPreferences("recent", Context.MODE_PRIVATE);
        Set<String> recent = ss.getStringSet("recent faces", new LinkedHashSet<String>());
        professors = new ArrayList<>();
        professors.addAll(recent);
        if (professors.size() == 0){
            no_face.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            no_face.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter.setData(professors);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{

        private List<String> data = null;
        MyAdapter(List<String> data) {
            this.data = data;
        }

        private void setData(List<String> data){
            this.data = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_item, null);
            return new MyViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.position = position;
            String s = data.get(position);
            Professor professor = JSON.parseObject(s, Professor.class);
            holder.card_name.setText(professor.getName());
            holder.card_university.setText(professor.getUniversity());
            Glide.with(getActivity())
                    .load(new File(professor.getUrl(), professor.getPath_name()))
                    .placeholder(R.drawable.pic_bg)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transform(new CornersTransform(getActivity(), 15))
                    .crossFade()
                    .into(holder.card_img);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView card_img;
        TextView card_name;
        private TextView card_university;

        private int position;

        MyViewHolder(View itemView) {
            super(itemView);

            card_img = (ImageView) itemView.findViewById(R.id.card_img);
            card_name = (TextView) itemView.findViewById(R.id.card_name);
            card_university = (TextView) itemView.findViewById(R.id.card_university);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String professor = professors.get(position);
                    String url = JSON.parseObject(professor, Professor.class).getWeb_page();
                    if (url.startsWith("http")){
                        Intent i = new Intent(getActivity(), WebViewActivity.class);
                        i.putExtra("url", url);
                        i.putExtra("name", JSON.parseObject(professor, Professor.class).getName());
                        i.putExtra("from", 0);
                        startActivity(i);
                    } else if(url.equals("")){
                        Toast.makeText(getActivity(), "TA没有自我介绍...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), url, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
