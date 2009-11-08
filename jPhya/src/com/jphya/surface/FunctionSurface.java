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

package com.jphya.surface;

import com.jphya.contact.ContactGenerator;
import com.jphya.contact.FunctionContactGenerator;
import com.jphya.impact.FunctionImpactGen;
import com.jphya.impact.ImpactGenerator;
import com.jphya.scene.Scene;
import com.jphya.signal.Block;


public class FunctionSurface extends Surface {

	// Surface properties for contact:
	protected Function m_fun; // Function which generates the raw surface profile.
	protected float m_cutoffRate;
	protected float m_cutoffMin;
	protected float m_cutoffMax;
	protected float m_cutoffRate2; // Secondary filter used to steepen cutoff
									// when rolling.
	protected float m_cutoffMin2;
	protected float m_cutoffMax2;
	protected float m_rateScale;
	protected float m_quietSpeed;
	protected float m_boost; // Boost when rolling due to increased transfer at
								// low freqs.
	protected float m_boostBreak; // Slip speed at which boost starts phasing
									// in.
	protected float m_boostRate;

	// Surface properties for impact:
	protected float m_skidImpulseToForceRatio;
	protected float m_skidGain; // Gain of contact gen, possibly redundant
								// because of impulseToForceRatio.
	protected int m_nSkidBlocks; // ! Skid time, change this.
	protected float m_skidThickness; // Simulate a loose surface layer.
	// Skidding time = skidThickness / normalSpeed.
	protected float m_skidMinTime;
	protected float m_skidMaxTime;

	
	protected final Scene scene;
	
	public Function getM_fun() {
		return m_fun;
	}

	public float getM_cutoffRate() {
		return m_cutoffRate;
	}

	public float getM_cutoffMin() {
		return m_cutoffMin;
	}

	public float getM_cutoffMax() {
		return m_cutoffMax;
	}

	public float getM_cutoffRate2() {
		return m_cutoffRate2;
	}

	public float getM_cutoffMin2() {
		return m_cutoffMin2;
	}

	public float getM_cutoffMax2() {
		return m_cutoffMax2;
	}

	public float getM_rateScale() {
		return m_rateScale;
	}

	public float getM_quietSpeed() {
		return m_quietSpeed;
	}

	public float getM_boost() {
		return m_boost;
	}

	public float getM_boostBreak() {
		return m_boostBreak;
	}

	public float getM_boostRate() {
		return m_boostRate;
	}

	public float getM_skidImpulseToForceRatio() {
		return m_skidImpulseToForceRatio;
	}

	public float getM_skidGain() {
		return m_skidGain;
	}

	public int getM_nSkidBlocks() {
		return m_nSkidBlocks;
	}

	public float getM_skidThickness() {
		return m_skidThickness;
	}

	public float getM_skidMinTime() {
		return m_skidMinTime;
	}

	public float getM_skidMaxTime() {
		return m_skidMaxTime;
	}

	public int setFun(Function f) {
		m_fun = f;
		return 0;
	}

	public Function getFun() {
		return m_fun;
		// return null;
	}

	public int setHardness(float h) {
		m_hardness = h;
		return 0;
	}

	// Rate is the rate a surface is traversed, and has different
	// meanings for different kinds of surface. eg
	// Grid - number of bars per second.
	// Rnd - number of bumps per second.
	// Wav - number of wav seconds per second.
	public int setRateAtSpeed(float r, float s) {
		m_rateScale = r / s;
		return 0;
	};

	// Slip speed / filter cutoff curve.
	// Specify rate of cutoff change with slip speed.
	// Cutoff rises to maximum if given, or sample rate max if that comes first.
	// cutoff 2 is for beefing up the sound when rolling.
	public int setCutoffFreqAtRoll(float f) {
		m_cutoffMin = f;
		return 0;
	}

	public int setCutoffFreqRate(float r) {
		m_cutoffRate = r;
		return 0;
	}

	public int setCutoffFreqMax(float m) {
		m_cutoffMax = m;
		return 0;
	}

	public int setCutoffFreq2AtRoll(float f) {
		m_cutoffMin2 = f;
		return 0;
	}

	public int setCutoffFreq2Rate(float r) {
		m_cutoffRate2 = r;
		return 0;
	}

