package com.dexafree.materialList.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;

import com.dexafree.materialList.controller.MaterialListViewAdapter;
import com.dexafree.materialList.controller.OnDismissCallback;
import com.dexafree.materialList.controller.SwipeDismissListener;
import com.dexafree.materialList.events.BusProvider;
import com.dexafree.materialList.events.DataSetChangedEvent;
import com.dexafree.materialList.events.DismissEvent;
import com.dexafree.materialList.model.Card;
import com.etsy.android.grid.StaggeredGridView;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.*;
import com.squareup.otto.Subscribe;

import java.util.Collection;


public class MaterialStaggeredGridView extends StaggeredGridView implements IMaterialView {
    private MaterialListViewAdapter mAdapter;
    private OnDismissCallback mDismissCallback;
    private SwipeDismissListener mDismissListener;

    public MaterialStaggeredGridView(Context context) {
        super(context);
        init();
    }

    public MaterialStaggeredGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MaterialStaggeredGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mDismissListener =
                new SwipeDismissListener(
                        this,
                        new SwipeDismissListener.OnDismissCallback() {
                            @Override
                            public void onDismiss(IMaterialView listView, final Card[] reverseSortedCards) {
                                for (Card dismissedCard : reverseSortedCards) {
                                    int position = getAdapter().getPosition(dismissedCard);
                                    //Log.d(getClass().getSimpleName(), dismissedCard.getmTitle() +
                                    // " [Position="+position+"]");

                                    if (mDismissCallback != null) {
                                        mDismissCallback.onDismiss(dismissedCard, position);
                                    }

                                    getAdapter().remove(dismissedCard);
                                }
                                getAdapter().notifyDataSetChanged();
                            }
                        });

        setOnTouchListener(mDismissListener);
        setOnScrollListener(mDismissListener.makeScrollListener());

        mAdapter = new MaterialListViewAdapter(getContext());
        setAdapter(mAdapter);
    }

    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void remove(Card card) {
        if (card.isDismissible()) {
            BusProvider.dismiss(card);
        }
    }

    public void add(Card card) {
        getAdapter().add(card);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void addAll(Card... cards) {
        getAdapter().addAll(cards);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void addAll(Collection<Card> cards) {
        getAdapter().addAll(cards);
    }

    public int getPosition(Card card) {
        return getAdapter().getPosition(card);
    }

    public Card getCard(int position) {
        return getAdapter().getItem(position);
    }

    public void notifyDataSetChanged() {
        getAdapter().notifyDataSetChanged();
    }

    public void setCardAnimation(CardAnimation type) {
        BaseAdapter baseAdapter = mAdapter;

        switch (type) {
            case ALPHA_IN: {
                AnimationAdapter animationAdapter = new AlphaInAnimationAdapter(baseAdapter);
                animationAdapter.setAbsListView(this);
                baseAdapter = animationAdapter;
            }
            break;
            case SCALE_IN: {
                AnimationAdapter animationAdapter = new ScaleInAnimationAdapter(baseAdapter);
                animationAdapter.setAbsListView(this);
                baseAdapter = animationAdapter;
            }
            break;
            case SWING_BOTTOM_IN: {
                AnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(baseAdapter);
                animationAdapter.setAbsListView(this);
                baseAdapter = animationAdapter;
            }
            break;
            case SWING_LEFT_IN: {
                AnimationAdapter animationAdapter = new SwingLeftInAnimationAdapter(baseAdapter);
                animationAdapter.setAbsListView(this);
                baseAdapter = animationAdapter;
            }
            break;
            case SWING_RIGHT_IN: {
                AnimationAdapter animationAdapter = new SwingRightInAnimationAdapter(baseAdapter);
                animationAdapter.setAbsListView(this);
                baseAdapter = animationAdapter;
            }
            break;
        }

        setAdapter(baseAdapter);
    }

    public MaterialListViewAdapter getAdapter() {
        return mAdapter;
    }

    public void setOnDismissCallback(OnDismissCallback callback) {
        mDismissCallback = callback;
    }

    @Subscribe
    public void onNotifyDataSetChanged(DataSetChangedEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onCardDismiss(DismissEvent event) {
        Card dismissedCard = event.getDismissedCard();
        View dismissedCardView = null;
        for (int index = 0; index < getCount(); index++) {
            View view = getChildAt(index);
            if (view.getTag() != null && view.getTag().equals(dismissedCard)) {
                dismissedCardView = view;
                break;
            }
        }
        if (dismissedCardView != null) {
            mDismissListener.dismissCard(dismissedCardView, dismissedCard);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BusProvider.register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BusProvider.unregister(this);
    }
}
