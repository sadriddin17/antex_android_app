package uz.codic.ahmadtea.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;
import java.util.Random;

import uz.codic.ahmadtea.BuildConfig;
import uz.codic.ahmadtea.R;
import uz.codic.ahmadtea.data.db.entities.User;
import uz.codic.ahmadtea.service.news.LocationMonitoringService;
import uz.codic.ahmadtea.ui.base.BaseActivity;
import uz.codic.ahmadtea.ui.dailyPlan.DailyFragment;
import uz.codic.ahmadtea.ui.dashboard.DashboardFragment;
import uz.codic.ahmadtea.ui.login.LoginActivity;
import uz.codic.ahmadtea.ui.mainpage.FragmentsListener;
import uz.codic.ahmadtea.ui.mainpage.MainActivityPresenter;
import uz.codic.ahmadtea.ui.mainpage.MainActivityView;
import uz.codic.ahmadtea.ui.mainpage.leftusers.LeftUsersAdapter;
import uz.codic.ahmadtea.ui.merchants.MerchantsFragment;
import uz.codic.ahmadtea.ui.new_merchants.NewMerchantsFragment;
import uz.codic.ahmadtea.ui.report.ReportFragment;
import uz.codic.ahmadtea.ui.report.basketList.BasketActivity;
import uz.codic.ahmadtea.ui.saved_visits.SavedVisits;
import uz.codic.ahmadtea.ui.sittings.VersionInfoActivity;
import uz.codic.ahmadtea.ui.synchronisation.SynchronisationFragment;
import uz.codic.ahmadtea.utils.Consts;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MainActivityView, FragmentsListener,
        DashboardFragment.IUpdateDashboard,
        BasketActivity.IUpdateSavedVisits{

    //region Old Staffs
    String id_employee;
    SearchView mSearchView;
    MenuItem map_item;
    MenuItem calendar_item;
    MenuItem filter_item;
    MenuItem search_item;
    TextView agentName;
    TextView agentRole;
    LinearLayout lnl_nav_head;
    Toolbar toolbar;
    MenuItem nav_item_new_merchants;
    Fragment fragment = null;
    Class fragmentClass = null;


    MainActivityPresenter<MainActivityView> presenter;
    LeftUsersAdapter adapter;
    //region Server properties
    public static final String TAG = "AhmadTeaLogLocation";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private boolean mAlreadyStartedService = false;

    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presenter = new MainActivityPresenter<>(this);
        presenter.onAttach(this);
        adapter = new LeftUsersAdapter(this);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        nav_item_new_merchants = navigationView.getMenu().findItem(R.id.nav_new_merchants);
        if (presenter.getDataManager().isAdmin()) {
            nav_item_new_merchants.setVisible(true);
        } else {
            nav_item_new_merchants.setVisible(false);
        }

        agentName = headerView.findViewById(R.id.navHeader_userName);
        agentRole = headerView.findViewById(R.id.navHeader_userRole);
        lnl_nav_head = headerView.findViewById(R.id.id_lnl_nav_head);

        ((RecyclerView) findViewById(R.id.userRecycler)).setAdapter(adapter);
        presenter.LoadUsers();

        //region Initial step
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        id_employee = getIntent().getStringExtra("id_employee");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this); //Why we need this


        //default fragment
        fragmentClass = DashboardFragment.class;
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
        DashboardFragment.setUpdater(this);
        //startStep1();
        //endregion

    }

    public void onAddFabClickListener(View v) {
        Intent login = new Intent(MainActivity.this, LoginActivity.class);
        login.putExtra("isFirstTime", false);
        startActivity(login);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!mSearchView.isIconified())
                mSearchView.onActionViewCollapsed();
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Вы хотите выйти из приложение?");
                builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       exit();
                    }
                });
                builder.show();
            }

        }
    }

    private void exit(){
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        map_item = menu.findItem(R.id.app_bar_map);
        calendar_item = menu.findItem(R.id.app_bar_calendar);
        filter_item = menu.findItem(R.id.app_bar_filter);
        search_item = menu.findItem(R.id.app_bar_search);


        //for ()

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.app_bar_search));
        final Toast toast = new Toast(this);

        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            mSearchView.setIconifiedByDefault(true);

            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                String mSearchString;

                public boolean onQueryTextChange(String newText) {

                    mSearchString = newText;
                    //doFilterAsync(mSearchString);
                    Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (f instanceof MerchantsFragment) {
                        ((MerchantsFragment) f).searcMerchant(newText);
                    } else toast.makeText(getApplicationContext(), "no", Toast.LENGTH_LONG).show();

                    return true;
                }

                public boolean onQueryTextSubmit(String query) {
                    mSearchString = query;
                    //doFilterAsync(mSearchString);
                    toast.makeText(getApplicationContext(), query, Toast.LENGTH_LONG).show();

                    return true;
                }


            };

            mSearchView.setOnQueryTextListener(queryTextListener);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        if (id == R.id.app_bar_search) {
            showMessage("yes");
            return true;
        }

        if (id == R.id.app_bar_map) {

            Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof DailyFragment) {
                ((DailyFragment) f).openMap();
            }

            if (f instanceof MerchantsFragment) {
                ((MerchantsFragment) f).openMap();
            }
            return true;
        }
        if (id == R.id.app_bar_calendar) {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            ((DailyFragment) f).openCanlendar();
        }

        if (id == R.id.app_bar_filter) {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof MerchantsFragment) {
                ((MerchantsFragment) f).filter();
            }
            if (f instanceof DailyFragment) {
                ((DailyFragment) f).filter();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    void closeSearchField() {
        if (!mSearchView.isIconified())
            mSearchView.onActionViewCollapsed();
    }

    @SuppressWarnings("StatementWithEmptyBody")

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            search_item.setVisible(false);
            closeSearchField();
            closeMapItem();
            closeCalendarItem();
            closeFilter();
            fragmentClass = DashboardFragment.class;
            DashboardFragment.setUpdater(this);
        } else if (id == R.id.nav_synch) {
            search_item.setVisible(false);
            closeSearchField();
            closeMapItem();
            closeCalendarItem();
            closeFilter();
            fragmentClass = SynchronisationFragment.class;
        } else if (id == R.id.nav_daily_plan) {
            search_item.setVisible(true);
            closeSearchField();
            if (presenter.getDataManager().getMyWorkspaceIds(presenter.getDataManager().getId_employee()).size() > 1) {
                if (!filter_item.isVisible()) {
                    filter_item.setVisible(true);
                }
            }else closeFilter();
            if (!map_item.isVisible()) {
                map_item.setVisible(true);
            }
            if (!calendar_item.isVisible()) {
                calendar_item.setVisible(true);
            }
            fragmentClass = DailyFragment.class;
        } else if (id == R.id.nav_merchants) {
            search_item.setVisible(true);
            closeSearchField();
            if (!map_item.isVisible()) {
                map_item.setVisible(true);
            }
            if (presenter.getDataManager().getMyWorkspaceIds(presenter.getDataManager().getId_employee()).size() > 1) {
                if (!filter_item.isVisible()) {
                    filter_item.setVisible(true);
                }
            }else closeFilter();
            closeCalendarItem();
            fragmentClass = MerchantsFragment.class;
        } else if (id == R.id.nav_orders) {
            search_item.setVisible(true);
            closeSearchField();
            closeFilter();
            closeMapItem();
            closeCalendarItem();
            fragmentClass = ReportFragment.class;
        } else if (id == R.id.nav_saved) {
            search_item.setVisible(true);
            closeSearchField();
            closeFilter();
            closeMapItem();
            closeCalendarItem();
            fragmentClass = SavedVisits.class;
        }
        if (id == R.id.sittings) {
            startActivity(new Intent(MainActivity.this, VersionInfoActivity.class));
            return true;
        }

        if (id == R.id.nav_new_merchants) {
            search_item.setVisible(true);
            closeSearchField();
            closeFilter();
            closeMapItem();
            closeCalendarItem();
            fragmentClass = NewMerchantsFragment.class;
        }
        if (id == R.id.logout) {
            presenter.logOut();
            return true;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();

        item.setChecked(true);
        setTitle(item.getTitle());

        return true;
    }

    private void closeCalendarItem() {
        if (calendar_item.isVisible()) {
            calendar_item.setVisible(false);
        }
    }

    private void closeMapItem() {
        if (map_item.isVisible())
            map_item.setVisible(false);
    }

    private void closeFilter() {
        if (filter_item.isVisible()) {
            filter_item.setVisible(false);
        }
    }

    //region Location started from here

    private void startStep1() {

        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {

            //Passing null to indicate that it is executing for the first time.
            startStep2(null);

        } else {
            Toast.makeText(getApplicationContext(), "No Google play service available", Toast.LENGTH_LONG).show();
        }
    }

    private Boolean startStep2(DialogInterface dialog) {

        if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
            startStep3();
        } else {  //No user has not granted the permissions yet. Request now.
            requestPermissions();
        }
        return true;
    }


    private void startStep3() {

        if (!mAlreadyStartedService) {
            //Start location sharing service to app server.........
            Intent intent = new Intent(this, LocationMonitoringService.class);
            startService(intent);

            mAlreadyStartedService = true;
            //Ends................................................
        }
    }


    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

    }


    /**
     * Start permissions requests.
     */
    private void requestPermissions() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //   turnGPSOn(); //I am turning on the GPS if dissabled.
                startStep3();

            } else {


                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private void turnGPSOn() {

        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) { //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }

    }

    @Override
    public void userListReady(List<User> all) {
        adapter.setData(all);
    }

    @Override
    public void initializeUser(User user) {
        agentName.setText(user.getName());
        agentRole.setText(user.getRole_label());
        presenter.getDataManager().putToken(user.getToken());
        presenter.getDataManager().setId_employee(user.getId());
        if (!user.getRole_label().isEmpty())
            if (user.getRole_label().equals("admin")) {
                presenter.getDataManager().changeIsAdmin(true);
                nav_item_new_merchants.setVisible(true);
            } else {
                presenter.getDataManager().changeIsAdmin(false);
                nav_item_new_merchants.setVisible(false);
            }
    }

    @Override
    public void initializeUserForButton(User user) {

        if (!user.getId().equals(presenter.getDataManager().getId_employee())) {
            Random rnd = new Random();
            agentName.setText(user.getName());
            agentRole.setText(user.getRole_label());
            lnl_nav_head.setBackgroundColor(Color.parseColor(Consts.colors[rnd.nextInt(6)]));
            presenter.getDataManager().putToken(user.getToken());
            presenter.getDataManager().setId_employee(user.getId());
            if (user.getRole_label().equals("admin")) {
                presenter.getDataManager().changeIsAdmin(true);
                nav_item_new_merchants.setVisible(true);
            } else {
                presenter.getDataManager().changeIsAdmin(false);
                nav_item_new_merchants.setVisible(false);
            }
            //presenter.getWorkspaceAndMerchant();
            updateFragment();
        }
    }

    private void updateFragment() {

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof NewMerchantsFragment && !presenter.getDataManager().isAdmin()) {
            try {
                fragment = (Fragment) DashboardFragment.class.newInstance();
                setTitle("AntEx");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        } else {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }


    // listener override metodes
    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void functionBeforeLogOut(boolean isLogin) {
        Intent login = new Intent(MainActivity.this, LoginActivity.class);
        if (isLogin) {
            login.putExtra("isFirstTime", false);
            startActivity(login);
        } else {
            login.putExtra("isFirstTime", true);
            startActivity(login);
            finish();
        }

    }

    @Override
    public void updateDailyPlan() {
        search_item.setVisible(true);
        closeSearchField();
        closeFilter();
        if (!map_item.isVisible()) {
            map_item.setVisible(true);
        }
        closeCalendarItem();
//        if (!calendar_item.isVisible()) {
//            calendar_item.setVisible(true);
//        }
            fragment = DailyFragment.newInstance(1);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void updateDailyOutOfDaily() {
        search_item.setVisible(true);
        closeSearchField();
        closeFilter();
        if (!map_item.isVisible()) {
            map_item.setVisible(true);
        }
        closeCalendarItem();
//        if (!calendar_item.isVisible()) {
//            calendar_item.setVisible(true);
//        }
        fragment = DailyFragment.newInstance(2);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void updateAllDoneVisits() {
        search_item.setVisible(true);
        closeSearchField();
        closeFilter();
        if (!map_item.isVisible()) {
            map_item.setVisible(true);
        }
        closeCalendarItem();
//        if (!calendar_item.isVisible()) {
//            calendar_item.setVisible(true);
//        }
        fragment = DailyFragment.newInstance(3);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    @Override
    public void updateNow() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Consts.isSaved){
            search_item.setVisible(true);
            closeSearchField();
            closeFilter();
            closeMapItem();
            closeCalendarItem();
            fragmentClass = SavedVisits.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
            Consts.isSaved = false;
        }
    }
}
