package com.lelloman.lousyaudiolibrary.algorithm.phasevocoder;



public enum VocoderType {

	JAVA(JavaPhaseVocoder.class, 0),
	NATIVE_OLD(NativePhaseVocoderOld.class, 10),
	NATIVE(NativePhaseVocoder.class, 1),
	NATIVE_MULTITHREAD(NativePhaseVocoderMultiThread.class, 2);

	public final Class vocoderClass;
	public final int index;

	public static final VocoderType[] ALL = new VocoderType[]{
			JAVA, NATIVE_OLD, NATIVE, NATIVE_MULTITHREAD
	};

	VocoderType(Class clazz, int index){
		this.vocoderClass = clazz;
		this.index = index;
	}
}
