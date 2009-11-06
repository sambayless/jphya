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

package com.jphya.resonator;

import java.nio.FloatBuffer;

import com.jphya.distance.DistanceModel;
import com.jphya.scene.Scene;
import com.jphya.signal.Block;

public class ModalResonator extends Resonator {

	public static final float TWOPI = 6.2853f;

	private static final float DENORMALISATION_EPSILON = 1e-20f;

	private ModalData m_data;
	private int m_nActiveModes = 0;

	// Runtime coefficients calculated from ModalData.

	/**
	 * Array of resonator coefficients for different modes.
	 */
	private float[] m_cplus; // 
	
	/**
	 * Array of resonator coefficients for different modes.
	 */
	private float[] m_cminus;
	
	/**
	 * Array of resonator coefficients for different modes.
	 */
	private float[] m_aa;
	
	/**
	 *  End-of-last-block resonator state-variables for each mode,
	 */
	private float[] m_u; //
	
	/**
	 * Stored for use at the start of the next block.
	 */
	private float[] m_v; // s
	
	/**
	 * coefficients used in finding the envelope level of the modes.
	 */
	private float[] m_l0; 
	
	/**
	 * coefficients used in finding the envelope level of the modes.
	 */
	private float[] m_l1;
	
	/**
	 *  Threshold for being quiet.
	 *  Default: 1
	 */
	private float m_quietLevelSqr = 1f; //
	// float m_dcBias; // Used to reduce limit cycles and denormalization.

	/**
	 *  Scale factor for frequency (act in addition to the scale factors in the modal data.
	 */
	private float m_auxFreqScale;
	
	/**
	 *  Scale factor for dampening (act in addition to the scale factors in the modal data.
	 */
	private float m_auxDampScale;
	
	/**
	 *  Scale factor for amplitude/volume (act in addition to the scale factors in the modal data.
	 */
	private float m_auxAmpScale;

	private boolean m_recalcAll;

	public int setnActiveModes(int n) {
		// assert(n <= m_data.getnModes());
		if (m_data == null || n > m_data.getM_nModes())
			return -1;
		m_nActiveModes = n;
		return 0;
	};

	public void setAuxAmpScale(float s) {
		if (m_auxAmpScale != s) {
			m_auxAmpScale = s;
			m_recalcAll = true;
		}
	
	};

	public void setAuxFreqScale(float s) {
		if (m_auxFreqScale != s) {
			m_auxFreqScale = s;
			m_recalcAll = true;
		}

	};

	public void setAuxDampScale(float s) {
		if (m_auxDampScale != s) {
			m_auxDampScale = s;
			m_recalcAll = true;
		}
	
	};

	public int setData(ModalData d) {
		if (d != null) // Otherwise initialize default mode
		{
			m_data = d;
			m_nActiveModes = m_data.getM_nModes();
		}

		zero();
		m_recalcAll = false;

		return (0);
	}

	public void zero() {
		int i;

		calcCoefficients();

		for (i = 0; i < Modal.nMaxModes; i++) // Set initial states, consistent
		// with DC bias.
		{
			m_u[i] = 0; // m_dcBias / (1-m_cminus[i] + 1/(1-m_cplus[i]));
			m_v[i] = 0; // m_u[i]/(1-m_cplus[i]);
		}

	
	}

	public ModalResonator(Scene scene) {
		super(scene);
		m_nActiveModes = 0;

		// ! Mallocs should be managed dynamically in file load etc.
		m_cplus = new float[Modal.nMaxModes]; //paFloatCalloc(paModal::nMaxModes
		// );
		m_cminus = new float[Modal.nMaxModes]; //paFloatCalloc(paModal::nMaxModes
		// );

		m_aa = new float[Modal.nMaxModes];// paFloatCalloc(paModal::nMaxModes);

		m_u = new float[Modal.nMaxModes];// paFloatCalloc(paModal::nMaxModes);
		m_v = new float[Modal.nMaxModes];// paFloatCalloc(paModal::nMaxModes);

		m_l0 = new float[Modal.nMaxModes];// paFloatCalloc(paModal::nMaxModes);
		m_l1 = new float[Modal.nMaxModes];// paFloatCalloc(paModal::nMaxModes);

		m_data = null;

		m_auxFreqScale = 1.0f;
		m_auxAmpScale = 1.0f;
		m_auxDampScale = 1.0f;

		contactDampingOld = 1.0f;
		resetContactDamping();

		setQuietLevel(1.0f);

		m_recalcAll = true;

		// m_dcBias = 0.0f;
	}

	

