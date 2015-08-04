package cl.monsoon.s1next.view.fragment;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import cl.monsoon.s1next.BuildConfig;
import cl.monsoon.s1next.R;
import cl.monsoon.s1next.databinding.FragmentHelpBinding;
import cl.monsoon.s1next.util.ToastUtil;
import cl.monsoon.s1next.view.activity.OpenSourceLicensesActivity;
import cl.monsoon.s1next.viewmodel.WebPageViewModel;

/**
 * A Fragment represents a help page.
 * <p>
 * Also some related controls are provided in overflow menu:
 * 1.Link our app to Android marketplaces or Google Play website.
 * 2.See open sources licenses information.
 * 3.See version number.
 */
public final class HelpFragment extends Fragment {

    public static final String TAG = HelpFragment.class.getName();

    private static final String HELP_PAGE_URL = "http://monsoon.cl/S1-Next/HELP.html";

    // https://developer.android.com/distribute/tools/promote/linking.html#OpeningDetails
    private static final String ANDROID_APP_MARKET_LINK = "market://details?id=%s";
    private static final String ANDROID_WEB_SITE_MARKET_LINK = "http://play.google.com/store/apps/details?id=%s";

    private FragmentHelpBinding mFragmentHelpBinding;
    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentHelpBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_help, container,
                false);
        mWebView = mFragmentHelpBinding.webView;
        return mFragmentHelpBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WebPageViewModel viewModel = new WebPageViewModel();
        mFragmentHelpBinding.setWebPageViewModel(viewModel);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                viewModel.setFinishedLoading(true);
            }
        });

        // restore the state of WebView on configuration change
        // see http://www.devahead.com/blog/2012/01/preserving-the-state-of-an-android-webview-on-screen-orientation-change/
        if (savedInstanceState == null) {
            mWebView.loadUrl(HELP_PAGE_URL);
        } else {
            mWebView.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_help, menu);

        menu.findItem(R.id.menu_version).setTitle(getString(R.string.menu_version,
                BuildConfig.VERSION_NAME));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_view_in_google_play_store:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String packageName = getActivity().getPackageName();
                intent.setData(Uri.parse(String.format(ANDROID_APP_MARKET_LINK, packageName)));
                try {
                    // link our app in Android marketplaces
                    startActivity(intent);
                } catch (ActivityNotFoundException exception) {
                    intent.setData(Uri.parse(String.format(ANDROID_WEB_SITE_MARKET_LINK, packageName)));
                    try {
                        // link our app in Google Play website if user hasn't installed any Android marketplaces
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // show Toast if user hasn't installed any Android marketplaces or browsers
                        ToastUtil.showByResId(R.string.message_chooser_no_applications,
                                Toast.LENGTH_SHORT);
                    }
                }

                return true;
            case R.id.menu_open_source_licenses:
                OpenSourceLicensesActivity.startOpenSourceLicensesActivity(getActivity());

                return true;
            case R.id.menu_version:
                // copy version number to clipboard though it make no sense actually
                ClipboardManager clipboardManager = (ClipboardManager)
                        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("simple text", item.getTitle());
                clipboardManager.setPrimaryClip(clipData);
                ToastUtil.showByResId(R.string.message_version_number_copied, Toast.LENGTH_SHORT);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mWebView.saveState(outState);
    }

    public WebView getWebView() {
        return mWebView;
    }
}