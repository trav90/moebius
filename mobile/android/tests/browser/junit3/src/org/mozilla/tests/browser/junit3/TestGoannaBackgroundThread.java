/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tests.browser.junit3;

import android.test.InstrumentationTestCase;
import org.mozilla.goanna.util.ThreadUtils;

public class TestGoannaBackgroundThread extends InstrumentationTestCase {

    private boolean finishedTest;
    private boolean ranFirstRunnable;

    public void testGoannaBackgroundThread() throws InterruptedException {

        final Thread testThread = Thread.currentThread();

        ThreadUtils.postToBackgroundThread(new Runnable() {
            @Override
            public void run() {
                // Must *not* be on thread that posted the Runnable.
                assertFalse(ThreadUtils.isOnThread(testThread));

                // Must be on background thread.
                assertTrue(ThreadUtils.isOnBackgroundThread());

                ranFirstRunnable = true;
            }
        });

        // Post a second Runnable to make sure it still runs on the background thread,
        // and it only runs after the first Runnable has run.
        ThreadUtils.postToBackgroundThread(new Runnable() {
            @Override
            public void run() {
                // Must still be on background thread.
                assertTrue(ThreadUtils.isOnBackgroundThread());

                // This Runnable must be run after the first Runnable had finished.
                assertTrue(ranFirstRunnable);

                synchronized (TestGoannaBackgroundThread.this) {
                    finishedTest = true;
                    TestGoannaBackgroundThread.this.notify();
                }
            }
        });

        synchronized (this) {
            while (!finishedTest) {
                wait();
            }
        }
    }
}
