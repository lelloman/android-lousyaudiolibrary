package com.lelloman.lousyaudiolibrary.algorithm.phasevocoder;

/**
 * Created by lelloman on 15-9-16.
 */
public interface IPhaseVocoder {
	void setScale(double v);

	double[] next();

	int getH();

	int getN();
}
