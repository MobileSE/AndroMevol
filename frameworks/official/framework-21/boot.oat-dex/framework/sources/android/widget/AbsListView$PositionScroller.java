package android.widget;

import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;

class AbsListView$PositionScroller extends AbsListView.AbsPositionScroller implements Runnable {
    private static final int MOVE_DOWN_BOUND = 3;
    private static final int MOVE_DOWN_POS = 1;
    private static final int MOVE_OFFSET = 5;
    private static final int MOVE_UP_BOUND = 4;
    private static final int MOVE_UP_POS = 2;
    private static final int SCROLL_DURATION = 200;
    private int mBoundPos;
    private final int mExtraScroll;
    private int mLastSeenPos;
    private int mMode;
    private int mOffsetFromTop;
    private int mScrollDuration;
    private int mTargetPos;
    final /* synthetic */ AbsListView this$0;

    AbsListView$PositionScroller(AbsListView absListView) {
        this.this$0 = absListView;
        this.mExtraScroll = ViewConfiguration.get(AbsListView.access$4200(absListView)).getScaledFadingEdgeLength();
    }

    public void start(int position) {
        int viewTravelCount;
        stop();
        if (this.this$0.mDataChanged) {
            this.this$0.mPositionScrollAfterLayout = new 1(this, position);
            return;
        }
        int childCount = this.this$0.getChildCount();
        if (childCount != 0) {
            int firstPos = this.this$0.mFirstPosition;
            int lastPos = (firstPos + childCount) - 1;
            int clampedPosition = Math.max(0, Math.min(this.this$0.getCount() - 1, position));
            if (clampedPosition < firstPos) {
                viewTravelCount = (firstPos - clampedPosition) + 1;
                this.mMode = 2;
            } else if (clampedPosition > lastPos) {
                viewTravelCount = (clampedPosition - lastPos) + 1;
                this.mMode = 1;
            } else {
                scrollToVisible(clampedPosition, -1, 200);
                return;
            }
            if (viewTravelCount > 0) {
                this.mScrollDuration = 200 / viewTravelCount;
            } else {
                this.mScrollDuration = 200;
            }
            this.mTargetPos = clampedPosition;
            this.mBoundPos = -1;
            this.mLastSeenPos = -1;
            this.this$0.postOnAnimation(this);
        }
    }

    public void start(int position, int boundPosition) {
        int viewTravelCount;
        stop();
        if (boundPosition == -1) {
            start(position);
        } else if (this.this$0.mDataChanged) {
            this.this$0.mPositionScrollAfterLayout = new 2(this, position, boundPosition);
        } else {
            int childCount = this.this$0.getChildCount();
            if (childCount != 0) {
                int firstPos = this.this$0.mFirstPosition;
                int lastPos = (firstPos + childCount) - 1;
                int clampedPosition = Math.max(0, Math.min(this.this$0.getCount() - 1, position));
                if (clampedPosition < firstPos) {
                    int boundPosFromLast = lastPos - boundPosition;
                    if (boundPosFromLast >= 1) {
                        int posTravel = (firstPos - clampedPosition) + 1;
                        int boundTravel = boundPosFromLast - 1;
                        if (boundTravel < posTravel) {
                            viewTravelCount = boundTravel;
                            this.mMode = 4;
                        } else {
                            viewTravelCount = posTravel;
                            this.mMode = 2;
                        }
                    } else {
                        return;
                    }
                } else if (clampedPosition > lastPos) {
                    int boundPosFromFirst = boundPosition - firstPos;
                    if (boundPosFromFirst >= 1) {
                        int posTravel2 = (clampedPosition - lastPos) + 1;
                        int boundTravel2 = boundPosFromFirst - 1;
                        if (boundTravel2 < posTravel2) {
                            viewTravelCount = boundTravel2;
                            this.mMode = 3;
                        } else {
                            viewTravelCount = posTravel2;
                            this.mMode = 1;
                        }
                    } else {
                        return;
                    }
                } else {
                    scrollToVisible(clampedPosition, boundPosition, 200);
                    return;
                }
                if (viewTravelCount > 0) {
                    this.mScrollDuration = 200 / viewTravelCount;
                } else {
                    this.mScrollDuration = 200;
                }
                this.mTargetPos = clampedPosition;
                this.mBoundPos = boundPosition;
                this.mLastSeenPos = -1;
                this.this$0.postOnAnimation(this);
            }
        }
    }

    public void startWithOffset(int position, int offset) {
        startWithOffset(position, offset, 200);
    }

