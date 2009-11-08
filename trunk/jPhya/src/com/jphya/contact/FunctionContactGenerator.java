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
package com.jphya.contact;

import com.jphya.scene.Scene;
import com.jphya.signal.Block;
import com.jphya.signal.Lowpass;
import com.jphya.surface.FunctionSurface;


public class FunctionContactGenerator extends ContactGenerator{

	private FunctionSurface m_surface;		// Surface description used by this generator.
	private Lowpass m_lowpass = new Lowpass();			// Filter used to model variation in relative body slip / roll
	private Lowpass m_lowpass2 = new Lowpass();		// Steeper cutoff for better rolling sound.

	public FunctionContactGenerator(Scene scene)
	{
		super(scene);
		
	}


	public void
	initialize(FunctionSurface surface)
	{
		// Copy / reference surface data.

		m_surface = surface;
		assert(surface.getFun()!=null);
		setM_x(setM_y1(m_y2 = 0));
//		m_x = 100;
		m_rate = 0;
		m_limit = surface.m_contactAmpLimit;
		m_lowpass.reset();
		m_lowpass2.reset();
	}

	public void setOutput(Block o) { 
		m_output = o;
		m_lowpass.setInput(o);
		m_lowpass.setOutput(o);
		m_lowpass2.setInput(o);
		m_lowpass2.setOutput(o);

	};

	// State variables accessible to the surface function:
	private float m_x;					// Position of contact on surface.
	private float m_rate;				// Speed of contact relative to surface.
	public float getM_rate() {
		return m_rate;
	}

	private float m_y1;					// State, could be used to record surface height 
	private float m_y2;					// currently being interpolated for instance.
									// Could add more here...
	
	public Block	tick()
	{
		if (m_speedBody1RelBody2 < 0f) m_speedBody1RelBody2 = -m_speedBody1RelBody2;

		float cutoffFreq = m_surface.getM_cutoffMin() + m_speedBody1RelBody2 * m_surface.getM_cutoffRate();
		if (cutoffFreq > m_surface.getM_cutoffMax()) cutoffFreq = m_surface.getM_cutoffMax();
	//printf("%f\n", cutoffFreq);

		//! Amp changes should be interpolated over the block for smoothness. 
		float amp = m_contactForce * m_gain;
		if (m_speedBody1RelBody2 < m_surface.getM_boostBreak()) 
			amp *= (1f+ (m_surface.getM_boostBreak()-m_speedBody1RelBody2) * m_surface.getM_boostRate());
		if (m_limit > 0f) { if (amp > m_limit) amp = m_limit; }


		m_lowpass.setCutoffFreq(cutoffFreq);
		m_lowpass.setGain(amp);

		m_rate = m_speedContactRelBody * m_surface.getM_rateScale();	// for m_fun.


		m_surface.getM_fun().tick(this);		// Use state from this gen. Output to m_lowpass.

		m_lowpass.tick();

		if (m_surface.getM_cutoffMin2() > 0)
		{
			float cutoffFreq2 = m_surface.getM_cutoffMin2() + m_speedBody1RelBody2 * m_surface.getM_cutoffRate2();
			if (cutoffFreq2 > m_surface.getM_cutoffMax2()) cutoffFreq2 = m_surface.getM_cutoffMax2();
			m_lowpass2.setCutoffFreq(cutoffFreq2);
			m_lowpass2.setGain(1.0f);

			m_lowpass2.tick();				// 2nd order lowpass.

		}

		return m_output;
	}




	public boolean isQuiet()
	{
		if (m_contactForce == 0 || Math.abs(m_speedContactRelBody) < m_surface.getM_quietSpeed()) 
			return true;
//		else return false;
	//! This logic is no good for constant non-zero gen output.
	//! Issue is a little tricky, and the performance gains are not that great, so for now..
	return false;
	}


	public void setM_x(float m_x) {
		this.m_x = m_x;
	}


	public float getM_x() {
		return m_x;
	}


	public float setM_y1(float m_y1) {
		this.m_y1 = m_y1;
		return m_y1;
	}


	public float getM_y1() {
		return m_y1;
	};
}