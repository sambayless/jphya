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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.jphya.scene.Scene;

public class Limiter {

	public static final int DIRECT = 0;
	public static final int ATTACK = 1;
	public static final int HOLD = 2;
	public static final int RELEASE = 3;

	private Block m_input;
	private Block m_output;
	private int m_bufferLen; // Used for attack lookahead.
	private FloatBuffer m_bufferStart;

	private ByteBuffer buffer;
	private FloatBuffer m_bufferIO; // Initial readout/readin point.

	private int m_bufferIO_position = 0;

	private float m_threshold; // Control parameters.
	private long m_holdLen;
	private long m_releaseLen;

	private float m_gain;
	private float m_gainRate;
	private float m_gainTarget;
	private float m_peak;
	private long m_holdCount;
	private long m_releaseCount;
	private int m_state;

	protected final Scene scene;
	
	public void setInput(Block input) {
		m_input = input;
	};

	public void setOutput(Block output) {
		m_output = output;
	};

	public void setThreshold(float t) {
		m_threshold = t;
	};

	public void setHoldTime(float t) {
		m_holdLen = (long) (t * scene.getFPS());
	};

	public void setReleaseTime(float t) {
		m_releaseLen = (long) (t * scene.getFPS());
	};

	public Block tick(Block input, Block output) {
		m_input = input;
		m_output = output;
		return tick();
	}

	public Block tick(Block io) {
		m_input = io;
		m_output = io;
		return tick();
	}

	Block getOutput() {
		return m_output;
	};

	public final static float RATEBOOST = 2.0f;

	// public final static float REPORT = check(out[i],m_threshold);
	void check(float f, float t) {
		if (f > t) {
			// printf("limiter fail\n");
		}
	}

	public Limiter(float attackTime, float holdTime, float releaseTime,Scene scene) {
		this.scene = scene;
		m_input = null; // io blocks, user set.
		m_output = null;

		// Make buffer a multiple of blocks, to make buffer read write simpler.
		m_bufferLen =  scene.getNFrames() * (1 + (int) (attackTime * scene.getFPSFrames()));
		buffer = ByteBuffer.allocateDirect(m_bufferLen*Float.SIZE/Byte.SIZE).order(ByteOrder.nativeOrder());
		m_bufferStart =  buffer.asFloatBuffer();
		m_bufferIO = m_bufferStart.duplicate(); // (float*)paCalloc(m_bufferLen,
												// sizeof(float));
	
		// m_bufferEnd = m_bufferStart + m_bufferLen;
		// m_bufferStart = 0; // Initial readout/readin point.

		m_state = DIRECT;
		m_threshold = 20000; // 32767 //! Reduced to prevent glitching -
								// something not quite right.
		m_peak = 0.0f;
		m_gain = 1.0f;
		m_holdLen = (long) (holdTime * scene.getFPS()); // Cannot be
																// smaller than
																// paBlock
																// ::nFrames.
		m_releaseLen = (long) (releaseTime * scene.getFPS());

	}

	/*
	 * ~paLimiter() { paFree(m_bufferStart); }
	 */

