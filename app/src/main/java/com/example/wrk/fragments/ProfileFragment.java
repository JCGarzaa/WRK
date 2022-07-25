package com.example.wrk.fragments;

import static android.app.Activity.RESULT_OK;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wrk.GymMapsActivity;
import com.example.wrk.MainActivity;
import com.example.wrk.ProfileAdapter;
import com.example.wrk.R;
import com.example.wrk.models.WorkoutPerformed;
import com.google.android.material.navigation.NavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileFragment extends Fragment {
    public static final String TAG = "ProfileFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public final static int PICK_PHOTO_CODE = 1046;
    public String photoFileName = "pfp.jpg";

    private MainActivity mainActivity;
    private ParseUser user;
    private ImageView ivProfilePicture;
    private File photoFile;
    private TextView tvProfileName;
    private TextView tvProfileUsername;
    private TextView tvDailyStreak;
    private TextView tvWorkoutsThisMonth;
    private ImageButton ibGymsNearMe;
    private ImageButton ibFollow;
    private RecyclerView rvPrevWorkouts;
    private ProgressBar progressBar;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    protected ProfileAdapter adapter;
    protected List<WorkoutPerformed> workoutsPerformed;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public ProfileFragment(MainActivity mainActivity, ParseUser user) {
        this.mainActivity = mainActivity;
        this.user = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDrawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.navmenu);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                // perform action based on which item pressed
                switch (item.getItemId()) {
                    case R.id.itemLogout:
                        ParseUser.getCurrentUser().logOut();
                        mainActivity.goLogin();
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        progressBar = view.findViewById(R.id.pbLoading);

        ParseFile file = user.getParseFile("profilePic");
        // check if there is a profile picture that the user has
        if (file != null) {
            Glide.with(getContext())
                    .load(file.getUrl())
                    .centerCrop()
                    .transform(new RoundedCorners(150))
                    .into(ivProfilePicture);
        }
        else {
            // upload a default picture
            Glide.with(getContext())
                    .load(R.drawable.instagram_user_filled_24)
                    .centerCrop()
                    .transform(new RoundedCorners(150))
                    .into(ivProfilePicture);
        }

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileName.setText(user.getString("name"));

        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        tvProfileUsername.setText(user.getUsername() + " ");

        tvDailyStreak = view.findViewById(R.id.tvDailyStreak);
        tvDailyStreak.setText(String.valueOf(user.getInt("streak")));
        if (user.getInt("streak") == 1) {
            tvDailyStreak.append(" day");
        }
        else {
            tvDailyStreak.append(" days");
        }

        tvWorkoutsThisMonth = view.findViewById(R.id.tvWorkoutsThisMonth);
        tvWorkoutsThisMonth.setText(String.valueOf(user.getInt("workoutsThisMonth")));

        if (user.hasSameId(ParseUser.getCurrentUser())) {
            updateStreak(user);
            // check if user follows anyone (user follows themselves by default)
            if (user.getList("following").size() > 1) {
                // add percentage in comparison to your friends
                try {
                    double percentage = 0;
                    percentage = calcPercentage();
                    if (percentage >= 0) {
                        tvWorkoutsThisMonth.append(" (" + percentage + "% more than people you follow!)");
                    }
                    else {
                        tvWorkoutsThisMonth.append(" (" + (percentage * -1) + "% less than people you follow!)");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            ivProfilePicture.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(getContext(), v);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.itemTakePicture:
                                    launchCamera();
                                    break;
                                case R.id.itemUploadPicture:
                                    onPickPhoto(ivProfilePicture);
                                    break;
                            }
                            return false;
                        }
                    });
                    MenuInflater inflater = popupMenu.getMenuInflater();
                    inflater.inflate(R.menu.menu_popup, popupMenu.getMenu());
                    popupMenu.show();
                }
            });

            // send user to their profile page if they click on their own profile
            ibGymsNearMe = view.findViewById(R.id.ibProfile);
            ibGymsNearMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getContext(), GymMapsActivity.class);
                    startActivity(i);
                }
            });
        }
        else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);  // disable drawer from opening
            ibFollow = view.findViewById(R.id.ibProfile);
            if (isFollowedByCurrentUser()) {
                ibFollow.setBackground(getResources().getDrawable(android.R.drawable.btn_star_big_on));
            }
            else {
                ibFollow.setBackground(getResources().getDrawable(android.R.drawable.btn_star_big_off));
            }

            ibFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFollowedByCurrentUser()) {
                        unFollow();
                        ibFollow.setBackground(getResources().getDrawable(android.R.drawable.btn_star_big_off));
                    }
                    else {
                        follow();
                        ibFollow.setBackground(getResources().getDrawable(android.R.drawable.btn_star_big_on));
                    }
                }
            });
        }

        rvPrevWorkouts = view.findViewById(R.id.rvPosts);
        rvPrevWorkouts.setHasFixedSize(true);      // allows for optimizations
        workoutsPerformed = new ArrayList<>();
        // 2 column grid layout
        final GridLayoutManager layout = new GridLayoutManager(getContext(), 2);
        rvPrevWorkouts.setLayoutManager(layout);
        adapter = new ProfileAdapter(getContext(), workoutsPerformed);
        rvPrevWorkouts.setAdapter(adapter);
        queryWorkouts();
    }

    private double calcPercentage() throws ParseException {
        ParseUser currentUser = ParseUser.getCurrentUser();
        List<ParseUser> followingList = getFollowing();
        List<ParseUser> friendList = new ArrayList<>();
        // to ensure that signed in user is not included in the calculations (they follow themselves by default)
        for (int i = 0; i < followingList.size(); i++) {
            if (!followingList.get(i).hasSameId(currentUser)) {
                friendList.add(followingList.get(i));
            }
        }
        int friendsWorkoutsThisMonth = 0;
        double percentDifference = 0;
        if (friendList.size() != 0) {
            for (int i = 0; i < friendList.size(); i++) {
                friendsWorkoutsThisMonth += friendList.get(i).fetchIfNeeded().getInt("workoutsThisMonth");
            }
            // calculate percent difference
            double averageWorkoutsThisMonth = friendsWorkoutsThisMonth / friendList.size(); // # of workouts / friends
            double difference = currentUser.getInt("workoutsThisMonth") - averageWorkoutsThisMonth;
            percentDifference = (difference / averageWorkoutsThisMonth) * 100;
        }
        return Math.round(percentDifference * 10.0) / 10.0; // round to one decimal
    }

    private void unFollow() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        List<ParseUser> followingList = getFollowing();
        // remove user from currentUsers follow list
        for (int i = 0; i < followingList.size(); i++) {
            if (followingList.get(i).hasSameId(user)) {
                followingList.remove(i);
            }
        }
        currentUser.put("following", followingList);
        currentUser.saveInBackground();
    }

    private void follow() {
        unFollow();     // to ensure there is no duplicates of users added
        List<ParseUser> followingList = getFollowing();
        ParseUser currentUser = ParseUser.getCurrentUser();
        followingList.add(user);        // add user to currentUsers follow list
        currentUser.put("following", followingList);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(mainActivity, "Error saving user.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isFollowedByCurrentUser() {
        List<ParseUser> following = getFollowing();
        for (int i = 0; i< following.size(); i++) {
            if (following.get(i).hasSameId(user)) {
                return true;
            }
        }
        return false;
    }

    protected List<ParseUser> getFollowing() {
        List<ParseUser> users = ParseUser.getCurrentUser().getList("following");
        if (users == null) {
            return new ArrayList<>();
        }
        return users;
    }

    protected void queryWorkouts() {
        ParseQuery<WorkoutPerformed> performedQuery = ParseQuery.getQuery(WorkoutPerformed.class);
        // includes specified data
        performedQuery.whereEqualTo(WorkoutPerformed.KEY_USER, user);
        performedQuery.include(WorkoutPerformed.KEY_USER);
        performedQuery.include(WorkoutPerformed.KEY_WORKOUT);
        // limits number of items to generate
        performedQuery.setLimit(4);
        // order by creation date (newest first)
        performedQuery.addDescendingOrder("createdAt");
        // async call for posts
        performedQuery.findInBackground(new FindCallback<WorkoutPerformed>() {
            @Override
            public void done(List<WorkoutPerformed> workouts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with fetching workouts. ", e);
                    return;
                }
                progressBar.setVisibility(View.GONE);   // hide progress bar
                // save received posts to list and notify adapter of new data
                workoutsPerformed.addAll(workouts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void updateStreak(ParseUser user) {
        Date lastWorkout = user.getDate("lastWorkout");
        int streak = user.getInt("streak");     // current daily streak
        int workoutsThisMonth = user.getInt("workoutsThisMonth");   // current number workouts this month
        int lastWorkoutMonth = -1;      // initialize in case of null
        long timeDifference = 0;
        final long SECONDS_IN_DAY = 86400;
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();

        if (lastWorkout != null) {
            // calculate difference in days since last workout
            long todayTime = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

            LocalDate recentWorkout = lastWorkout.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long recentWorkoutTime = recentWorkout.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            lastWorkoutMonth = recentWorkout.getMonthValue();

            timeDifference = todayTime - recentWorkoutTime;
        }

        // reset streak to 0 if more than 1 day has past since working out
        if (lastWorkout == null || timeDifference > SECONDS_IN_DAY) {
            streak = 0;
        }
        // reset workouts this month to 0 if new month has started and no workouts
        if (lastWorkoutMonth != currentMonth) {
            workoutsThisMonth = 0;
        }

        // save to database
        user.put("streak", streak);
        user.put("workoutsThisMonth", workoutsThisMonth);
        user.saveInBackground();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // only create the settings menu button for a user's own profile
        if (user.hasSameId(ParseUser.getCurrentUser())) {
            mainActivity.getMenuInflater().inflate(R.menu.menu_top, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuSettings) {
            // check if drawer is already opened
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
            }
            else {
                mDrawerLayout.openDrawer(GravityCompat.END);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider, required for API >= 24
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.wrk.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Trigger gallery selection for a photo
    private void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // Load the taken image into a preview
                Glide.with(this).load(takenImage)
                        .centerCrop()
                        .transform(new RoundedCorners(450))
                        .into(ivProfilePicture);
                ParseUser.getCurrentUser().put("profilePic", new ParseFile(photoFile));
                ParseUser.getCurrentUser().saveInBackground();
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
                // Load the selected image into a profile
                Glide.with(this).load(bitmap)
                        .centerCrop()
                        .transform(new RoundedCorners(450))
                        .disallowHardwareConfig()
                        .into(ivProfilePicture);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
                byte[] bitmapBytes = stream.toByteArray();
                ParseUser.getCurrentUser().put("profilePic", new ParseFile("image", bitmapBytes));
                ParseUser.getCurrentUser().saveInBackground();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }
        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }
}