    public void startWithOffset(final int position, final int offset, final int duration) {
        int viewTravelCount;
        stop();
        if (this.this$0.mDataChanged) {
            this.this$0.mPositionScrollAfterLayout = new Runnable() {
                /* class android.widget.AbsListView$PositionScroller.AnonymousClass3 */

                public void run() {
                    AbsListView$PositionScroller.this.startWithOffset(position, offset, duration);
                }
            };
            return;
        }
        int childCount = this.this$0.getChildCount();
        if (childCount != 0) {
            int offset2 = offset + this.this$0.getPaddingTop();
            this.mTargetPos = Math.max(0, Math.min(this.this$0.getCount() - 1, position));
            this.mOffsetFromTop = offset2;
            this.mBoundPos = -1;
            this.mLastSeenPos = -1;
            this.mMode = 5;
            int firstPos = this.this$0.mFirstPosition;
            int lastPos = (firstPos + childCount) - 1;
            if (this.mTargetPos < firstPos) {
                viewTravelCount = firstPos - this.mTargetPos;
            } else if (this.mTargetPos > lastPos) {
                viewTravelCount = this.mTargetPos - lastPos;
            } else {
                this.this$0.smoothScrollBy(this.this$0.getChildAt(this.mTargetPos - firstPos).getTop() - offset2, duration, true);
                return;
            }
            float screenTravelCount = ((float) viewTravelCount) / ((float) childCount);
            if (screenTravelCount >= 1.0f) {
                duration = (int) (((float) duration) / screenTravelCount);
            }
            this.mScrollDuration = duration;
            this.mLastSeenPos = -1;
            this.this$0.postOnAnimation(this);
        }
    }

    private void scrollToVisible(int targetPos, int boundPos, int duration) {
        int firstPos = this.this$0.mFirstPosition;
        int lastPos = (firstPos + this.this$0.getChildCount()) - 1;
        int paddedTop = this.this$0.mListPadding.top;
        int paddedBottom = this.this$0.getHeight() - this.this$0.mListPadding.bottom;
        if (targetPos < firstPos || targetPos > lastPos) {
            Log.w("AbsListView", "scrollToVisible called with targetPos " + targetPos + " not visible [" + firstPos + ", " + lastPos + "]");
        }
        if (boundPos < firstPos || boundPos > lastPos) {
            boundPos = -1;
        }
        View targetChild = this.this$0.getChildAt(targetPos - firstPos);
        int targetTop = targetChild.getTop();
        int targetBottom = targetChild.getBottom();
        int scrollBy = 0;
        if (targetBottom > paddedBottom) {
            scrollBy = targetBottom - paddedBottom;
        }
        if (targetTop < paddedTop) {
            scrollBy = targetTop - paddedTop;
        }
        if (scrollBy != 0) {
            if (boundPos >= 0) {
                View boundChild = this.this$0.getChildAt(boundPos - firstPos);
                int boundTop = boundChild.getTop();
                int boundBottom = boundChild.getBottom();
                int absScroll = Math.abs(scrollBy);
                if (scrollBy < 0 && boundBottom + absScroll > paddedBottom) {
                    scrollBy = Math.max(0, boundBottom - paddedBottom);
                } else if (scrollBy > 0 && boundTop - absScroll < paddedTop) {
                    scrollBy = Math.min(0, boundTop - paddedTop);
                }
            }
            this.this$0.smoothScrollBy(scrollBy, duration);
        }
    }

    public void stop() {
        this.this$0.removeCallbacks(this);
    }