	/**
	 *  Calculate resonator filter coefficients.
	
	
	 This is done once when a resonator is first initialized,
	 and then whenever changes are made to the modes,
	 eg when auxScaleFactors are changed.
	
	 Frequent Frequency Update For Deformable Body Effects and Similar:
	
	 The calculations here look fairly heavy, but small fequency jumps aren't
	 very noticeable, so we can get away with update rates of as low as 30Hz
	 for typical frequency
	 variations. At this rate the relative cost of calculating coefficients
	 is low compared to the resonator calculation.
	 Frequent Amplitude Update For Continuous Contact:
	
	 Approximating continuous variations of coupling amplitudes with discrete
	 jumps is
	 much more noticeable than for frequency variation, but luckily the cost
	 of updating
	 the aa parameters can be much lower if simple interpolation is applied
	 between updates.
	 Continuous amplitude coupling variation corresponds to moving a contact
	 point over
	 a surface. However we can just set the coupling on initial contact and
	 not bother to
	 vary it, without noticing much difference - Variation of coupling with
	 impacts is
	 more important to model. For this reason coupling interpolation is not
	 implemented,
	 although it could be added transparently as a user option for each input.
	 *  
	 */
	private int calcCoefficients() {
		float t1;
		float t2;
		float t3;

		float creal;
		float cimg;
		float ds = contactDamping * m_auxDampScale / scene.getFPS();
		float fs = m_auxFreqScale / scene.getFPS();
		assert (m_data != null);// ("Modal data undefined in resonator.",
		// assert( !(m_data!=null && (!m_data.m_amp || !m_data.m_damp ||
		// !m_data.
		// m_freq))));//("Modal data defined in resonator, but not valid",

		float[] d = m_data.m_damp; // Resolve indirection before loop.
		float[] f = m_data.m_freq;
		float[] a = m_data.m_amp;

		float fTemp;

		int m;

		for (m = 0; m < m_nActiveModes; m++) {
			fTemp = (float) f[m] * fs;
			if (fTemp > .5f)
				m_aa[m] = 0.0f; // Remove mode if it would frequency-alias.
			else {
				t1 = (float) Math.exp(-d[m] * ds);
				t2 = (float) TWOPI * fTemp;
				creal = (float) Math.cos(t2) * t1; // ! Replace with cheaper
				// approximation?
				cimg = (float) Math.sin(t2) * t1;

				t3 = (float) Math.sqrt(1f - cimg * cimg);
				m_cplus[m] = creal + t3;
				m_cminus[m] = creal - t3;
				m_aa[m] = a[m] * cimg * m_auxAmpScale;

				// Find constants used by isQuiet() to find level of mode[0]
				m_l0[m] = (float) (2.0f * (t3 - (float) 1.0f));
				// m_l0[m] = (float)(2.0* (sqrt(1 - cimg*cimg) - (float)1.0));
				m_l1[m] = 1f / cimg / cimg;

			}
		}

		return 0;
	}

	public void processControlInput() {
		// ContactDamping accumulation check.
		if (contactDamping > maxContactDamping)
			contactDamping = maxContactDamping;
		if (contactDamping != contactDampingOld) {
			contactDampingOld = contactDamping;
			m_recalcAll = true;
		}

		if (m_recalcAll) {
			calcCoefficients();
			m_recalcAll = false;
			return;
		}

		// Amp coupling update for separate inputs goes here..
	}

