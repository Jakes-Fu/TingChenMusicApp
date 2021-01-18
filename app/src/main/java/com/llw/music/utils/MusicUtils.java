package com.llw.music.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.llw.music.R;
import com.llw.music.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐扫描工具类 */
public class MusicUtils {
    private static Context context;

    /**
     * 扫描系统里面的音频文件，返回一个list集合
     */
    public static List<Song> getMusicData(Context context) {
        List<Song> list = new ArrayList<Song>();
        // 媒体库查询语句（写一个工具类MusicUtils）
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));//歌曲名称
                song.singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));//歌手名
                song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));//专辑名
                song.album_art = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST));
                song.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//歌曲路径
                song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));//歌曲时长
                song.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));//歌曲大小

                if (song.size > 1000 * 800) {
                    // 注释部分是切割标题，分离出歌曲名和歌手 （本地媒体库读取的歌曲信息不规范）
                    if (song.song.contains("-")) {
                        String[] str = song.song.split("-");
                        song.singer = str[0];
                        song.song = str[1];
                    }
                    list.add(song);
                }
            }
            // 释放资源
            cursor.close();
        }
        return list;
    }
    /**
     * 定义一个方法用来格式化获取到的时间
     */
    public static String formatTime(int time) {
        if (time / 1000 % 60 < 10) {
            return time / 1000 / 60 + ":0" + time / 1000 % 60;

        } else {
            return time / 1000 / 60 + ":" + time / 1000 % 60;
        }
    }
    /**
     * 获取专辑封面
     * @param context 上下文
     * @param url    歌曲路径
     * @param ivPic 在哪显示该图片,这个放置图片的imageView必须实例化
     */
    public static Bitmap setArtwork(Context context, String url, ImageView ivPic) {
        Uri selectedAudio = Uri.parse(url);
        MediaMetadataRetriever myRetriever = new MediaMetadataRetriever();
        myRetriever.setDataSource(url); // the URI of audio file
        byte[] artwork;
        artwork = myRetriever.getEmbeddedPicture();

        if (artwork != null) {
            Bitmap bMap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
            ivPic.setImageBitmap(bMap);

            return bMap;
        } else {
            ivPic.setImageResource(R.mipmap.icon_empty);
            return BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_empty);
        }
    }

}

