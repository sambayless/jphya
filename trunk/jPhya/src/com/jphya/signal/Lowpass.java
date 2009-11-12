/*
Copyright (C) 2001, Dylan Menzies
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

package com.jphya.signal;

import java.nio.FloatBuffer;

public class Lowpass {

	private final float twoPiDivFrameRate = (float) 2.0f * 3.142659f;/// ((float) Scene.nFramesPerSecond)

	private static final float DENORMALISATION_EPSILON = 1e-20f;

	private Block m_input;
	private Block m_output;

	// Buffered 'set' variables, for thread-safe operation, and efficiency.
	private float m_gain; // Gain at 0Hz.
	private float m_newGain; // If gain changes, must interpolate to this value
								// to avoid artifacts.
	private float m_pole; // Direct pole coefficient control.
	private float m_cutoffFreq; // More convenient.

	// Used to signal coefficient update at start of next block.
	private boolean m_updateCutoffFreq;

	private float m_b; // Filter coefficients.
	private float m_y; // Sample output in the last frame.

	public void setInput(Block input) {
		m_input = input;
	};

	public void setOutput(Block output) {
		m_output = output;
	}; // Output can be set to input.

	public void setPole(float pole) {
	
		m_b = pole;//this might be wrong
	}

	public void setGain(float gain) {
		m_newGain = gain;
	};

	public void reset() {
		m_gain = 0.0f;
	};

	// Set -3dB cutoff frequency.
	public void setCutoffFreq(float f) {
		m_cutoffFreq = f;
		m_updateCutoffFreq = true;
	}

	public Block tick(Block input, Block output) {
		m_input = input;
		m_output = output;
		return tick();
	}

	public Block getOutput() {
		return m_output;
	};

	public Lowpass() {
		m_input = null;
		m_output = null;

		// Initially no filtering.
		m_gain = 0.0f;
		m_b = 0.0f;
		m_y = 0.0f;

		m_updateCutoffFreq = false;
	}

	public Block tick() {

		// Catch control variable changes.
		// NB need event list to reproduce changes in the correct order.

		// This cutoff-to-pole calc comes from Csound. It looks a bit dodgey
		// because aliased
		// cutoffs make the pole wrap around: It should be meaningful to make
		// the pole shrink
		// to zero so that the filter becomes a bypass in the limit of high
		// cutoff.

		if (m_updateCutoffFreq) {
			float f = m_cutoffFreq * twoPiDivFrameRate/m_output.getScene().getFPS();

			// Make aliased cutoffs stick to upper bound:
			if (f > 3.142659f)
				f = 3.142659f;

			float t = 2.0f - (float) Math.cos(f); // ! use approximation.
			m_b = 1f - (1f - (t - (float) Math.sqrt(t * t - 1f))) * 1.2071f; // Warped
																			// to
																			// reach
																			// full
																			// pole
																			// range
																			// .
		}

		m_updateCutoffFreq = false;
	
		FloatBuffer x = m_input.getStart();
		FloatBuffer y = m_output.getStart();
		// NB x=y is ok, but obviously the contents of x are then lost.

		int i;

		// Combine filter coefficient with amplitude envelope on input:
		float g = m_gain * (1f - m_b);
		float gIncr = (m_newGain - m_gain) / (float) m_output.getNFrames() * (1f - m_b);

		for (i = 0; i <m_output.getNFrames(); i++) {
			y.put(i, g * x.get(i) + m_b * m_y + (float) DENORMALISATION_EPSILON);
			m_y = y.get(i);//what is the value of m_y on the first iteration? should it be zero?
			g += gIncr;
		}

		m_gain = m_newGain;

		return m_output;
	}
}
