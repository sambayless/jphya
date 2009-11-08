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

import com.jphya.scene.Scene;
import com.jphya.signal.Block;
import com.jphya.surface.Surface;


/**
 * 
 //
// paImpactGen.hpp
//
// Abstract base class for generating sound from a continuous contact.
// Referenced by active paContact objects.
//
 *
 */
public abstract class ImpactGenerator {

	protected Block m_output;

	protected Surface m_otherSurface;
	protected float m_impactGain;
	protected float m_limit;

	protected float m_relTangentSpeedAtImpact;
	protected float m_relNormalSpeedAtImpact;
	protected float m_impactImpulse;

	protected boolean m_isNew;
	protected final Scene scene;
	
	public Scene getScene() {
		return scene;
	}

	public ImpactGenerator(Scene scene) {
		super();
		this.scene = scene;
		//scene.addImpactGenerator(this);
	}

	public abstract Block tick();

	//public int m_poolHandle; // Handle used by the scene contact manager to
								// release the object.

	public abstract boolean isQuiet(); // Used to determine when impact should
										// be deleted.

	public void setRelTangentSpeedAtImpact(float s) {
		m_relTangentSpeedAtImpact = s;
	}

	public void setRelNormalSpeedAtImpact(float s) {
		m_relNormalSpeedAtImpact = s;
	}

	public void setImpactImpulse(float i) {
		m_impactImpulse = i;
	}

	public abstract void setOutput(Block output);

	public Block getOutput() {
		return m_output;
	}

	public void setIsNew() {
		m_isNew = true;
	}

	public void setImpactGain(float g) {
		m_impactGain = g;
	}

	public void setLimit(float l) {
		m_limit = l;
	}

	public void setOtherSurface(Surface s) {
		m_otherSurface = s;
	}

}
