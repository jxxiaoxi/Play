package com.well.wellvideo.codec;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Encoder {
    private static final int TIMEOUT_US = 10000;

    /*
    *
    *  将video中的视频和音频分开
    *
    */

    public void statMediaExtractor(String inputVideoPath) {
        MediaCodec codec = null;
        long timeStart = System.nanoTime();
        long timeFinish = 0;
        ByteBuffer[] codecInputBuffers;
        ByteBuffer[] codecOutputBuffers;

        MediaExtractor extractor = new MediaExtractor();
        File file = new File(inputVideoPath);
        try {
            extractor.setDataSource(inputVideoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int tracks = extractor.getTrackCount();
        extractor.selectTrack(0);

        MediaFormat format = extractor.getTrackFormat(0);
        String mime = format.getString(MediaFormat.KEY_MIME);
        try {
            codec = MediaCodec.createDecoderByType(mime);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (codec != null) {
            codec.configure(format, null, null, 0);
            codec.start();
            codecInputBuffers = codec.getInputBuffers();
            codecOutputBuffers = codec.getOutputBuffers();

            boolean sawInputEOS = false;
            boolean sawOutputEOS = false;

            int inputBufIndex = codec.dequeueInputBuffer(TIMEOUT_US);

            if (inputBufIndex > 0) {
                ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                int sampleSize = extractor.readSampleData(dstBuf, 0);
                long presentationTimeUs = 0;
                if (sampleSize < 0) {
                    sawInputEOS = true;
                    sampleSize = 0;
                } else {
                    presentationTimeUs = extractor.getSampleTime();
                }

                codec.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);

                if (!sawInputEOS) {
                    boolean adv = extractor.advance();
                }
            }
        }
    }
}