    public void run() {
        int extraScroll;
        int extraScroll2;
        int listHeight = this.this$0.getHeight();
        int firstPos = this.this$0.mFirstPosition;
        switch (this.mMode) {
            case 1:
                int lastViewIndex = this.this$0.getChildCount() - 1;
                int lastPos = firstPos + lastViewIndex;
                if (lastViewIndex < 0) {
                    return;
                }
                if (lastPos == this.mLastSeenPos) {
                    this.this$0.postOnAnimation(this);
                    return;
                }
                View lastView = this.this$0.getChildAt(lastViewIndex);
                int lastViewHeight = lastView.getHeight();
                int lastViewPixelsShowing = listHeight - lastView.getTop();
                if (lastPos < this.this$0.mItemCount - 1) {
                    extraScroll2 = Math.max(this.this$0.mListPadding.bottom, this.mExtraScroll);
                } else {
                    extraScroll2 = this.this$0.mListPadding.bottom;
                }
                this.this$0.smoothScrollBy((lastViewHeight - lastViewPixelsShowing) + extraScroll2, this.mScrollDuration, true);
                this.mLastSeenPos = lastPos;
                if (lastPos < this.mTargetPos) {
                    this.this$0.postOnAnimation(this);
                    return;
                }
                return;
            case 2:
                if (firstPos == this.mLastSeenPos) {
                    this.this$0.postOnAnimation(this);
                    return;
                }
                View firstView = this.this$0.getChildAt(0);
                if (firstView != null) {
                    int firstViewTop = firstView.getTop();
                    if (firstPos > 0) {
                        extraScroll = Math.max(this.mExtraScroll, this.this$0.mListPadding.top);
                    } else {
                        extraScroll = this.this$0.mListPadding.top;
                    }
                    this.this$0.smoothScrollBy(firstViewTop - extraScroll, this.mScrollDuration, true);
                    this.mLastSeenPos = firstPos;
                    if (firstPos > this.mTargetPos) {
                        this.this$0.postOnAnimation(this);
                        return;
                    }
                    return;
                }
                return;
            case 3:
                int childCount = this.this$0.getChildCount();
                if (firstPos != this.mBoundPos && childCount > 1 && firstPos + childCount < this.this$0.mItemCount) {
                    int nextPos = firstPos + 1;
                    if (nextPos == this.mLastSeenPos) {
                        this.this$0.postOnAnimation(this);
                        return;
                    }
                    View nextView = this.this$0.getChildAt(1);
                    int nextViewHeight = nextView.getHeight();
                    int nextViewTop = nextView.getTop();
                    int extraScroll3 = Math.max(this.this$0.mListPadding.bottom, this.mExtraScroll);
                    if (nextPos < this.mBoundPos) {
                        this.this$0.smoothScrollBy(Math.max(0, (nextViewHeight + nextViewTop) - extraScroll3), this.mScrollDuration, true);
                        this.mLastSeenPos = nextPos;
                        this.this$0.postOnAnimation(this);
                        return;
                    } else if (nextViewTop > extraScroll3) {
                        this.this$0.smoothScrollBy(nextViewTop - extraScroll3, this.mScrollDuration, true);
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            case 4:
                int lastViewIndex2 = this.this$0.getChildCount() - 2;
                if (lastViewIndex2 >= 0) {
                    int lastPos2 = firstPos + lastViewIndex2;
                    if (lastPos2 == this.mLastSeenPos) {
                        this.this$0.postOnAnimation(this);
                        return;
                    }
                    View lastView2 = this.this$0.getChildAt(lastViewIndex2);
                    int lastViewHeight2 = lastView2.getHeight();
                    int lastViewTop = lastView2.getTop();
                    int lastViewPixelsShowing2 = listHeight - lastViewTop;
                    int extraScroll4 = Math.max(this.this$0.mListPadding.top, this.mExtraScroll);
                    this.mLastSeenPos = lastPos2;
                    if (lastPos2 > this.mBoundPos) {
                        this.this$0.smoothScrollBy(-(lastViewPixelsShowing2 - extraScroll4), this.mScrollDuration, true);
                        this.this$0.postOnAnimation(this);
                        return;
                    }
                    int bottom = listHeight - extraScroll4;
                    int lastViewBottom = lastViewTop + lastViewHeight2;
                    if (bottom > lastViewBottom) {
                        this.this$0.smoothScrollBy(-(bottom - lastViewBottom), this.mScrollDuration, true);
                        return;
                    }
                    return;
                }
                return;
            case 5:
                if (this.mLastSeenPos == firstPos) {
                    this.this$0.postOnAnimation(this);
                    return;
                }
                this.mLastSeenPos = firstPos;
                int childCount2 = this.this$0.getChildCount();
                int position = this.mTargetPos;
                int lastPos3 = (firstPos + childCount2) - 1;
                int viewTravelCount = 0;
                if (position < firstPos) {
                    viewTravelCount = (firstPos - position) + 1;
                } else if (position > lastPos3) {
                    viewTravelCount = position - lastPos3;
                }
                float modifier = Math.min(Math.abs(((float) viewTravelCount) / ((float) childCount2)), 1.0f);
                if (position < firstPos) {
                    this.this$0.smoothScrollBy((int) (((float) (-this.this$0.getHeight())) * modifier), (int) (((float) this.mScrollDuration) * modifier), true);
                    this.this$0.postOnAnimation(this);
                    return;
                } else if (position > lastPos3) {
                    this.this$0.smoothScrollBy((int) (((float) this.this$0.getHeight()) * modifier), (int) (((float) this.mScrollDuration) * modifier), true);
                    this.this$0.postOnAnimation(this);
                    return;
                } else {
                    int distance = this.this$0.getChildAt(position - firstPos).getTop() - this.mOffsetFromTop;
                    this.this$0.smoothScrollBy(distance, (int) (((float) this.mScrollDuration) * (((float) Math.abs(distance)) / ((float) this.this$0.getHeight()))), true);
                    return;
                }
            default:
                return;
        }
    }
}