	/**
	 * Determines at what rms envelope level
	 	a resonator will be
		faded out when no longer in contact, to save cpu.
	 	Make bigger to save more cpu, but possibly truncate decays
		notceably.
	 */
	public void setQuietLevel(float l) {
		// l *= 0.1f; // Makes up for inaccuracy in level detector.
		m_quietLevelSqr = l * l;
	};

	public Block tick() {

		m_output.zero(); // Could be made faster by overwriting output on mode
		// 0,
		// but this won't make much difference because num modes not small.
		tickAdd();
		
		DistanceModel dist = super.body.getDistanceModel();
		dist.applyDistanceModel(body.getPreviousDistance(), body.getCurrentDistance(), m_output);
		return m_output;
	}

	public Block tickAdd() {
		// Modal resonator, based on 2nd order variant optimized for speed by
		// Kees van den Doel.

		int nFrames = scene.getNFrames();
		int mode;
		int i;
		float uPrev;
		float u = 0f;
		float v;
		float cp, cm, aaa;

		assert (getM_input() != null);
		assert (m_output != null);
		assert (getM_input() != m_output);// (
		// "res.tickAdd() cannot use an input and output block the same."
		// ,

		// float[] out = m_output.getStart(); // Output samples. 'output' is
		// const. 'start' isn't.
		// float[] in = getM_input().getStart();
		FloatBuffer out = m_output.getStart();
		FloatBuffer in = getM_input().getStart();

		processControlInput(); // Thread-safe updating of internal state (incl
		// coefficients)
		// according to control input received since last processControlInput().

		for (mode = 0; mode < m_nActiveModes; mode++) {

			uPrev = m_u[mode]; // Load resonator state for this mode from end of
			// previous block.
			v = m_v[mode];
			cp = m_cplus[mode];
			cm = m_cminus[mode];

			// dcOffset = m_dcBias / (1-cm + 1/(1-cp)); // Experimental
			// anti-limit cycle offset

			// Run the resonator for this mode for one block.

			// The impulse response of each mode has the form sin(wt)exp(lambda
			// t), t>0
			// This is the physical displacement generated by a force impulse.
			// Note the standard 2-pole resonator is slightly different having
			// the response
			// cos(wt)exp(lambda t)

			// There are limit cycles, but they are made small
			// by scaling the input up, and the output down:

			aaa = m_aa[mode];

			for (i = 0; i < nFrames; i++) {
				u = cm * uPrev - v + aaa * in.get(i)
						+ (float) DENORMALISATION_EPSILON; // Prevents
				// denormalization.
				v = cp * v + uPrev;
				uPrev = u;
				out.put(i, out.get(i) + v); // out[i] += v;
			}

			m_u[mode] = u;
			m_v[mode] = v;
		}

		return m_output; // paBlock pointer

	} // tickAdd()

	public float estimateVolume()
	{
		float u;
		float v;
		int mode;
		float lsqr = 0;

		for (mode = 0; mode < m_nActiveModes; mode++) {

			// Use state variables to find mode levels and estimated total level
			// (squared)
			// (total level is sqrt energy. energy = sum of mode energies = sum
			// of mode levels squared)

			u = m_u[mode];
			v = m_v[mode];

			lsqr += m_l1[mode] * ((u + v) * (u + v) + m_l0[mode] * u * v);
		}
		return lsqr;
	}
	
	public boolean isQuiet() {
		if (makeQuiet) {
			makeQuiet = false;
			return true;
		}

		float lsqr = estimateVolume();

		// ! Could maybe quieten modes seperately if they are barely excited.
		// ! Need to distinguish between impact and contact cases though,
		// because
		// ! impacts can be faded from a fairly high level, whereas contacts
		// will
		// ! sound poor if repeatedly faded and rewoken: Use 'inContact' flag in
		// resonators.

		return (lsqr < m_quietLevelSqr);
	}

	public int getTimeCost() {
		// For now a rough estimate of time cost, on an arbitrary scale.
		return m_nActiveModes * 10;
	}
}
