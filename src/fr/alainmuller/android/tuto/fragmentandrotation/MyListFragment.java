package fr.alainmuller.android.tuto.fragmentandrotation;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Alain Muller
 * Date: 18/09/12
 * Time: 14:30
 * Fragment permettant de gérer un traitement en tâche de fond, ici représenté par une AsyncTask
 * afin de conserver l'exécution du traitement en tâche de fond lors du changement d'orientation
 */
public class MyListFragment extends android.support.v4.app.ListFragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<Void> {

    public static final String LOG_TAG = "MyListFragment";

    private MyAdapter mAdapter;
    private ArrayList<String> mItems;
    private LayoutInflater mInflater;
    private Button mBtnReload;

    private boolean mFirstRun = true;
    private final Handler mHandler = new Handler();

    private static final String[] WORDS = {"Lorem", "ipsum", "dolor", "sit",
            "amet", "consectetur", "adipiscing", "elit", "Fusce", "pharetra",
            "luctus", "sodales"};

    private static final int SIZE = 12;
    private static final int SLEEP = 5000;

    private final int wordBarColor = R.color.word_bar;
    private Resources mRes;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Cette ligne est très importante : elle permet de préciser qu'on va conserver l'instance du Fragment
        // si la vue est détachée (par un changement d'orientation par exemple)
        setRetainInstance(true);

        // Traces de déboggage du LoaderManager
//        LoaderManager.enableDebugLogging(true);

        mRes = getResources();
        mInflater = LayoutInflater.from(getActivity());

        mBtnReload = (Button) getActivity().findViewById(R.id.btn);
        mBtnReload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // first time
                if (mFirstRun) {

                    mFirstRun = false;
                    mBtnReload.setText(mRes.getString(R.string.restart));

                    startLoading();
                }
                // already started once
                else {
                    restartLoading();
                }
            }
        });

        // you only need to instantiate these the first time your fragment is
        // created; then, the method above will do the rest
        if (mAdapter == null) {
            mItems = new ArrayList<String>();
            mAdapter = new MyAdapter(getActivity(), mItems);
        }
        getListView().setAdapter(mAdapter);

        // ---- magic lines starting here -----
        // call this to re-connect with an existing
        // loader (after screen configuration changes for e.g!)
        android.support.v4.app.LoaderManager lm = getLoaderManager();
        if (lm.getLoader(0) != null) {
            lm.initLoader(0, null, this);
        }
        // ----- end magic lines -----

        if (!mFirstRun) {
            mBtnReload.setText(mRes.getString(R.string.restart));
        }
    }

    protected void startLoading() {
        showDialog();

        // first time we call this loader, so we need to create a new one
        getLoaderManager().initLoader(0, null, this);
    }

    protected void restartLoading() {
        showDialog();

        mItems.clear();
        mAdapter.notifyDataSetChanged();
        getListView().invalidateViews();

        // --------- the other magic lines ----------
        // call restart because we want the background work to be executed
        // again
        Log.d(LOG_TAG, "restartLoading(): re-starting loader");
        getLoaderManager().restartLoader(0, null, this);
        // --------- end the other magic lines --------
    }

    @Override
    public android.support.v4.content.Loader<Void> onCreateLoader(int id, Bundle args) {
        AsyncTaskLoader<Void> loader = new AsyncTaskLoader<Void>(getActivity()) {

            @Override
            public Void loadInBackground() {
                try {
                    // simulate some time consuming operation going on in the
                    // background
                    Log.d(LOG_TAG, "loadInBackground(): doing some work....");
                    for (int i = 0; i < SIZE; ++i) {
                        mItems.add(WORDS[i]);
                    }

                    Thread.sleep(SLEEP);
                }
                catch (InterruptedException e)
                {
                    Log.d(LOG_TAG, "loadInBackground(): InterruptedException : '" + String.valueOf(e) + "'");
                }
                return null;
            }
        };
        // somehow the AsyncTaskLoader doesn't want to start its job without
        // calling this method
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Void> loader, Void result) {

        mAdapter.notifyDataSetChanged();
        hideDialog();
        Log.d(LOG_TAG, "onLoadFinished(): done loading!");

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Void> loader) {
    }

    private class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context, List<String> objects) {
            super(context, R.layout.list_item, R.id.text, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Wrapper wrapper;

            if (view == null) {
                view = mInflater.inflate(R.layout.list_item, null);
                wrapper = new Wrapper(view);
                view.setTag(wrapper);
            } else {
                wrapper = (Wrapper) view.getTag();
            }

            wrapper.getTextView().setText(getItem(position));
            wrapper.getBar().setBackgroundColor(
                    getResources().getColor(wordBarColor));
            return view;
        }

    }

    // use an wrapper (or view holder) object to limit calling the
    // findViewById() method, which parses the entire structure of your
    // XML in search for the ID of your view
    private static class Wrapper {
        private final View mRoot;
        private TextView mText;
        private View mBar;

        public Wrapper(View root) {
            mRoot = root;
        }

        public TextView getTextView() {
            if (mText == null) {
                mText = (TextView) mRoot.findViewById(R.id.text);
            }
            return mText;
        }

        public View getBar() {
            if (mBar == null) {
                mBar = mRoot.findViewById(R.id.bar);
            }
            return mBar;
        }
    }

    public static class MyAlertDialog extends DialogFragment {
        /*
           * All subclasses of Fragment must include a public empty constructor.
           * The framework will often re-instantiate a fragment class when needed,
           * in particular during state restore, and needs to be able to find this
           * constructor to instantiate it. If the empty constructor is not
           * available, a runtime exception will occur in some cases during state
           * restore.
           */
        public MyAlertDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog progress = new ProgressDialog(getActivity());
            progress.setMessage(getString(R.string.loading));
            return progress;
        }
    }

    private void showDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }

        // Create and show the dialog.
        DialogFragment newFragment = new MyAlertDialog();
        newFragment.show(ft, "dialog");
    }

    private void hideDialog() {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                FragmentTransaction ft = getFragmentManager()
                        .beginTransaction();
                Fragment prev = getFragmentManager()
                        .findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev).commit();
                }
            }
        });
    }

}