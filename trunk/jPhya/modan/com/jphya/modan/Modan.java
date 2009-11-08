/*
Copyright (C) 2001-Present Dylan Menzies
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
   this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation and/or 
   other materials provided with the distribution.
 * Neither the name of 'JPhya' nor the names of its contributors may be used 
   to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jphya.modan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.math.MathException;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

/**
 * 
 * Modal Analysis command line function. Simplified Macauly-Quaterieri analysis:
 * modes assumed to stay within a single bin. Final log amp evolution
 * approximated by straight lines. Suitable for extracting modes from the
 * impulse response of hard objects. Note that objects such as wood exhibit a
 * notable amount of time-domain impulse response which is not well described
 * exclusively with modes.
 * 
 */
public class Modan {

	private final static int DEFAULT_N_SAMPLES_IN_FFT_WINDOW = 4096;
	private final static float DEFAULT_FRACTIONAL_WINDOW_GAP = 0.1f;
	private final static int DEFAULT_NOISE_FLOOR = 3; // 4
	private final static int DEFAULT_MIN_PEAKS = 5; // 3
	private final static float PULSE_THRESHOLD_AMP = 4000; // 3

	// Scheme:
	// 
	// load wav file.
	// Create space for bin amplitudes for each time frame.
	// Scan wav and fill bins.
	// Scan bins for peaks, increment peak counts.
	// Look at significant bin counts and do least-squares to determine amp
	// coupling and damping.

	private static short[] audioData;
	private static AudioFormat format;
	private static int frameSize = 0;

	public static void readWav(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
		AudioInputStream audio = AudioSystem.getAudioInputStream(inputStream);
		format = audio.getFormat();
		if (format.getChannels() != 1)
			throw new RuntimeException("Only supports mono track wav files");
		if (format.getFrameSize() != 2)
			throw new RuntimeException("Only supports 16 bit/sample wav files");
		audioData = new short[(int) audio.getFrameLength()];
		frameSize = (int) audio.getFrameLength();
		// ByteBuffer dataBuf =
		// ByteBuffer.allocateDirect((int)audio.getFrameLength()*format.getFrameSize()).order(ByteOrder.LITTLE_ENDIAN);//wav
		// is always little endian
		// dataBuf.limit();

		// assume 16 bit signed data...
		// audio.r
		byte[] b = new byte[(int) audio.getFrameLength() * format.getFrameSize()];
		int read = audio.read(b);
		ByteBuffer dataBuf = ByteBuffer.wrap(b, 0, read).order(ByteOrder.LITTLE_ENDIAN);

		dataBuf.asShortBuffer().get(audioData);

	}

