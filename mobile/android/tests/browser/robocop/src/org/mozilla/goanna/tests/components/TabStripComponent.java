package org.mozilla.goanna.tests.components;

import android.view.View;
import android.support.v7.widget.RecyclerView;

import com.robotium.solo.Condition;

import org.mozilla.goanna.R;
import org.mozilla.goanna.RobocopUtils;
import org.mozilla.goanna.tabs.TabStripItemView;
import org.mozilla.goanna.tests.UITestContext;
import org.mozilla.goanna.tests.helpers.DeviceHelper;
import org.mozilla.goanna.tests.helpers.WaitHelper;

import static org.mozilla.goanna.tests.helpers.AssertionHelper.*;

/**
 * A class representing any interactions that take place on the tablet tab strip.
 */
public class TabStripComponent extends BaseComponent {
    // Using a text id because the layout and therefore the id might be stripped from the (non-tablet) build
    private static final String TAB_STRIP_ID = "tab_strip";

    public TabStripComponent(final UITestContext testContext) {
        super(testContext);
    }

    public TabStripComponent assertTabCount(int count) {
        fAssertEquals("The tab strip tab count is " + count, count, getTabStripView().getAdapter().getItemCount());
        return this;
    }

    /**
     * Scroll the tab at {@code index} into view and click on it, where {@code index} is with
     * respect to the start of the tabs list (and not just relative to the tabs visible on screen).
     */
    public void switchToTab(int index) {
        // The tab strip is only available on tablets
        DeviceHelper.assertIsTablet();

        View tabView = waitForTabView(index);
        fAssertNotNull(String.format("Tab at index %d is not null", index), tabView);

        mSolo.clickOnView(tabView);
    }

    public void clickNewTabButton() {
        final int preNewTabCount = getTabCount();

        mSolo.clickOnView(mSolo.getView(R.id.tablet_add_tab));

        waitForNewTab(preNewTabCount);
    }

    public void waitForNewTab(final int tabCountBeforeNewTab) {
        WaitHelper.waitFor("new tab", new Condition() {
            @Override
            public boolean isSatisfied() {
                return getTabCount() == tabCountBeforeNewTab + 1;
            }
        });
    }

    /**
     * Add tabs until scrolling is required to view all tabs, and then add at least one more.
     */
    public void fillStripWithTabs() {
        if (getTabCount() > getTabStripView().getChildCount() + 1) {
            // We're already full and then some.
            return;
        }

        waitForTabView(0);
        final int firstId = getTabViewAtVisualIndex(0).getTabId();

        while (true) {
            clickNewTabButton();
            if (getTabViewAtVisualIndex(0).getTabId() != firstId) {
                break;
            }
        }

        // Add an extra to be really convincing.
        clickNewTabButton();
    }

    /**
     * @param index the *visual* index of the tab view to return (the first visible tab is index 0).
     * @return the tab view at the given visual index.
     */
    public TabStripItemView getTabViewAtVisualIndex(int index) {
        final RecyclerView tabStripView = getTabStripView();
        fAssertTrue("The tab at index " + index + " is visible", index >= 0 && index < tabStripView.getChildCount());
        return (TabStripItemView) tabStripView.getChildAt(index);
    }


    public int getTabCount() {
        return getTabStripView().getAdapter().getItemCount();
    }

    private View waitForTabView(final int index) {
        final View[] childView = { null };

        final RecyclerView tabStrip = getTabStripView();

        RobocopUtils.runOnUiThreadSync(mTestContext.getActivity(), new Runnable() {
            @Override
            public void run() {
                tabStrip.scrollToPosition(index);
                // The selection isn't updated synchronously; posting a runnable to the view's queue
                // guarantees we'll run after the layout pass.
                tabStrip.post(new Runnable() {
                    @Override
                    public void run() {
                        final RecyclerView.ViewHolder itemViewHolder = tabStrip.findViewHolderForLayoutPosition(index);
                        childView[0] = itemViewHolder == null ? null : itemViewHolder.itemView;
                    }
                });
            }
        });

        WaitHelper.waitFor("tab at " + index + " to scroll into view",
                new Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return childView[0] != null;
                    }
                });

        fAssertNotNull("Item at index " + index + " exists", childView[0]);

        return childView[0];
    }

    /**
     * You should generally use {@link #getTabStripView()} instead unless you're sure the view
     * exists; this version may return {@code null}.
     */
    private RecyclerView maybeGetTabStripView() {
        return (RecyclerView) mSolo.getView("tab_strip");
    }

    private RecyclerView getTabStripView() {
        RecyclerView tabStrip = maybeGetTabStripView();

        fAssertNotNull("Tab strip is not null", tabStrip);

        return tabStrip;
    }
}
