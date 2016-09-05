package com.lelloman.lousyaudiolibrary.player;

import android.content.Context;

import com.lelloman.lousyaudiolibrary.AudioReader;
import com.lelloman.lousyaudiolibrary.PhaseVocoder;

import java.nio.ByteBuffer;

public class SlowAudioPlayer extends BufferedAudioPlayer {

    protected static final int FRAME_SIZE = 4096*4;
    protected static final int HOP = FRAME_SIZE / 4;
    protected static final float SCALE = .5f;


    private boolean mSlow = false;
    private byte[] mPrevChunk = null;

    private PhaseVocoder mVocoder = null;
    private ByteBuffer mMiniByteBuffer = ByteBuffer.allocate(2);

    public SlowAudioPlayer(EventsListener listener) {
        super(listener);
    }

    @Override
    public boolean init(Context context, int resId) {
        boolean output = super.init(context, resId);
        if(output) initVocoder();
        return output;
    }

    @Override
    public boolean init(String src) {
        boolean output = super.init(src);
        if(output) initVocoder();
        return output;
    }

    @Override
    public boolean init(AudioReader reader) {
        boolean output = super.init(reader);
        if(output) initVocoder();
        return output;
    }

    private void initVocoder(){
        if(reader == null) return;
        mVocoder = new PhaseVocoder(reader,SCALE,FRAME_SIZE,HOP);
    }

    @Override
    protected int fillBuffer() {

        byte[] theChunk;
        if(mSlow){
            double[] chunk = mVocoder.next();
            theChunk = new byte[chunk.length*2];
            for(int i=0;i<chunk.length;i++) {
                short x = (short) (chunk[i] * Short.MAX_VALUE);
                mMiniByteBuffer.position(0);
                mMiniByteBuffer.putShort(x);
                byte[]arr = mMiniByteBuffer.array();
                int i2 = i*2;
                theChunk[i2+1] = arr[0];
                theChunk[i2] = arr[1];
            }

        }else {
            byte[] chunk = reader.nextChunk();

            if (chunk == null) return -1;
            if (chunk.length == 0) return -1;

            byte[] chunkClone = chunk.clone();
            theChunk = chunkClone;
            mPrevChunk = chunkClone;
        }

        synchronized (buffer){
            //debugga("feeder feeds chunk");
            buffer.add(theChunk);
        }

        return 0;
    }

    public void setSlowScale(double scale){
        mSlow = scale < .99;
        this.mVocoder.setScale(scale);
    }

    public boolean isSlow(){
        return mSlow;
    }
}
