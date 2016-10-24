package com.lelloman.lousyaudiolibrary.algorithm.phasevocoder;



public enum VocoderType {

	JAVA(JavaPhaseVocoder.class, 10),
	HYBRID(NativePhaseVocoderOld.class, 20),
	NATIVE(NativePhaseVocoder.class, 30),
	NATIVE_MULTITHREAD(NativePhaseVocoderMultiThread.class, 40);

	public final Class vocoderClass;
	public final int index;

	public static final VocoderType[] ALL = new VocoderType[]{
			JAVA, HYBRID, NATIVE//, NATIVE_MULTITHREAD
	};

	VocoderType(Class clazz, int index){
		this.vocoderClass = clazz;
		this.index = index;
	}
}
