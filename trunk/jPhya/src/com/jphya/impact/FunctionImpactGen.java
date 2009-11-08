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

package com.jphya.impact;

import com.jphya.contact.FunctionContactGenerator;
import com.jphya.scene.Scene;
import com.jphya.signal.Block;
import com.jphya.signal.TriPulser;
import com.jphya.surface.FunctionSurface;


public class FunctionImpactGen extends ImpactGenerator {

	private FunctionSurface m_surface; // Surface description used by this generator.
	private TriPulser m_pulser;
	private FunctionContactGenerator m_contactGen;
	private boolean m_isQuiet;
	private boolean m_isSkidding;
	private int m_skidCount;
	private float m_impulseToHardnessBreakpoint;
	private float m_impulseToHardnessScale;

	public void setOutput(Block o) {
		m_pulser.setOutput(o);
		m_output = o;
	};

	public FunctionImpactGen(Scene scene) {
		super(scene);
		m_pulser = new TriPulser(scene);
		m_contactGen = new FunctionContactGenerator(scene);//when does this get removed?
		
		m_isQuiet = true;
		//scene.addContactGenerator(m_contactGen);
	}

	public void initialize(FunctionSurface surface) {
		// Copy / reference surface data.

		m_isNew = true;
		m_surface = surface;
		m_otherSurface = null;
		m_contactGen.initialize(surface);
		m_limit = surface.m_impactAmpLimit;
	}

	public Block tick() {
		if (m_isNew) {
			float hardness = m_surface.m_hardness;

			// Add extra hardness from nonlinearity of impact.
			if (m_impactImpulse > m_surface.m_impulseToHardnessBreakpoint) {
				hardness += m_surface.m_impulseToHardnessScale
						* (m_impactImpulse - m_surface.m_impulseToHardnessBreakpoint);
			}

			// Find the least hard surface, if there are two.
			// (This is a simplification of impulseTime =
			// sqrt((1/l1+1/l2)/(1/m1+1/m2))
			// (l1,l2 are spring constants for the surfaces ie 'hardnesses'.
			// Could incorporate body mass from body data accessed through
			// paImpact.

			if (m_otherSurface != null) {
				float otherSurfaceHardness = m_otherSurface.m_hardness;

				if (m_impactImpulse > m_otherSurface.m_impulseToHardnessBreakpoint) {
					otherSurfaceHardness += m_otherSurface.m_impulseToHardnessScale
							* (m_impactImpulse - m_otherSurface.m_impulseToHardnessBreakpoint);
				}

				if (otherSurfaceHardness < hardness)
					hardness = otherSurfaceHardness;
			}

			// printf("%f\n",hardness);

			m_pulser.setWidthSeconds(1f / hardness);

			float amp = m_impactImpulse * m_impactGain;

			// amp *= amp; // More realistic transfer. // User can do this.

			if (m_limit > 0) {
				if (amp > m_limit)
					amp = m_limit;
			}

			m_pulser.hit(amp);

			m_isSkidding = false;

			if ((m_surface.getM_skidGain() != 0.0)
					&& ((m_surface.getM_skidThickness() > 0) || (m_surface
							.getM_nSkidBlocks() > 0))
					&& (m_relTangentSpeedAtImpact != 0.0)) {
				m_isSkidding = true;
				m_contactGen.setGain(m_surface.getM_skidGain());
				m_contactGen.setSpeedBody1RelBody2(m_relTangentSpeedAtImpact);
				m_contactGen.setSpeedContactRelBody(m_relTangentSpeedAtImpact);
				m_contactGen.setContactForce(m_impactImpulse
						* m_surface.getM_skidImpulseToForceRatio());

				if (m_surface.getM_skidThickness() > 0) {
					float skidTime = m_surface.getM_skidThickness()
							/ m_relNormalSpeedAtImpact;
					m_surface.setSkidTime(skidTime); // Calc nBlocks.
				}
				m_skidCount = m_surface.getM_nSkidBlocks();
			}

			m_isNew = false;
		}

		m_pulser.tick();

		if (m_isSkidding) {
			// Get temporary audio block for contact calculation.
			Block contactOutput = Block.newBlock(scene);

			m_contactGen.setOutput(contactOutput);
			m_contactGen.tick();
			m_output.add(contactOutput);
			Block.deleteBlock(contactOutput);
			m_skidCount--;
			if (m_skidCount == 0)
				m_isSkidding = false;
		}

		// Impact quiet when pulser quiet and skid quiet.

		m_isQuiet = (m_pulser.isQuiet() && (!m_isSkidding));

		return m_output;
	}

	public boolean isQuiet() {
		return m_isQuiet;
	}

}