	public static void main(String[] args) throws IllegalArgumentException, MathException, FileNotFoundException, UnsupportedAudioFileException, IOException {

		int nTimeSamplesPerWindow;
		int nFreqSamplesPerWindow;
		int nTimeSamplesBetweenWindows;

		System.out.println("MODAN modal analyser, (C) 2001-present Dylan Menzies. (Adapted for JPhya 2009)\n");

		if (args.length < 1) {
			System.out.printf("\nusage : \n modan <wav file> (<block size> <block spacing> <noise floor> <min num peaks>)\n\n");
			System.out.printf("<wav file> a 16 bit mono wave file containing a smooth section of a recording of a hit resonant object eg bell.\n");
			System.out.printf("<block size> in samples, default %d, (will be rounded to nearest power of two)>\n", DEFAULT_N_SAMPLES_IN_FFT_WINDOW);
			System.out.printf("<block spacing> as fraction of a block, default %f\n", DEFAULT_FRACTIONAL_WINDOW_GAP);
			System.out.printf("<noise floor> spectral peaks below this level are ignored, default %d\n", DEFAULT_NOISE_FLOOR);
			System.out.printf("<min num peaks> for each freq bin, the number of successive peaks required to generate a resonance, default %d\n\n", DEFAULT_MIN_PEAKS);

			System.exit(-1);
		}

		String soundfileName = args[0];
		Scanner scanner = new Scanner(System.in);

		nTimeSamplesPerWindow = DEFAULT_N_SAMPLES_IN_FFT_WINDOW;
		if (args.length > 1) {
			nTimeSamplesPerWindow = Integer.valueOf(args[1]);

		}

		int fft2Power = (int) (Math.log((float) nTimeSamplesPerWindow) / Math.log(2.0) - 1.0 + .5); // +.5
																									// rounds
																									// avoids
																									// numerical
																									// inaccuracy.

		nTimeSamplesPerWindow = (int) Math.pow(2.0, fft2Power) * 2; // Make sure
																	// its a
																	// power of
																	// two!!

		nFreqSamplesPerWindow = nTimeSamplesPerWindow / 2; // Half because phase
															// info discarded.

		nTimeSamplesBetweenWindows = (short) (nTimeSamplesPerWindow * DEFAULT_FRACTIONAL_WINDOW_GAP);
		if (args.length > 2) {
			float f;
			f = Float.valueOf(args[2]);
			// sscanf( args[3], "%f", &f );
			nTimeSamplesBetweenWindows = (short) (f * nTimeSamplesPerWindow);
		}

		float noiseFloor = DEFAULT_NOISE_FLOOR;
		if (args.length > 3) {
			float f;
			f = Float.valueOf(args[3]);
			noiseFloor = f;
		}

		int minPeaks = DEFAULT_MIN_PEAKS;
		if (args.length > 4) {
			int d;
			d = Integer.valueOf(args[4]);
			minPeaks = d;
		}

		readWav(new BufferedInputStream(new FileInputStream(new File(soundfileName))));

		File output = new File(soundfileName.substring(0, soundfileName.lastIndexOf('.')) + ".md");

		analyze(nTimeSamplesPerWindow, nFreqSamplesPerWindow, fft2Power, noiseFloor, nTimeSamplesBetweenWindows, minPeaks, output);

	}

