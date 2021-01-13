package com.llw.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.Transliterator;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.llw.music.adapter.ChooseMusicListAdapter;
import com.llw.music.adapter.MusicListAdapter;
import com.llw.music.model.Song;
import com.llw.music.service.MusicService;
import com.llw.music.utils.Constant;
import com.llw.music.utils.HttpUtil;
import com.llw.music.utils.MusicUtils;
import com.llw.music.utils.ObjectUtils;
import com.llw.music.utils.SPUtils;
import com.llw.music.utils.StatusBarUtil;
import com.llw.music.utils.ToastUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.llw.music.utils.DateUtil.parseTime;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    /**
     * 播放列表draw_layout的相关布局*/
//    @BindView(R.id.music_list_item)
//    LinearLayout musicListItem;
//    @BindView(R.id.list_view)
//    RecyclerView listView;
    @BindView(R.id.btn_close)
    TextView btnClose;

    /**
     * activity_music_player里的相关布局 */
    @BindView(R.id.music_player)
    LinearLayout musicPlayer;
    @BindView(R.id.music_btn_back)
    ImageView musicBack;
    @BindView(R.id.music_album_art)
    ImageView musicAlbumArt;
    @BindView(R.id.music_song_name)
    TextView musicSongName;
    @BindView(R.id.music_singer_name)
    TextView musicSingerName;
    @BindView(R.id.music_seekBar_control)
    LinearLayout musicSeekBarControl;
    @BindView(R.id.music_play_time)
    TextView musicPlayTime;
    @BindView(R.id.music_time_seekBar)
    SeekBar musicTimeSeekBar;
    @BindView(R.id.music_total_time)
    TextView musicTotalTime;
    @BindView(R.id.player_music_model)
    ImageView playerMusicModel;
    @BindView(R.id.music_btn_previous)
    ImageView musicBtnPrevious;
    @BindView(R.id.music_btn_play_or_pause)
    ImageView musicBtnPlayOrPause;
    @BindView(R.id.music_btn_next)
    ImageView musicBtnNext;
    @BindView(R.id.music_selected_player_list)
    ImageView musicSelectedPlayerList;

    /**
    * activity_main里的相关布局*/
    @BindView(R.id.drawer_layout)
    DrawerLayout chooseMusic;
    @BindView(R.id.selected_music_list)
    ImageView selectedMusic;
    @BindView(R.id.activity_main_lay)
    FrameLayout mainLay;
    @BindView(R.id.bing_pic_img)
    ImageView bingPicImg;
    @BindView(R.id.rv_music)
    RecyclerView rvMusic;
    @BindView(R.id.btn_scan)
    Button btnScan;
    @BindView(R.id.scan_lay)
    LinearLayout scanLay;
//    @BindView(R.id.tv_clear_list)
//    TextView tvClearList;
    @BindView(R.id.change_background)
    TextView change_background;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.list_title)
    TextView listTitle;
    @BindView(R.id.back_btn)
    TextView back_btn;
    @BindView(R.id.play_album_img)
    ImageView albumImg;
    @BindView(R.id.show_music_info)
    TextView musicInfo;

    private static final String TAG = "MainActivity";
    /**
    * 绑定服务及通知栏*/
//    private MusicService.MusicBinder musicBinder;
//    private MusicService musicService;

    /**
     * 播放界面所需变量*/
    private ChooseMusicListAdapter adapter;//歌曲列表适配器
    private MusicListAdapter mAdapter;//歌曲适配器
    private List<Song> mList;//歌曲列表
    private RxPermissions rxPermissions;//权限请求
    private String musicData = null;
    private boolean changeBackground = true;
    public int playModel = 0;
    public int PLAY_IN_ORDER = 0;   //顺序播放
    public int PLAY_RANDOM = 1;    //随机播放
    public int PLAY_SINGLE = 2;    //单曲循环

    /**
     * 碎片和活动之间通信，需要设置为public的变量*/
    public int i = 0;
    public boolean playMusic = false;
    public boolean firstClick = true;
    public MediaPlayer mediaPlayer;//音频播放器
    public ImageView btnPlayOrPause;

    // 记录当前播放歌曲的位置
    public int mCurrentPosition;
    private static final int INTERNAL_TIME = 1000;// 音乐进度间隔时间

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            // 展示给进度条和当前时间
            int progress = mediaPlayer.getCurrentPosition();
