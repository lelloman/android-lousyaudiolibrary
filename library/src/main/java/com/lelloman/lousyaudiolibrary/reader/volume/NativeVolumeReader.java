package com.lelloman.lousyaudiolibrary.reader.volume;


import com.lelloman.lousyaudiolibrary.reader.NativeAudioReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NativeVolumeReader extends IVolumeReader{

	public static NativeVolumeReader createSync(final NativeAudioReader audioReader, int... zoomLevels) {
		return new NativeVolumeReader(audioReader, true, zoomLevels);
	}

	public static NativeVolumeReader createAsyn(NativeAudioReader audioReader, int... zoomLevels) {
		return new NativeVolumeReader(audioReader, false, zoomLevels);
	}

	public static NativeVolumeReader fromSerializableBucket(Object serializableBucket){
		try {
			return new NativeVolumeReader(serializableBucket);
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public NativeVolumeReader(Object object){
		super(object);
	}

	protected NativeVolumeReader(IVolumeReader parent, float start, float end) {
		super(parent, start, end);
	}

	protected NativeVolumeReader(final NativeAudioReader audioReader, boolean sync, final int... zoomLevels) {
		super(audioReader, zoomLevels);
		reading = true;

		final VolumeMaker[] makers = new VolumeMaker[zoomLevels.length];
		for (int i = 0; i < zoomLevels.length; i++) {
			int pcmFramesPerVolume = (int) (totalFrames / zoomLevels[i]);
			makers[i] = new VolumeMaker(i, pcmFramesPerVolume, data[i].length);
		}

		if (sync) {
			load(audioReader, makers);
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					load(audioReader, makers);
				}
			}).start();
		}
	}

	private void load(NativeAudioReader audioReader, VolumeMaker[] makers) {

		while (cursor < totalFrames && !audioReader.getSawOutputEOS()) {
			ByteBuffer nextChunk = audioReader.nextNativeChunk();
			for (VolumeMaker maker : makers) {
				maker.nextSample(nextChunk);
			}
			cursor++;
		}

		for (int i = 0; i < zoomLevels.length; i++) {
			VolumeMaker maker = makers[i];
			double[] volume = new double[maker.size];
			maker.nativeData.order(ByteOrder.nativeOrder());
			maker.nativeData.asDoubleBuffer().get(volume);
			data[i] = volume;

		}
		if (listener != null) {
			listener.onFrameReadingEnd();
		}
		reading = false;
		synchronized (children) {
			children.clear();
		}
	}


	@Override
	public IVolumeReader subWindow(float start, float end) {
		IVolumeReader output = new NativeVolumeReader(this, start, end);
		if (reading) {
			synchronized (children) {
				children.add(output);
			}
		}

		return output;
	}

	private native void nextSample(ByteBuffer nativeAttrsDouble, ByteBuffer nativeAttrsInt, ByteBuffer data, int dataSize, ByteBuffer sample, int sampleSize);

	private class VolumeMaker {
		final int pcmFramesPerVolumeFrame;
		final int size;
		final ByteBuffer nativeData;
		final ByteBuffer nativeAttrsDouble;
		final ByteBuffer nativeAttrsInt;

		public VolumeMaker(int index, int pcmFramesPerVolumeFrame, int size) {
			this.pcmFramesPerVolumeFrame = pcmFramesPerVolumeFrame;
			this.size = size;

			int doubleBytes = Double.SIZE / 8;
			nativeData = ByteBuffer.allocateDirect(doubleBytes * size);

			nativeAttrsDouble = ByteBuffer.allocateDirect(Double.SIZE / 8 * 2);
			nativeAttrsDouble.order(ByteOrder.nativeOrder());
			nativeAttrsDouble.asDoubleBuffer().put(new double[]{0, pcmFramesPerVolumeFrame});

			nativeAttrsInt = ByteBuffer.allocateDirect(Integer.SIZE / 8 * 2);
			nativeAttrsInt.order(ByteOrder.nativeOrder());
			nativeAttrsInt.asIntBuffer().put(new int[]{0, 0});
		}

		public void nextSample(ByteBuffer sample) {
			NativeVolumeReader.this.nextSample(nativeAttrsDouble, nativeAttrsInt, nativeData, size, sample, sample.limit());
		}
	}

}
