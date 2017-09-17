package cn.edu.swufe.fife.professor.fragment;

import android.app.Activity;
import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import cn.edu.swufe.fife.professor.BaseApplication;
import cn.edu.swufe.fife.professor.R;
import cn.edu.swufe.fife.professor.Utils.Constant;
import cn.edu.swufe.fife.professor.activity.WebViewActivity;
import cn.edu.swufe.fife.professor.bean.DaoSession;
import cn.edu.swufe.fife.professor.bean.Professors;
import cn.edu.swufe.fife.professor.bean.ProfessorsDao;
import cn.edu.swufe.fife.professor.customView.GlideRoundTransform;

public class MainActivityFragment extends Fragment {
    private List<Professors> professors;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private LinearLayout no_face;

    private ProfessorsDao professorsDao = null;

    private OnFragmentInteractionListener mListener;

    public MainActivityFragment() {
    }

    public static Fragment newInstance(String arg) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", arg);
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(boolean up);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inner_view = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) inner_view.findViewById(R.id.main_recycler);
        no_face = (LinearLayout) inner_view.findViewById(R.id.no_face_linear);
        final int spanCount = 2;

        RecyclerView.LayoutManager mLayoutManager = new StaggeredGridLayoutManager(
                spanCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        DaoSession daoSession = ((BaseApplication) getActivity().getApplication()).getDaoSession();
        professorsDao = daoSession.getProfessorsDao();
        professors = professorsDao.queryBuilder()
                .where(ProfessorsDao.Properties.Uid.eq(getArguments().getString("uid")))
                .orderDesc(ProfessorsDao.Properties.Id)
                .build()
                .list();

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
        professors = professorsDao.queryBuilder()
                .where(ProfessorsDao.Properties.Uid.eq(getArguments().getString("uid")))
                .orderDesc(ProfessorsDao.Properties.Id)
                .build()
                .list();
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

        private List<Professors> data = null;

        MyAdapter(List<Professors> data) {
            this.data = data;
        }

        private void setData(List<Professors> data) {
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
            Professors s = data.get(position);
            holder.card_name.setText(s.getName());
            holder.card_university.setText(s.getUniversity());
            Glide.with(getActivity())
                    .load(Constant.professor_domain + s.getFace_token())
                    .placeholder(R.drawable.pictures_loading)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new GlideRoundTransform(getActivity(), 15))
                    .dontAnimate()
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
                    Professors professor = professors.get(position);
                    if (professor.getWeb_url().startsWith("http")) {
                        Intent i = new Intent(getActivity(), WebViewActivity.class);
                        i.putExtra("url", professor.getWeb_url());
                        i.putExtra("name", professor.getName());
                        startActivity(i);
                    } else if (professor.getWeb_url().equals("")) {
                        Toast.makeText(getActivity(), "TA没有自我介绍...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), professor.getWeb_url(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

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