//            timeSeekBar.setProgress(progress);
//            tvPlayTime.setText(parseTime(progress));

            musicTimeSeekBar.setProgress(progress);
            musicPlayTime.setText(parseTime(progress));
            // 继续定时发送数据
            updateProgress();
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         *判断android系统是否在5.0(版本号大于等于21)及以上；若满足，则调用app与系统状态栏融为一体；反之...
         * */
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);

        btnPlayOrPause = (ImageView) findViewById(R.id.btn_play_or_pause);
        ActivityCollector.addActivity(this);//将当前活动添加到活动管理器中
        ButterKnife.bind(this);
        StatusBarUtil.StatusBarLightMode(this);
        rxPermissions = new RxPermissions(this);//使用前先实例化
//        timeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);//滑动条监听
        musicTimeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        musicData = SPUtils.getString(Constant.MUSIC_DATA_FIRST, "yes", this);
//        musicPlayOrPause = (ImageView) findViewById(R.id.music_play_or_pause);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pc",null);//加载每日必应一图，如果初始化没有图片则执行loadBIngPic；若有，则刷新
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        if (musicData.equals("null")) {//说明是第一次打开APP，未进行扫描
            scanLay.setVisibility(View.GONE);
            initMusic();
        } else {
            scanLay.setVisibility(View.VISIBLE);
        }

    }

    private void permissionRequest() {//使用这个框架需要制定JDK版本，建议用1.8
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(granted -> {
                    if (granted) {//请求成功之后开始扫描
                        initMusic();
                    } else {//失败时给一个提示
                        ToastUtils.showShortToast(MainActivity.this, "未授权");
                    }
                });
    }

    //获取音乐列表
    private void initMusic() {
        mList = new ArrayList<>();//实例化
        musicPlayer.setVisibility(View.GONE);
        scanLay.setVisibility(View.VISIBLE);
        back_btn.setVisibility(View.VISIBLE);
//        musicPlayOrPause.setVisibility(View.GONE);
        //数据赋值
        mList = MusicUtils.getMusicData(this);//将扫描到的音乐赋值给音乐列表
        if (!ObjectUtils.isEmpty(mList) && mList != null) {
            scanLay.setVisibility(View.GONE);
            SPUtils.putString(Constant.MUSIC_DATA_FIRST, "null", this);
        }

        adapter = new ChooseMusicListAdapter(R.layout.choose_music,mList);
        mAdapter = new MusicListAdapter(R.layout.item_music_rv_list, mList);//指定适配器的布局和数据源
        //线性布局管理器，可以设置横向还是纵向，RecyclerView默认是纵向的，所以不用处理,如果不需要设置方向，代码还可以更加的精简如下
        rvMusic.setLayoutManager(new LinearLayoutManager(this));
//        listView.setLayoutManager(new LinearLayoutManager(this));
        //如果需要设置方向显示，则将下面代码注释去掉即可
//        LinearLayoutManager manager = new LinearLayoutManager(this);
//        manager.setOrientation(RecyclerView.HORIZONTAL);
//        rvMusic.setLayoutManager(manager);

        //设置适配器
        rvMusic.setAdapter(mAdapter);

        /**
         * 1.与MusicListActivity进行通信
         * 2.调用该碎片中的initMusic方法，实现在播放队列中的点击事件 */
        MusicListActivity musicListActivity = (MusicListActivity) getFragmentManager().findFragmentById(R.id.choose_music_fragment);
        musicListActivity.initMusic();

        //item的点击事件
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                /**
                 * 点击歌曲进行切歌、播放、暂停的操作
                 * */
                if (view.getId() == R.id.item_music) {
//                    mCurrentPosition = position;
//                    changeMusic(mCurrentPosition);

                    if (playMusic == false && firstClick == true){//初始化app，点击任意一首歌曲的操作
                        mCurrentPosition = position;
                        changeMusic(mCurrentPosition);
                        musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        musicSeekBarControl.setVisibility(View.VISIBLE);
                        playMusic = true;
                        firstClick = false;
                        i = 1;
                    }
                    else if (playMusic == true && firstClick == false && mCurrentPosition == position){//音乐播放时，点击相同歌曲的操作
                        mediaPlayer.pause();
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
                        musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
                        playMusic = false;
                    }
                    else if (playMusic == false && firstClick == false && mCurrentPosition == position){//音乐暂停时，点击相同歌曲的操作
                        mediaPlayer.start();
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        playMusic = true;
                    }
                    else if (playMusic == true && firstClick == false && mCurrentPosition != position){//音乐播放时，点击不同于当前歌曲的操作
                        mCurrentPosition = position;
                        changeMusic(mCurrentPosition);
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        playMusic = true;
                    }
                    else if (playMusic == false && firstClick == false && mCurrentPosition != position){//音乐暂停时，点击不同于当前歌曲的操作
                        mCurrentPosition = position;
                        changeMusic(mCurrentPosition);
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        playMusic = true;
                    }
                }
            }
        });
        //设置背景样式
        initStyle();

    }
    private void initStyle() {
//        tvPlaySongInfo.setSelected(true);//跑马灯效果
//        playStateLay.setVisibility(View.VISIBLE);
        musicInfo.setVisibility(View.VISIBLE);

        chooseMusic.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//关闭手势滑动

//        musicAlbumArt.setBackground(getResources().getDrawable(R.mipmap.img_main_bg_1));
        Glide.with(this).load(R.mipmap.icon_empty).into(albumImg);//使用Glide动态加载初始化专辑图片

//        playStateImg.setBackground(getResources().getDrawable(R.mipmap.icon_pause));
//        toolbar.setBackgroundColor(getResources().getColor(R.color.half_transparent));//toolbar背景变透明
        tvTitle.setTextColor(getResources().getColor(R.color.white));//文字变白色
//        tvClearList.setTextColor(getResources().getColor(R.color.white));
        StatusBarUtil.transparencyBar(this);
    }

    /**
     * 连接服务*/
