package com.lelloman.lousyaudiolibrary;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioReader {

    public static final int CODEC_TIMEOUT_US = 1000;
    public static final int NO_OUTPUT_COUNTER_LIMIT = 10;

    private MediaExtractor extractor;
    private MediaCodec codec;
    private MediaCodec.BufferInfo info;

    private MediaFormat format;
    private String mime;
    private int sampleRate;
    private int channels;
    private long durationUs;
    private long durationMs;
    private int bitRate;

    private double percent = 0;
    private long currentMs = 0;
    private long currentUs = 0;

    private int noOutputCounter = 0;

    private byte[] chunk = null;
    private double[] chunkDouble = null;

    // pre lollipop
    ByteBuffer[] codecInputBuffers;
    ByteBuffer[] codecOutputBuffers;

    // lollipop+
    ByteBuffer inputBuffer;
    ByteBuffer outputBuffer;

    private boolean released = false;

    private boolean sawInputEOS = false;
    private boolean sawOutputEOS = false;

    public AudioReader(String src) throws Exception {
        extractor = new MediaExtractor();
        extractor.setDataSource(src);

        readHeader();
    }

    public AudioReader(Context context, int resId) throws Exception {
        extractor = new MediaExtractor();
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(resId);
        extractor.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getDeclaredLength());
        fd.close();

        readHeader();
    }

    private void readHeader() throws Exception {

        format = extractor.getTrackFormat(0);
        mime = format.getString(MediaFormat.KEY_MIME);
        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

        // if duration is 0, we are probably playing a live stream
        durationUs = format.getLong(MediaFormat.KEY_DURATION);
        durationMs = durationUs / 1000;

        if(!mime.startsWith("audio/")) throw new Exception("AudioReader can only read audio file");

        codec = MediaCodec.createDecoderByType(mime);
        if(codec == null) throw new Exception(String.format("MediaCodec failed to instantiate for mime type <%s>",mime));

        codec.configure(format, null, null, 0);
        codec.start();

        extractor.selectTrack(0);

        info = new MediaCodec.BufferInfo();
        if(Build.VERSION.SDK_INT < 21){
            codecInputBuffers = codec.getInputBuffers();
            codecOutputBuffers = codec.getOutputBuffers();
        }
    }

    public String getMimeType(){
        return mime;
    }

    public byte[] nextChunk(){

        synchronized (this) {
            if (sawOutputEOS || released) return null;

            processInputBuffer();
            noOutputCounter++;
            processOutputBuffer();

            if (noOutputCounter > NO_OUTPUT_COUNTER_LIMIT) sawOutputEOS = true;

        }
        return chunk;
    }

    public double[] nextChunkDouble(){
        byte[] chunk = nextChunk();

        if(chunk == null) return null;

        if(chunkDouble == null || chunkDouble.length != chunk.length/2)
            chunkDouble = new double[chunk.length/2];


        ByteBuffer bb = ByteBuffer.wrap(chunk);
        double shortMax = Short.MAX_VALUE;
        bb.order(ByteOrder.nativeOrder());
        for(int i=0;i<chunkDouble.length;i++)
            chunkDouble[i] = bb.getShort() / shortMax;

        return chunkDouble;
    }

    private void processInputBuffer(){

        if(sawInputEOS) return;

        int inputBufIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_US);
        if(inputBufIndex >= 0){
            if(Build.VERSION.SDK_INT < 21)
                inputBuffer = codecInputBuffers[inputBufIndex];
            else
                inputBuffer = codec.getInputBuffer(inputBufIndex);

            int sampleSize = extractor.readSampleData(inputBuffer,0);
            if(sampleSize < 0){
                sawInputEOS = true;
                sampleSize = 0;
            }else{
                currentUs = extractor.getSampleTime();
                currentMs = currentUs / 1000;
                percent = durationMs == 0 ? 0 : (double) currentUs / durationUs;
            }

            codec.queueInputBuffer(inputBufIndex,0,sampleSize,currentUs,sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);

            if(!sawInputEOS) extractor.advance();
        }
    }

    private void processOutputBuffer(){

        int res = codec.dequeueOutputBuffer(info, CODEC_TIMEOUT_US);

        if (res >= 0) {
            if (info.size > 0)  noOutputCounter = 0;

            int outputBufIndex = res;

            if(Build.VERSION.SDK_INT < 21) {
                outputBuffer = codecOutputBuffers[outputBufIndex];
            }else{
                outputBuffer = codec.getOutputBuffer(outputBufIndex);
            }

            if(chunk == null){
                chunk = new byte[info.size];
            }
            else if(chunk.length != info.size){
                chunk = new byte[info.size];
            }

            outputBuffer.get(chunk);
            outputBuffer.clear();

            /*if(chunk.length == 0)
                chunk = null;*/

            codec.releaseOutputBuffer(outputBufIndex, false);
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                sawOutputEOS = true;
            }
        } else if (Build.VERSION.SDK_INT < 21 && res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            codecOutputBuffers = codec.getOutputBuffers();
        } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat oformat = codec.getOutputFormat();
            // TODO maybe one should do something about it other than logging
        } else {

        }
    }

    public long getDurationMs(){
        return durationMs;
    }

    public long getCurrentMs(){
        return currentMs;
    }

    public double getPercent(){
        return percent;
    }

    public int getSampleRate(){
        return sampleRate;
    }

    public int getChannels(){
        return channels;
    }

    public int getBitRate() {
        return bitRate;
    }

    public boolean getSawOutputEOS(){
        return sawOutputEOS;
    }

    public void reset(){
        codec.flush();
        seek(0);
        sawOutputEOS = false;
        sawInputEOS = false;
    }

    public void seek(long pos){
        extractor.seekTo(pos, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
    }
    public void seek(double percent){
        long pos = (long) (percent * durationUs);
        seek(pos);
    }

    public void release(){

        if (released) return;

        synchronized (this) {
            codec.stop();
            codec.release();
            codec = null;
            released = true;
        }
    }
}