	private static void analyze(int nTimeSamplesPerWindow, int nFreqSamplesPerWindow, int fft2Power, float noiseFloor, int nTimeSamplesBetweenWindows, int minPeaks, File output)
			throws IllegalArgumentException, MathException, IOException {
		float[] fftFrameBufferReal = new float[nTimeSamplesPerWindow];// (float[])calloc(
																		// nTimeSamplesPerWindow,
																		// sizeof(float)
																		// );
		float[] fftFrameBufferImag = new float[nTimeSamplesPerWindow];// (float[])calloc(
																		// nTimeSamplesPerWindow,
																		// sizeof(float)
																		// );
		float[] window = new float[nTimeSamplesPerWindow];// (float[])malloc(
															// nTimeSamplesPerWindow
															// * sizeof(float)
															// );
		float[] amp = new float[nFreqSamplesPerWindow];// (float[])malloc(
														// nFreqSamplesPerWindow
														// * sizeof(float) );
		// Sum of amps of frames for each bin:
		float[] ampSum = new float[nFreqSamplesPerWindow];// (float[])calloc(
															// nFreqSamplesPerWindow,
															// sizeof(float) );
		// First moment of amps:
		float[] ampMoment = new float[nFreqSamplesPerWindow];// (float[])calloc(
																// nFreqSamplesPerWindow,
																// sizeof(float)
																// );
		// Number of frames overwhich amp statistics have been taken:
		int[] ampNum = new int[nFreqSamplesPerWindow];// (long*)calloc(
														// nFreqSamplesPerWindow,
														// sizeof(long) );
		// Number of peaks found in each bin:
		int[] nPeaks = new int[nFreqSamplesPerWindow];// (long*)calloc(
														// nFreqSamplesPerWindow,
														// sizeof(long) );

		// Result space.

		float[] ampcoupling = new float[nFreqSamplesPerWindow];// (float[])malloc(
																// nFreqSamplesPerWindow
																// *
																// sizeof(float)
																// );
		float[] damping = new float[nFreqSamplesPerWindow];// (float[])malloc(
															// nFreqSamplesPerWindow
															// * sizeof(float)
															// ); // Sum of amps
															// of frames for
															// each bin.
		float[] freq = new float[nFreqSamplesPerWindow];// (float[])malloc(
														// nFreqSamplesPerWindow
														// * sizeof(float) ); //
														// First moment of amps.

		// Create window to be applied before FFT.

		float f = 2.0f * 3.142659f / (nTimeSamplesPerWindow - 1);
		for (int n = 0; n < nTimeSamplesPerWindow; n++) { // Hanning.
			window[n] = .5f * (1.0f - (float) Math.cos(f * n));
			// Hamming.
			// window[n] = .54-.46*cos(f*n);
		}

		// Find impulse start.

		short[] soundStart = audioData;
		int pulseStart = 0;

		// while( soundStart[(pulseStart++)] < PULSE_THRESHOLD_AMP );
		// ! while( (pulseStart++ - soundStart) < 5000 );

		int framePos = 0; // Cursor for working frame.
		int maxFramePos = 0 + frameSize - nTimeSamplesPerWindow;

		int nFrame = 0; // Count the number of frames.

		int samplePos;

		if (framePos > maxFramePos) {
			System.err.printf("Audio too short for window size.\n");
			System.exit(-1);
		}

		// //// Main scan.

		// For each frame:
		// 1. Calculate the FFT.
		// 2. Update statistics for least square fitting of modes identified
		// later.
		// 3. Find peaks and update total peak count.

		while (framePos < maxFramePos) {

			samplePos = framePos;

			// Copy wav data into FFT buffer.
			for (int n = 0; n < nTimeSamplesPerWindow; n++) {
				fftFrameBufferReal[n] = window[n] * ((float) soundStart[samplePos + n]);
			}

			fft2(fftFrameBufferReal, fftFrameBufferImag, fft2Power, (float) 1.0, (int) 0);

			// Calc log of bin magnitudes.

			for (int n = 0; n < nFreqSamplesPerWindow; n++) {
				amp[n] = .5f * (float) Math.log(fftFrameBufferReal[n] * fftFrameBufferReal[n] + fftFrameBufferImag[n] * fftFrameBufferImag[n]);

				if (amp[n] > noiseFloor) { // Above noise floor, so add to
											// statistics.
					ampSum[n] += amp[n];
					ampMoment[n] += nFrame * amp[n];
					ampNum[n]++;
				}

				// //////// Visualization:
				/*
				 * char line[256]; sprintf(line,
				 * "%8d Hz %s",(n*wavConfig.m_nFramesPerSecond
				 * )/nTimeSamplesPerWindow, ADBbar((short)amp[n]));
				 * ADBwriteToFile(line, nFreqSamplesPerWindow);
				 */
			}

			// Find peaks and increment peak counters.
			for (int n = 1; n < nFreqSamplesPerWindow - 1; n++) {
				if (amp[n] > noiseFloor && amp[n - 1] < amp[n] && amp[n] > amp[n + 1])
					nPeaks[n]++;
			}

			framePos += nTimeSamplesBetweenWindows;
			nFrame++;
		}

		float gradient; // Slope of linear fit.
		float offset; // value of fit at n=0.

		// int i;

		/*
		 * FILE* fh = fopen("results", "w");
		 * 
		 * for(i=0; i< nFreqSamplesPerWindow; i++) { gradient = ( ampMoment[i] -
		 * a * ampSum[i] ) * b; offset = ( ampSum[i] - c * gradient ) * d;
		 * fprintf(fh, "%10d %10d %10f %10f %10f\n", i, nPeaks[i], ampSum[i],
		 * offset, gradient); // fprintf(fh, "%s\n", ADBbar((short)ampSum[i]*.1)
		 * ); }
		 */

		int nModesFound = 0;
		float overlapFactor = (float) nTimeSamplesPerWindow / (float) nTimeSamplesBetweenWindows;
		float maxAmpcoupling = 0;
		int maxPeaks;

		// ///// Final Scan

		// 1. Find non-zero peak counts, starting with the biggest.
		// 2. Fit modal data using bin statistics.

		boolean firstPeak = true;
		do {
			maxPeaks = 0;
			int maxPeakIndex = 0;
			for (int i = 0; i < nFreqSamplesPerWindow; i++)
				if (nPeaks[i] > maxPeaks) {
					maxPeaks = nPeaks[i];
					maxPeakIndex = i;
				}
			;

			if (firstPeak) {
				// printf("Max peak count = %d/n", maxPeaks);
				firstPeak = false;
			}

			if (maxPeaks == 0)
				break; // No more peaks left.
			nPeaks[maxPeakIndex] = 0; // Exclude this peak next search.
			int i = maxPeakIndex;

			int n = ampNum[i];
			// ! I worked the coefficients by hand so there could be errors..

			float a = (n - 1) * .5f;
			float b = 12.0f / (n * (n - 1) * (n - 1));
			float c = n * (n - 1) * .5f;
			float d = 1.0f / n;

			gradient = (ampMoment[i] - a * ampSum[i]) * b;
			offset = (ampSum[i] - c * gradient) * d;

			if (gradient < 0.0) {
				freq[nModesFound] = (float) i / nTimeSamplesPerWindow * format.getSampleRate();
				damping[nModesFound] = (float) format.getSampleRate() * (-gradient) / (float) nTimeSamplesBetweenWindows;

				float p = overlapFactor * (-gradient); // Decay amp compensation
														// factor.
				float e = ampcoupling[nModesFound] = (float) (Math.exp(offset) * p / (1 - Math.exp(-p)));
				if (e > maxAmpcoupling)
					maxAmpcoupling = e;
				nModesFound++;
			}

		} while (maxPeaks >= minPeaks);

		// Normalize amplitudes.
		for (int i = 0; i < nModesFound; i++)
			ampcoupling[i] /= maxAmpcoupling;

		// ! Need to add normaliser based on energy response to white noise
		// excitation.
		// Should use Phya dll for the paModalRes.

		// Sort by field given by testFiled(i) macro.

		
		//#define testField(I) (ampcoupling[I])
		/*
				{
					float record;
					int recordIndex;
					int i;
					int j;
					float t;

					for(i=0; i<nModesFound-1; i++) {
						record = ampcoupling[i];
						recordIndex = i;
						for(j=i+1; j<nModesFound; j++) {
							if (ampcoupling[j] > record) {
								record = ampcoupling[j];
								recordIndex = j;
							}
						}
						if (recordIndex > i){
							t = ampcoupling[i];
							ampcoupling[i] = ampcoupling[recordIndex];
							ampcoupling[recordIndex] = t;

							t = damping[i];
							damping[i] = damping[recordIndex];
							damping[recordIndex] = t;

							t = freq[i];
							freq[i] = freq[recordIndex];
							freq[recordIndex] = t;
						}
					}
				}*/
		ArrayList<Mode> modes = new ArrayList<Mode>();
		
		for(int i = 0;i<nModesFound;i++)
		{
			modes.add(new Mode(freq[i], damping[i],ampcoupling[i]));
		}
		Collections.sort(modes);
		
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(output));
		PrintStream printer = new PrintStream(out, true, "Cp1252");
		// Writer writer = new OutputStreamWriter(out);
		printer.print("1.000000	1.000000	1.000000\n");
		// writer.write("1.000000	1.000000	1.000000\n");