//    private ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            musicBinder = (MusicService.MusicBinder) service;
//            musicService = musicBinder.getService();
//            Log.d(TAG,"service与activity已连接");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            musicBinder = null;
//        }
//    };



    @OnClick({R.id.change_background,R.id.btn_scan,  R.id.btn_play_or_pause,R.id.back_btn,R.id.list_title,
            R.id.selected_music_list,R.id.btn_close,R.id.play_album_img,R.id.show_music_info,
            R.id.music_btn_back,R.id.music_btn_previous,R.id.music_btn_play_or_pause,R.id.music_btn_next,R.id.music_selected_player_list,
            R.id.player_music_model})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.list_title:
                chooseMusic.closeDrawers();

//            case R.id.tv_clear_list: //清空数据
//                mList.clear();
//                mAdapter.notifyDataSetChanged();
//                SPUtils.putString(Constant.MUSIC_DATA_FIRST, "yes", this);
//                scanLay.setVisibility(View.VISIBLE);
//                toolbar.setBackgroundColor(getResources().getColor(R.color.white));
//                StatusBarUtil.StatusBarLightMode(this);
//                tvTitle.setTextColor(getResources().getColor(R.color.black));
//                tvClearList.setTextColor(getResources().getColor(R.color.black));
//                /**
//                 * 监听进度条*/
//                if (mediaPlayer == null) {
//                    mediaPlayer = new MediaPlayer();
//                    mediaPlayer.setOnCompletionListener(this);//监听音乐播放完毕事件，自动下一曲
//                }
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    mediaPlayer.reset();
//                }
                break;
            case R.id.change_background:
                btnClose.setVisibility(View.VISIBLE);
                /**
                 * 这一段if（i == 1）由于逻辑修改不过来，所以只能设置当更换背景时暂停播放 (已解决)*/
                if (i == 1) {       //播放中更换背景
                    if (changeBackground == true && mediaPlayer.isPlaying()) {
//                        mediaPlayer.start();
//                        tvPlaySongInfo.setSelected(true);
                        mainLay.setBackground(getResources().getDrawable(R.mipmap.img_main_bg));
//                        playStateImg.setBackground(getResources().getDrawable(R.mipmap.icon_play));
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        changeBackground = false;
                    }else if (changeBackground == false && mediaPlayer.isPlaying()){
//                        mediaPlayer.start();
//                        tvPlaySongInfo.setSelected(true);
                        mainLay.setBackground(getResources().getDrawable(R.mipmap.img_main_bg_1));
//                        playStateImg.setBackground(getResources().getDrawable(R.mipmap.icon_play));
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        changeBackground = true;
                    }else if (changeBackground == true && playMusic == false){
                        mainLay.setBackground(getResources().getDrawable(R.mipmap.img_main_bg));
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
                        changeBackground = false;
                    }else if (changeBackground == false && playMusic == false){
                        mainLay.setBackground(getResources().getDrawable(R.mipmap.img_main_bg_1));
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
                    }
                }else {     //初始化app时更换背景
                    if (changeBackground == true) {
                        mainLay.setBackground(getResources().getDrawable(R.mipmap.img_main_bg));
                        changeBackground = false;
                    } else {
                        mainLay.setBackground(getResources().getDrawable(R.mipmap.img_main_bg_1));
                        changeBackground = true;
                    }
                }
                break;
            case R.id.btn_scan://扫描本地歌曲
                permissionRequest();
