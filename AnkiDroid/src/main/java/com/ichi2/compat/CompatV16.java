/***************************************************************************************
 * Copyright (c) 2016 Timothy Rae <perceptualchaos2@gmail.com>                          *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.ichi2.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.TypedArray;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.os.StatFs;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.ichi2.anki.AbstractFlashcardViewer;
import com.ichi2.anki.AnkiActivity;
import com.ichi2.anki.AnkiDroidApp;
import com.ichi2.anki.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import timber.log.Timber;

/** Implementation of {@link Compat} for SDK level 16 */
@TargetApi(16)
public class CompatV16 implements Compat {
    // CookieSyncManager needs to be initialized before use.
    // Note: CookieSyncManager is deprecated since API level 21, but we still need to use it here
    @Override
    @SuppressWarnings("deprecation")
    public void prepareWebViewCookies(Context context) {
        android.webkit.CookieSyncManager.createInstance(context);
    }

    // Cookie data may be lost when an application exists just after it was written.
    // Below API level 21, this problem can be solved by using CookieSyncManager.sync().
    // Note: CookieSyncManager.sync() is deprecated since API level 21, but still needed here
    @Override
    @SuppressWarnings("deprecation")
    public void flushWebViewCookies() {
        android.webkit.CookieSyncManager.getInstance().sync();
    }

    // Below API level 17, there is no simple way to enable the auto play feature of HTML media elements.
    @Override
    public void setHTML5MediaAutoPlay(WebSettings webSettings, Boolean allow) { /* do nothing */ }

    // Immersive full screen isn't ready until API 19
    @SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass")
    protected static final int FULLSCREEN_ALL_GONE = 2;
    @Override
    public void setFullScreen(AbstractFlashcardViewer a) {
        a.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final int fullscreenMode = Integer.parseInt(AnkiDroidApp.getSharedPrefs(a).getString("fullscreenMode", "0"));
        if (fullscreenMode >= FULLSCREEN_ALL_GONE) {
            final View answerButtons = a.findViewById(R.id.answer_options_layout);
            answerButtons.setVisibility(View.GONE);
        }
    }

    // NOTE: we can't use android.R.attr.selectableItemBackground until API 21
    @Override
    public void setSelectableBackground(View view) {
        Context context = view.getContext();
        int[] attrs = new int[] {android.R.attr.colorBackground};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        view.setBackgroundColor(ta.getColor(0, ContextCompat.getColor(context, R.color.white)));
        ta.recycle();
    }

    // Not settable before API 21 so do nothing
    @Override
    public void setStatusBarColor(Window window, int color) { /* do nothing */ }

    // Immersive mode introduced in API 19
    @Override
    public boolean isImmersiveSystemUiVisible(AnkiActivity activity) { return false; }

    @Override
    public void setupNotificationChannel(Context context, String id, String name) { /* pre-API26, do nothing */ }

    // Until API 24 we ignore flags
    @Override
    @SuppressWarnings("deprecation")
    public Spanned fromHtml(String htmlString) {
        return Html.fromHtml(htmlString);
    }

    // Until API 18 it's not a long it's an int
    @Override
    @CheckResult
    @SuppressWarnings("deprecation")
    public long getAvailableBytes(StatFs stat) {
        long availableBlocks = stat.getAvailableBlocks();
        long blockSize = stat.getBlockSize();
        return availableBlocks * blockSize;
    }

    // Until API 23 the methods have "current" in the name
    @Override
    @SuppressWarnings("deprecation")
    public void setTime(TimePicker picker, int hour, int minute) {
        picker.setCurrentHour(hour);
        picker.setCurrentMinute(minute);
    }
    @Override
    @SuppressWarnings("deprecation")
    public int getHour(TimePicker picker) { return picker.getCurrentHour(); }
    @Override
    @SuppressWarnings("deprecation")
    public int getMinute(TimePicker picker) { return picker.getCurrentMinute(); }

    // Until API 21 it's Camera v1
    @Override
    @SuppressWarnings("deprecation")
    public int getCameraCount() { return android.hardware.Camera.getNumberOfCameras(); }

    // Until API 26 just specify time, after that specify effect also
    @Override
    @SuppressWarnings("deprecation")
    public void vibrate(Context context, long durationMillis) {
        Vibrator vibratorManager = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibratorManager != null) {
            vibratorManager.vibrate(durationMillis);
        }
    }

    // Until API 26 do the copy using streams
    public void copyFile(@NonNull String source, @NonNull String target) throws IOException {
        try (InputStream fileInputStream = new FileInputStream(new File(source))) {
            copyFile(fileInputStream, target);
        } catch (IOException e) {
            Timber.e(e, "copyFile() error copying source %s", source);
            throw e;
        }
    }

    // Until API 26 do the copy using streams
    public long copyFile(@NonNull String source, @NonNull OutputStream target) throws IOException {
        long count;

        try (InputStream fileInputStream = new FileInputStream(new File(source))) {
            count = copyFile(fileInputStream, target);
        } catch (IOException e) {
            Timber.e(e, "copyFile() error copying source %s", source);
            throw e;
        }

        return count;
    }

    // Until API 26 do the copy using streams
    public long copyFile(@NonNull InputStream source, @NonNull String target) throws IOException {
        long bytesCopied;

        try (OutputStream targetStream = new FileOutputStream(target)) {
            bytesCopied = copyFile(source, targetStream);
        } catch (IOException ioe) {
            Timber.e(ioe, "Error while copying to file %s", target);
            throw ioe;
        }
        return bytesCopied;
    }

    private long copyFile(@NonNull InputStream source, @NonNull OutputStream target) throws IOException {
        // balance memory and performance, it appears 32k is the best trade-off
        // https://stackoverflow.com/questions/10143731/android-optimal-buffer-size
        final byte[] buffer = new byte[1024 * 32];
        long count = 0;
        int n;
        while ((n = source.read(buffer)) != -1) {
            target.write(buffer, 0, n);
            count += n;
        }
        target.flush();
        return count;
    }

    @Override
    public Object initTtsParams() {
        return new HashMap<String, String>();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int speak(TextToSpeech tts, String text, int queueMode, Object ttsParams, String utteranceId) {
        HashMap<String, String> params = (HashMap) ttsParams;
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        return tts.speak(text, queueMode, params);
    }

    @Override
    @SuppressWarnings("deprecation")
    public long getVersionCode(PackageInfo pInfo) {
        return pInfo.versionCode;
    }
}