		for (Mode mode:modes) {
			printer.printf("%f	%f	%f\n", mode.freq, mode.damping,mode.amplitude);
		}
		printer.close();

		System.out.println("Created modal data file " + output.getName() + " with " + nModesFound + " modes.");
	}

	private static class Mode implements Comparable<Mode> {
		private float amplitude;
		private float freq;
		private float damping;

		private Mode(float freq, float damping, float amplitude) {
			super();
			this.freq = freq;
			this.damping = damping;
			this.amplitude = amplitude;
		}

		@Override
		public String toString() {
			return freq + "\t" + damping + "\t" + amplitude;
		}

		public int compareTo(Mode o) {
			// (invert order)
			return (int) Math.signum(o.amplitude - this.amplitude);
		}

	}

	private static void fft2(float[] fftFrameBufferReal, float[] fftFrameBufferImag, int fft2Power, float f, int i) throws IllegalArgumentException, MathException {

		FastFourierTransformer fft = new FastFourierTransformer();
		double[] data = new double[fftFrameBufferReal.length];
		for (int n = 0; n < fftFrameBufferReal.length; n++)
			data[n] = fftFrameBufferReal[n];

		Complex[] c = fft.inversetransform(data);

		for (int n = 0; n < c.length; n++) {
			fftFrameBufferReal[n] = (float) c[n].getReal();
			fftFrameBufferImag[n] = (float) c[n].getImaginary();
		}

	}
}