	public int setCutoffFreq2Max(float m) {
		m_cutoffMax2 = m;
		return 0;
	}

	public int setBoostBreakSlipSpeed(float s) {
		m_boostBreak = s;
		m_boostRate = m_boost / m_boostBreak;
		return 0;
	};

	public int setBoostAtRoll(float b) {
		m_boost = b;
		m_boostRate = m_boost / m_boostBreak;
		return 0;
	};

	public int setSkidImpulseToForceRatio(float r) {
		m_skidImpulseToForceRatio = r;
		return 0;
	}

	// public int setSkidTime(float seconds);
	public int setSkidGain(float g) {
		m_skidGain = g;
		return 0;
	}

	public int setSkidThickness(float t) {
		m_skidThickness = t;
		return 0;
	}

	public int setSkidMinTime(float t) {
		m_skidMinTime = t;
		return 0;
	}

	public int setSkidMaxTime(float t) {
		m_skidMaxTime = t;
		return 0;
	}

	public int setQuietSpeed(float s) {
		m_quietSpeed = s;
		return 0;
	}

	public FunctionSurface(Scene scene) {
		this.scene = scene;
		setCutoffFreqAtRoll( 70.0f);
		setCutoffFreqRate( 8000.0f);
		setCutoffFreqMax( 1.0E6f);
		setCutoffFreq2AtRoll( -1.0f);
		setCutoffFreq2Rate( 80000.0f);
		setCutoffFreq2Max( 1.0E6f);

		setRateAtSpeed( 1.0f, 1.0f);

		setSkidImpulseToForceRatio( .01f); // ! Would like normalised to
													// 1.0
		setSkidGain( 1.0f);
		setSkidTime( 0.0f); // No auto skid initially. Auto skidding is a
									// lower-quality technique..
		setSkidThickness( -1.0f);
		setSkidMinTime( -1.0f);
		setSkidMaxTime( -1.0f);
		setHardness( 1000.0f);
		setQuietSpeed( 0.01f); // Assumes length scale of 1.
		setBoostBreakSlipSpeed(200.0f);
		setBoostAtRoll(0.0f);
	}


	// This could be improved 'theoretically' by defining newContactGen in
	// Surface.cpp
	// and replacing contactGenPool.newActiveObject() with
	// m_contactGenPool.newActiveObject()
	// and defining m_contactGenPool = &contactGenPool in funSurface().
	// Worth changing when there are more than one type of surface.

	public ContactGenerator newContactGen() {
		FunctionContactGenerator gen = new FunctionContactGenerator(scene);//contactGenPool.newActiveObject();

		if (gen == null)
			return null; // error -no gens left.

		//gen.m_poolHandle = contactGenPool.getHandle();

		gen.initialize(this);
		
		return gen;
	};

	public void deleteContactGen(ContactGenerator gen) {
		scene.removeContactGenerator(gen);
		//int err = contactGenPool.deleteActiveObject((FunctionContactGenerator)gen);
		//gen.m_poolHandle = -1;
		//return -1;
	}

	public ImpactGenerator newImpactGen() {
		FunctionImpactGen gen= new FunctionImpactGen(scene);
	
		/*FunctionImpactGen gen = impactGenPool.newActiveObject();
		if (gen == null)
			return null; // error -no gens left.
*/
	//	gen.m_poolHandle = impactGenPool.getHandle();

		gen.initialize(this);
		scene.addImpactGenerator(gen);
		return gen;
	};

	public void deleteImpactGen(ImpactGenerator gen) {
		scene.removeImpactGenerator(gen);
	/*	int err = impactGenPool.deleteActiveObject((FunctionImpactGen)gen);
		//gen.m_poolHandle = -1;
		return -1;*/
	}

	public int setSkidTime(float time) { // time in seconds.
		if (m_skidMinTime > 0.0)
			if (time < m_skidMinTime)
				time = m_skidMinTime;
		if (m_skidMaxTime > 0.0)
			if (time > m_skidMaxTime)
				time = m_skidMaxTime;

		if (time > 0.0)
			// Skid for atleast one block of time.
			m_nSkidBlocks = (int) ((float) scene.getFPS()
					/ (float) Block.INIT_NBLOCKFRAMES * time) + 1;
		else
			m_nSkidBlocks = 0;
		return 0;
	}

}