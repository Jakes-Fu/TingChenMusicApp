package com.llw.music;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import android.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.llw.music.adapter.ChooseMusicListAdapter;
import com.llw.music.model.Song;
import com.llw.music.service.MyApplication;
import com.llw.music.utils.MusicUtils;

import java.util.ArrayList;
import java.util.List;


public class MusicListActivity extends Fragment implements MediaPlayer.OnCompletionListener {

    public static final String TAG = "LeftFragment";
    private static final int INTERNAL_TIME = 1000;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    public ChooseMusicListAdapter Adapter;//歌曲适配器
    private List<Song> mList;
    private RecyclerView listView;
    private LinearLayout musicList;
    private TextView titleView;
    private LinearLayout chooseMusicList;
    private DrawerLayout chooseMusic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_music_item, container, false);
        listView = (RecyclerView) view.findViewById(R.id.list_view);
        musicList = (LinearLayout) view.findViewById(R.id.music_list_item);
        chooseMusicList = (LinearLayout) view.findViewById(R.id.choose_music_list);
        titleView = (TextView) view.findViewById(R.id.list_title);
        chooseMusic = (DrawerLayout) view.findViewById(R.id.drawer_layout);

        initMusicList();
        mList = new ArrayList<>();//实例化
        //数据赋值
        mList = MusicUtils.getMusicData(MyApplication.getContext());//将扫描到的音乐赋值给播放列表
        Adapter = new ChooseMusicListAdapter(R.layout.choose_music, mList);//指定适配器的布局和数据源
        listView.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        listView.setAdapter(Adapter);

        return view;
    }

    public void initMusicList(){
        mList = new ArrayList<>();//实例化
        //数据赋值
        mList = MusicUtils.getMusicData(MyApplication.getContext());//将扫描到的音乐赋值给播放列表
        Adapter = new ChooseMusicListAdapter(R.layout.choose_music, mList);//指定适配器的布局和数据源
        listView.setLayoutManager(new LinearLayoutManager(MyApplication.getContext()));
        listView.setAdapter(Adapter);

        MainActivity mainActivity = (MainActivity) getActivity();
        Adapter.setItemSelectedCallBack(new ChooseMusicListAdapter.ItemSelectedCallBack() {
            @Override
            public void convert(BaseViewHolder holder, int position) {
                TextView list_item_play_or_pause = holder.getView(R.id.list_item_play_or_pause);
                if (mainActivity.changeTextColor == true){
                    if (mainActivity.mCurrentPosition == position && mainActivity.playMusic == true){
                        list_item_play_or_pause.setText("播放中");
                    }else if (mainActivity.mCurrentPosition == position && mainActivity.playMusic == false){
                        list_item_play_or_pause.setText("暂停");
                    }else {
                        list_item_play_or_pause.setText("");
                    }
                }else {
                    list_item_play_or_pause.setText("");
                }
            }
        });

        /**
         * 1.与mainActivity进行通信
         * 2.获取播放位置，调用changeMusic（）方法
         * 3.逻辑操作与mainActivity中点击歌曲的方法相同
         * */

        Adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.music_list_item){
                    mainActivity.changeTextColor = true;
                    mainActivity.mAdapter.notifyDataSetChanged();
                    Adapter.notifyDataSetChanged();
                    mainActivity.musicSeekBarControl.setVisibility(View.VISIBLE);
                    if (mainActivity.playMusic == false && mainActivity.firstClick == true){
                        mainActivity.mCurrentPosition = position;
                        mainActivity.changeMusic(mainActivity.mCurrentPosition);
                        mainActivity.musicBtnPlayOrPause.setBackgroundResource(R.mipmap.music_play);
                        mainActivity.btnPlayOrPause.setBackgroundResource(R.mipmap.music_play);
                        mainActivity.playMusic = true;
                        mainActivity.firstClick = false;
                        mainActivity.i = 1;
                    }else if (mainActivity.playMusic == true && mainActivity.firstClick == false
                            && mainActivity.mCurrentPosition == position){
                        mainActivity.mediaPlayer.pause();
                        mainActivity.musicBtnPlayOrPause.setBackgroundResource(R.mipmap.music_pause);
                        mainActivity.btnPlayOrPause.setBackgroundResource(R.mipmap.music_pause);
                        mainActivity.playMusic = false;
                    }else if (mainActivity.playMusic == false && mainActivity.firstClick == false
                            && mainActivity.mCurrentPosition == position){
                        mainActivity.mediaPlayer.start();
                        mainActivity.musicBtnPlayOrPause.setBackgroundResource(R.mipmap.music_play);
                        mainActivity.btnPlayOrPause.setBackgroundResource(R.mipmap.music_play);
                        mainActivity.playMusic = true;
                    }else if (mainActivity.playMusic == true && mainActivity.firstClick == false
                            && mainActivity.mCurrentPosition != position){
                        mainActivity.mCurrentPosition = position;
                        mainActivity.changeMusic(mainActivity.mCurrentPosition);
                        mainActivity.musicBtnPlayOrPause.setBackgroundResource(R.mipmap.music_play);
                        mainActivity.btnPlayOrPause.setBackgroundResource(R.mipmap.music_play);
                        mainActivity.playMusic = true;
                    }else if (mainActivity.playMusic == false && mainActivity.firstClick == false
                            && mainActivity.mCurrentPosition != position){
                        mainActivity.mCurrentPosition = position;
                        mainActivity.changeMusic(mainActivity.mCurrentPosition);
                        mainActivity.musicBtnPlayOrPause.setBackgroundResource(R.mipmap.music_play);
                        mainActivity.btnPlayOrPause.setBackgroundResource(R.mipmap.music_play);
                        mainActivity.playMusic = true;
                    }

                }
            }
        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }
}
