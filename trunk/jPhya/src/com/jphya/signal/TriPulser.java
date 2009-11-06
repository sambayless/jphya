/*
Copyright (c) 2009, Sam Bayless
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

import com.jphya.scene.Scene;

public class TriPulser {

	protected int m_widthSamps;
	protected float m_widthSecs;
	protected float m_amp;
	protected boolean m_beenHit;
	protected boolean m_isQuiet;

	protected Block m_output;

	public boolean isQuiet() {
		return m_isQuiet;
	};

	public void setOutput(Block output) {
		m_output = output;
	};

	public Block getOutput() {
		return m_output;
	};

	private final Scene scene;
	public TriPulser(Scene scene) {
		this.scene =scene;
		m_beenHit = false;
		m_widthSecs = 0.001f; // Sensible default width 1 millisecond.
		m_widthSamps = (int) (m_widthSecs * (scene.getFPS()));
		if (m_widthSamps > scene.getNFrames()) {
			m_widthSamps =scene.getNFrames();
			m_widthSecs = ((float) m_widthSamps) / scene.getFPS();
		}
	}

	public int setWidthSeconds(float width) {
		m_widthSecs = width;
		m_widthSamps = (int) (width * scene.getFPS());
		if (m_widthSamps == 0)
			m_widthSamps = 2;
		if (m_widthSamps > scene.getNFrames()) {
			m_widthSamps = scene.getNFrames() - 1;
			return -1;
		}
		return 0;
	}

	public int hit(float a) {
		if (m_beenHit)
			return -1; // Otherwize quieter hits could kill the initial big hit.

		m_amp = a;
		m_beenHit = true;

		return 0;
	}

	public Block tick() {
		if (!m_beenHit) {
			m_output.zero(); // Should have a zero flag.
			return m_output;
		}

		FloatBuffer out = m_output.getStart();
		m_beenHit = false;
		m_isQuiet = false;

		// The peak value attained is adjusted to make different
		// widths sound similar in loudness.

		int freeSamps =scene.getNFrames() - m_widthSamps;
		assert (freeSamps >= 0);

		float pulseSamp = 0;
		float rate;
		int t = 0;
		int i = 0;

		// Randomizing the onset of the pulse prevents 'machine gun' - like
		// effects
		// when many pulses are supposed to be occuring randomly.
		// Need to add random predelay of length order main-loop tick,
		// ie several audio-loop ticks.

		// if (freeSamps>0)
		// {
		// t = paRnd(0, freeSamps);
		// while(i<t) { // Zero
		// out[i] = 0;
		// i++;
		// }
		// }

		t += m_widthSamps / 2f;
		rate = m_amp / (m_widthSecs * t);

		while (i < t) { // Up
			out.put(i,pulseSamp);
			pulseSamp += rate;
			i++;
		}

		t += m_widthSamps / 2f;
		while (i < t) { // Down
			out.put(i,pulseSamp);
			pulseSamp -= rate;
			i++;
		}

		while (i < scene.getNFrames()) { // Zero
			out.put(i,0f);
			i++;
		}

		// If a pulse could cross several blocks then
		// m_isQuiet would be false across blocks.

		m_isQuiet = true;

		return m_output;

	}

	// Not finished- will allow impacts asynchronously across block boundaries.
	// Not very useful because block size is usually large enough for a complete
	// hit anyway.
	/*
	 * int i; if (rate == 0) return nullOutput; int sampsToTarget =
	 * (target-out)/rate;
	 * 
	 * if (sampsToTarget > nBlockSamples) sampsToTarget = nBlockSamples;
	 * 
	 * for(i=0; i<sampsToTarget; i++) { out+= rate; output[i] = out; }
	 * 
	 * if (i == nBlockSamples) {
	 * 
	 * 
	 * return output; }
	 */

}
