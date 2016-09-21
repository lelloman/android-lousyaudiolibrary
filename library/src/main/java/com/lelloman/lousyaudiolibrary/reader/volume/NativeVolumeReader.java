package com.lelloman.lousyaudiolibrary.reader.volume;


import com.lelloman.lousyaudiolibrary.reader.NativeAudioReader;

public class NativeVolumeReader extends IVolumeReader{

	protected NativeVolumeReader(IVolumeReader parent, float start, float end) {
		super(parent, start, end);
	}

	public NativeVolumeReader(NativeAudioReader audioReader, int...zoomLevels){
		super(audioReader, zoomLevels);
	}

	@Override
	public IVolumeReader subWindow(float start, float end) {
		IVolumeReader output = new NativeVolumeReader(this, start, end);
		if(reading) {
			synchronized (children) {
				children.add(output);
			}
		}

		return output;
	}

	@Override
	public Double getVolume(int zoom, int index) {
		return null;
	}
}