	Block tick() {

		FloatBuffer in = m_input.getStart();
		FloatBuffer out = m_output.getStart();
		float t;
		float gainRate;
		float gainTarget;
		float peak;
		int i;

		// REPORTSTART

		for (i = 0; i < scene.getNFrames(); i++) {

			switch (m_state) {
			case DIRECT:
				while (i < scene.getNFrames()) {
					t = in.get(i); // Need temp variable in case output = input.
					out.put(i,m_bufferIO.get(i));
					// REPORT
					m_bufferIO.put(i, t); // Buffer for next
																// time.

					if ((peak = Math.abs(t)) > m_threshold) {
						m_state = ATTACK;
						m_peak = peak;
						m_gain = 1.0f;
						m_gainTarget = m_threshold / (m_peak + 0.5f); // Ensures
																		// when
																		// target
																		// reached
																		// output
																		// is at
																		// threshold
																		// .
						// RATEBOOST* gainRate moves kink a bit earlier..
						// reduces glitch
						m_gainRate = RATEBOOST * (m_gainTarget - m_gain)
								/ m_bufferLen;
						break;
					}
					i++;
				}
				break;

			case ATTACK:
				while (i <scene.getNFrames()) {
					t = in.get(i);
					m_gain += m_gainRate;
					out.put(i, m_bufferIO.get(i) * m_gain);
					// REPORT
					/*
					 * if(ABS(out[i]) > m_threshold) { int a=1; }
					 */
					m_bufferIO.put(i, t); // Buffer for next
																// time.

					if (m_gain <= m_gainTarget) // Target reached.
					{
						m_state = HOLD;
						m_holdCount = m_holdLen;
						m_peak = m_threshold / m_gain;
						break;
					}

					if ((peak = Math.abs(t)) > m_threshold) {
						gainTarget = m_threshold / (peak + 0.5f);
						gainRate = RATEBOOST * (gainTarget - m_gain)
								/ m_bufferLen;

						if (gainRate < m_gainRate) // ie faster decrease
													// neccessary to cover new
													// peak.
						{
							m_gainTarget = gainTarget;
							m_gainRate = gainRate;
							m_peak = peak;
						} else if (peak > m_peak) // Current attack extended to
													// cover new peak.
						{
							m_gainTarget = gainTarget;
							m_peak = peak;
						}
						// Otherwise current attack or the following hold will
						// cover new peak.
					}

					i++;
				}
				break;

			case HOLD:
				while (i <scene.getNFrames()) {

					t = in.get(i);
					out.put(i, m_bufferIO.get(i) * m_gain);
					// REPORT
					m_bufferIO.put(i,t);

					m_holdCount--;

					if ((peak = Math.abs(t)) > m_threshold) {
						if (peak <= m_peak) // Extend hold to cover new peak.
						{
							if (m_holdCount < m_bufferLen)
								m_holdCount = m_bufferLen;
						} else {
							m_state = ATTACK; // Start new attack.
							m_peak = peak;
							m_gainTarget = m_threshold / (m_peak + 0.5f);
							m_gainRate = RATEBOOST * (m_gainTarget - m_gain)
									/ m_bufferLen;
							break;
						}
					}

					if (m_holdCount == 0) {
						m_state = RELEASE;
						m_gainRate = (1.0f - m_gain) / m_releaseLen;
						break;
					}

					i++;
				}
				break;

			case RELEASE:
				while (i <scene.getNFrames()) {
					t = in.get(i);
					m_gain += m_gainRate;
					out.put(i, m_bufferIO.get(i) * m_gain);

					// REPORT
					m_bufferIO.put(i,t);

					if ((peak = Math.abs(t)) > m_threshold) {
						gainTarget = m_threshold / (peak + 0.5f);
						if ((gainTarget - m_gain) / m_bufferLen < m_gainRate) // ie
																				// peak
																				// is
																				// not
																				// currently
																				// covered
																				// .
						{
							if (gainTarget > m_gain) // Start new attack.
							{
								m_state = ATTACK;
								m_peak = peak;
								m_gainTarget = gainTarget;
								m_gainRate = RATEBOOST
										* (m_gainTarget - m_gain) / m_bufferLen;
								break;
							} else { // Start short hold.
								m_state = HOLD;
								m_peak = peak;
								m_holdCount = m_bufferLen;
								break;
							}
						}
					}

					if (m_gain >= 1.0f) {
						m_state = DIRECT;
						break;
					}

					i++;
				}
				break;

			}
		}
		// REPORTSTOP
		m_bufferIO.position(scene.getNFrames());//position relative to the last slice
		m_bufferIO = m_bufferIO.slice();
		if(m_bufferIO.capacity()<scene.getNFrames())//if we have gotten to the end of the buffer, start from the begining.
			m_bufferIO = m_bufferStart.duplicate();

	/*	m_bufferIO_position += Block.nFrames;
		if (m_bufferIO_position >= m_bufferLen)
			m_bufferIO_position = 0; // watch out, this could easily be wrong...
*/		// if (m_bufferIO == m_bufferEnd) m_bufferIO = m_bufferStart;
		return m_output;
	}

}
