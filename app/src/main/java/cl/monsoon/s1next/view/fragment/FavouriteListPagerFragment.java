package cl.monsoon.s1next.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import cl.monsoon.s1next.Api;
import cl.monsoon.s1next.R;
import cl.monsoon.s1next.model.Favourite;
import cl.monsoon.s1next.model.Thread;
import cl.monsoon.s1next.model.list.Favourites;
import cl.monsoon.s1next.model.mapper.FavouritesWrapper;
import cl.monsoon.s1next.util.MathUtil;
import cl.monsoon.s1next.util.ToastUtil;
import cl.monsoon.s1next.view.activity.PostListActivity;
import cl.monsoon.s1next.view.adapter.FavouriteListRecyclerAdapter;
import cl.monsoon.s1next.widget.AsyncResult;
import cl.monsoon.s1next.widget.HttpGetLoader;
import cl.monsoon.s1next.widget.RecyclerViewHelper;

/**
 * A Fragment representing one of the pages of favourites.
 * <p>
 * All activities containing this Fragment must implement {@link PagerCallback}.
 * <p>
 * Similar to {@link cl.monsoon.s1next.view.fragment.ThreadListPagerFragment}.
 */
public final class FavouriteListPagerFragment extends BaseFragment<FavouritesWrapper> {

    private static final String ARG_PAGE_NUM = "page_num";

    private int mPageNum;

    private FavouriteListRecyclerAdapter mRecyclerAdapter;

    private PagerCallback mPagerCallback;

    public static FavouriteListPagerFragment newInstance(int pageNum) {
        FavouriteListPagerFragment fragment = new FavouriteListPagerFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE_NUM, pageNum);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPageNum = getArguments().getInt(ARG_PAGE_NUM);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerAdapter = new FavouriteListRecyclerAdapter();
        recyclerView.setAdapter(mRecyclerAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerViewHelper(getActivity(), recyclerView,
                new RecyclerViewHelper.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getActivity(), PostListActivity.class);

                        Favourite favourite = mRecyclerAdapter.getItem(position);
                        cl.monsoon.s1next.model.Thread thread = new Thread();
                        thread.setId(favourite.getId());
                        thread.setTitle(favourite.getTitle());
                        intent.putExtra(PostListActivity.ARG_THREAD, thread);

                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }
                }
        ));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mPagerCallback = (PagerCallback) getFragmentManager().findFragmentByTag(
                FavouriteListFragment.TAG);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mPagerCallback = null;
    }

    @Override
    public Loader<AsyncResult<FavouritesWrapper>> onCreateLoader(int id, Bundle args) {
        super.onCreateLoader(id, args);

        return new HttpGetLoader<>(getActivity(), Api.getFavouritesUrl(mPageNum),
                FavouritesWrapper.class);
    }

    @Override
    public void onLoadFinished(Loader<AsyncResult<FavouritesWrapper>> loader, AsyncResult<FavouritesWrapper> asyncResult) {
        super.onLoadFinished(loader, asyncResult);

        if (asyncResult.exception != null) {
            if (getUserVisibleHint()) {
                ToastUtil.showByResId(asyncResult.getExceptionStringRes(), Toast.LENGTH_SHORT);
            }
        } else {
            Favourites favourites = asyncResult.data.getFavourites();

            // if user has logged out
            if (favourites.getFavouriteList() == null) {
                String message = asyncResult.data.getResult().getMessage();
                if (!TextUtils.isEmpty(message)) {
                    ToastUtil.showByText(message, Toast.LENGTH_SHORT);
                }
            } else {
                mRecyclerAdapter.setDataSet(favourites.getFavouriteList());
                mRecyclerAdapter.notifyDataSetChanged();

                new Handler().post(() ->
                        mPagerCallback.setTotalPage(MathUtil.divide(favourites.getTotal(),
                                favourites.getFavouritesPerPage())));
            }
        }
    }

    /**
     * A callback interface that all activities containing this Fragment must implement.
     */
    public interface PagerCallback {

        /**
         * A callback to set actual total pages
         * which used for {@link android.support.v4.view.PagerAdapter}。
         */
        void setTotalPage(int totalPage);
    }
}