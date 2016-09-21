package com.lelloman.lousyaudiolibrary.reader.volume;


import com.lelloman.lousyaudiolibrary.reader.NativeAudioReader;

public class NativeVolumeReader extends IVolumeReader{

	public NativeVolumeReader(NativeAudioReader audioReader, int...zoomLevels){
		super(audioReader, zoomLevels);
	}

	@Override
	public IVolumeReader subWindow(float start, float end) {
		return null;
	}

	@Override
	public Double getVolume(int zoom, int index) {
		return null;
	}
}