//                back_btn.setVisibility(View.GONE);
                break;
            case R.id.back_btn:
//                ActivityCollector.finishAll();
//                Intent intent = getIntent();
//                boolean isFirstInto = intent.getBooleanExtra("extra_FirstInto",true);
//                Log.d("MainActivity", String.valueOf(isFirstInto));

                scanLay.setVisibility(View.VISIBLE);
                back_btn.setVisibility(View.GONE);
                break;
//            case R.id.btn_previous://上一曲
//                changeMusic(--mCurrentPosition);//当前歌曲位置减1
//                break;
            case R.id.btn_play_or_pause://播放或者暂停
                // 首次点击播放按钮，默认播放第0首，下标从0开始
                if (mediaPlayer == null) {
//                    tvPlaySongInfo.setSelected(true);//跑马灯效果
//                    playStateLay.setVisibility(View.VISIBLE);
                    musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                    changeMusic(0);
                    musicSeekBarControl.setVisibility(View.VISIBLE);
                    i = 1;
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
                        musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
                        playMusic = false;
                    } else {
                        mediaPlayer.start();
//                        playStateLay.setVisibility(View.VISIBLE);
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
//                        playStateImg.setBackground(getResources().getDrawable(R.mipmap.icon_play));
                        musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        playMusic = true;
                    }
                }
                break;
            case R.id.selected_music_list:
                chooseMusic.openDrawer(GravityCompat.START);//打开drawlayout滑动菜单
                break;
            case R.id.btn_close:
                chooseMusic.closeDrawers();
                break;
            case R.id.play_album_img:
                musicPlayer.setVisibility(View.VISIBLE);
                break;
            case R.id.show_music_info:
                musicPlayer.setVisibility(View.VISIBLE);
                break;
                /**
                * 这里开始都是musicPlayer界面的逻辑操作*/
            case R.id.music_btn_back:
                musicPlayer.setVisibility(View.GONE);
                break;
            case R.id.music_btn_previous:
                musicSeekBarControl.setVisibility(View.VISIBLE);
                if (playModel == PLAY_IN_ORDER){
                    changeMusic(--mCurrentPosition);
                }else if (playModel == PLAY_RANDOM){
                    mCurrentPosition = (mCurrentPosition + (int)(1+Math.random()*(20-1+1)))%11;
                    changeMusic(mCurrentPosition);
                }else if (playModel == PLAY_SINGLE){
                    changeMusic(mCurrentPosition);
                }
                break;
            case R.id.music_btn_play_or_pause:
                // 首次点击播放按钮，默认播放第0首，下标从0开始
                if (mediaPlayer == null) {
                    changeMusic(0);
                    musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                    musicSeekBarControl.setVisibility(View.VISIBLE);
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
//                        playStateImg.setBackground(getResources().getDrawable(R.mipmap.icon_pause));
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
                        musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
                        playMusic = false;
                    } else {
                        mediaPlayer.start();
//                        playStateImg.setBackground(getResources().getDrawable(R.mipmap.icon_play));
                        btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
                        playMusic = true;
                    }
                }
                break;
            case R.id.music_btn_next:
                musicSeekBarControl.setVisibility(View.VISIBLE);
                if (playModel == PLAY_IN_ORDER){
                    changeMusic(++mCurrentPosition);
                }else if (playModel == PLAY_RANDOM){
                    mCurrentPosition = (mCurrentPosition + (int)(1+Math.random()*(20-1+1)))%12;
                    changeMusic(mCurrentPosition);
                }else if (playModel == PLAY_SINGLE){
                    changeMusic(mCurrentPosition);
                }
                break;
            case R.id.music_selected_player_list:
                chooseMusic.openDrawer(GravityCompat.START);//打开drawlayout滑动菜单
                break;
                /**
                 * 播放模式*/
            case R.id.player_music_model:
                playModel = (playModel+1)%3;
                if (playModel == PLAY_IN_ORDER){
                    playModel = 0;
                    playerMusicModel.setBackgroundResource(R.mipmap.music_order);
                }else if (playModel == PLAY_RANDOM){
                    playModel = 1;
                    playerMusicModel.setBackgroundResource(R.mipmap.musci_random);
                }else if (playModel == PLAY_SINGLE){
                    playModel = 2;
                    playerMusicModel.setBackgroundResource(R.mipmap.music_single);
                }
        }
    }

    /**
     * 歌曲切歌操作*/
    public void changeMusic(int position) {

        Log.e("MainActivity", "position:" + position);
        if (position < 0) {
            mCurrentPosition = position = mList.size() - 1;
            Log.e("MainActivity", "mList.size:" + mList.size());
        } else if (position > mList.size() - 1) {
            mCurrentPosition = position = 0;
        }
        Log.e("MainActivity", "position:" + position);
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);//监听音乐播放完毕事件，自动下一曲
        }

        try {
            // 切歌之前先重置，释放掉之前的资源
            mediaPlayer.reset();
            // 设置播放源
            Log.d("Music", mList.get(position).path);
            mediaPlayer.setDataSource(mList.get(position).path);

//            tvPlaySongInfo.setText("歌名： " + mList.get(position).song + "   歌手： " + mList.get(position).singer);
            musicInfo.setText(mList.get(position).song + " - " +mList.get(position).singer);
            musicSongName.setText(mList.get(position).song);
            musicSingerName.setText(mList.get(position).singer);

            //使用ImageBitmap来加载专辑图片
            albumImg.setImageBitmap(MusicUtils.getAlbumPicture(MyApplication.getContext(),mList.get(position).getPath()));
            musicAlbumArt.setImageBitmap(MusicUtils.getAlbumPicture(MyApplication.getContext(),mList.get(position).getPath()));

            // 开始播放前的准备工作，加载多媒体资源，获取相关信息
            mediaPlayer.prepare();
            // 开始播放
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        musicTimeSeekBar.setProgress(0);
        musicTimeSeekBar.setMax(mediaPlayer.getDuration());
        musicTotalTime.setText(parseTime(mediaPlayer.getDuration()));

        updateProgress();
        if (mediaPlayer.isPlaying()) {
            musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
            btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
//            playStateImg.setBackground(getResources().getDrawable(R.mipmap.icon_play));
        } else {
            musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
            btnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
//            playStateImg.setBackground(getResources().getDrawable(R.mipmap.icon_pause));
        }
    }

    private void updateProgress() {
        // 使用Handler每间隔1s发送一次空消息，通知进度条更新
        Message msg = Message.obtain();// 获取一个现成的消息
        // 使用MediaPlayer获取当前播放时间除以总时间的进度
        int progress = mediaPlayer.getCurrentPosition();
        msg.arg1 = progress;
        mHandler.sendMessageDelayed(msg, INTERNAL_TIME);
    }

    //滑动条监听
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_pause));
        }

        // 当手停止拖拽进度条时执行该方法
        // 获取拖拽进度
        // 将进度对应设置给MediaPlayer
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            mediaPlayer.seekTo(progress);
            if (playMusic == true){
                mediaPlayer.start();
            }else {
                mediaPlayer.start();
                musicBtnPlayOrPause.setBackground(getResources().getDrawable(R.mipmap.music_play));
            }
        }
    };

    /**
     * 加载天气App背景图片，该背景图片每日都会自动更换 */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
    /**
     * 歌曲播放完成后根据当前播放模式自动播放下一首
     * */
    @Override
    public void onCompletion(MediaPlayer mp)     {
        if (playModel == PLAY_IN_ORDER){
            changeMusic(++mCurrentPosition);
        }else if (playModel == PLAY_RANDOM){
            mCurrentPosition = (mCurrentPosition + (int)(1+Math.random()*(20-1+1)))%13;
            changeMusic(mCurrentPosition);
        }else if (playModel == PLAY_SINGLE){
            changeMusic(mCurrentPosition);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
//        unbindService(connection);
        ActivityCollector.removeActivity(this);
//        System.exit(0);
    }
}
