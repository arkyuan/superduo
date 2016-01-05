package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by ark on 1/2/2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    public static final String SELECTED_MATCH_ID = "selectedmid";
    public static final String SELECTED_POSITION = "selectedpos";
    // these indices must match the projection
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    public double detail_match_id = 0;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Date fragmentdate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                String[] mfragmentdate = new String[1];
                mfragmentdate[0] = mformat.format(fragmentdate);
                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        null,
                        null,
                        mfragmentdate,
                        null);
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);
                String home_name = data.getString(COL_HOME);
                String away_name = data.getString(COL_AWAY);
                String match_time = data.getString(COL_MATCHTIME);
                String scores = Utilies.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS));
                double match_id = data.getDouble(COL_ID);
                //int home_crest = Utilies.getTeamCrestByTeamName(data.getString(COL_HOME));
                //int away_crest = Utilies.getTeamCrestByTeamName(data.getString(COL_AWAY));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.widget_home_name, home_name);
                    views.setContentDescription(R.id.widget_away_name, away_name);
                    views.setContentDescription(R.id.widget_score_textview, scores);
                    views.setContentDescription(R.id.widget_data_textview, match_time);
                }

                views.setTextViewText(R.id.widget_home_name, home_name);
                views.setTextViewText(R.id.widget_away_name, away_name);
                views.setTextViewText(R.id.widget_score_textview,scores);
                views.setTextViewText(R.id.widget_data_textview, match_time);

                //views.setImageViewResource(R.id.home_crest, home_crest);
                //views.setImageViewResource(R.id.away_crest, away_crest);

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(SELECTED_MATCH_ID, match_id);
                fillInIntent.putExtra(SELECTED_POSITION, position);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }


            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
