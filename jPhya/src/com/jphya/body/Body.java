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

package com.jphya.body;

import com.jphya.distance.DistanceModel;
import com.jphya.distance.NullDistanceModel;
import com.jphya.resonator.Resonator;
import com.jphya.surface.Surface;

public class Body {

	private Resonator resonator;
	private Surface surface;
	private boolean isEnabled = true;

	private Object m_userData;

	private DistanceModel distanceModel = NullDistanceModel.instance;

	public DistanceModel getDistanceModel() {
		return distanceModel;
	}

	public void setDistanceModel(DistanceModel distanceModel) {
		this.distanceModel = distanceModel;
	}

	/**
	 * User to interpolate changes in position. This is distance to the camera
	 * or ear at the last sound point
	 */
	private float previousDistance = 0f;

	public float getPreviousDistance() {
		return previousDistance;
	}

	public void setPreviousDistance(float previousDistance) {
		this.previousDistance = previousDistance;
	}

	public float getCurrentDistance() {
		return currentDistance;
	}

	public void setCurrentDistance(float currentDistance) {
		this.currentDistance = currentDistance;
	}

	/**
	 * User to interpolate changes in position. This is distance to the camera
	 * or ear at the current sound point
	 */
	private float currentDistance = 0f;

	public Body() {
		resonator = null;
		surface = null;
		isEnabled = true;

		contactAmpLimit = (float) -1.0;
		contactMasterGain = (float) 1.0;
		contactToOtherResGain = (float) 1.0;
		contactDirectGain = (float) 0.0;

		impactAmpLimit = (float) -1.0;
		impactMasterGain = (float) 1.0;
		impactToOtherResGain = (float) 1.0;
		impactDirectGain = (float) 0.0;
	}

	public int setResonator(Resonator r) {
		resonator = r;
		r.setBody(this);
		r.zero();
		return 0;
	}

	public Resonator getResonator() {
		return resonator;
	}

	public Surface getSurface() {
		return surface;
	}

	public float maxImpulse;

	// Coupling parameters:

	private float contactAmpLimit;

	private float contactMasterGain;
	private float contactToOtherResGain;
	private float contactDirectGain;

	private float impactAmpLimit;
	private float impactMasterGain;
	private float impactToOtherResGain;
	private float impactDirectGain;

	public int setContactAmpLimit(float g) {
		contactAmpLimit = g;
		return 0;
	} // Limit the amplitude just before the surface generator.

	public int setContactMasterGain(float g) {
		contactMasterGain = g;
		return 0;
	}

	public int setContactToOtherResGain(float g) {
		contactToOtherResGain = g;
		return 0;
	}

	public int setContactDirectGain(float g) {
		contactDirectGain = g;
		return 0;
	}

	public int setImpactAmpLimit(float g) {
		impactAmpLimit = g;
		return 0;
	}

	public int setImpactMasterGain(float g) {
		impactMasterGain = g;
		return 0;
	}

	public int setImpactToOtherResGain(float g) {
		impactToOtherResGain = g;
		return 0;
	}

	public int setImpactDirectGain(float g) {
		impactDirectGain = g;
		return 0;
	}

	public float getContactAmpLimit() {
		return contactAmpLimit;
	}

	public float getContactMasterGain() {
		return contactMasterGain;
	}

	public float getContactToOtherResGain() {
		return contactToOtherResGain;
	}

	public float getContactDirectGain() {
		return contactDirectGain;
	}

	public float getImpactAmpLimit() {
		return impactAmpLimit;
	}

	public float getImpactMasterGain() {
		return impactMasterGain;
	}

	public float getImpactToOtherResGain() {
		return impactToOtherResGain;
	}

	public float getImpactDirectGain() {
		return impactDirectGain;
	}

	public Object getUserData() {
		return m_userData;
	}

	public int setUserData(Object d) {
		m_userData = d;
		return 0;
	}

	public int enable() {
		isEnabled = true;
		return 0;
	}

	public int disable() {
		isEnabled = false;
		return 0;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public int setSurface(Surface s) {
		surface = s;

		maxImpulse = s.m_maxImpulse;

		// Copy couplings from surface, so that alterations
		// can be made for this body.

		contactAmpLimit = s.m_contactAmpLimit;
		contactMasterGain = s.m_contactMasterGain;
		contactToOtherResGain = s.m_contactToOtherResGain;
		contactDirectGain = s.m_contactDirectGain;

		impactAmpLimit = s.m_impactAmpLimit;
		impactMasterGain = s.m_impactMasterGain;
		impactToOtherResGain = s.m_impactToOtherResGain;
		impactDirectGain = s.m_impactDirectGain;

		return 0;
	}

}
/*
 * 
 * class PHYA_API paBody { friend class paContact; friend class paImpact;
 * 
 * private: Res m_resonator; Surface m_surface; bool m_isEnabled;
 * 
 * void m_userData;
 * 
 * public:
 * 
 * paBody();
 * 
 * // User interface:
 * 
 * int setRes(Res r) { m_resonator = r; r.zero(); return 0; } int
 * setSurface(Surface s);
 * 
 * Res getRes() { return m_resonator; } Surface getSurface() { return m_surface;
 * }
 * 
 * 
 * float m_maxImpulse;
 * 
 * 
 * // Coupling parameters:
 * 
 * float m_contactAmpLimit; float m_contactMasterGain; float
 * m_contactToOtherResGain; float m_contactDirectGain;
 * 
 * float m_impactAmpLimit; float m_impactMasterGain; float
 * m_impactToOtherResGain; float m_impactDirectGain;
 * 
 * int setContactAmpLimit(float g) { m_contactAmpLimit = g; return 0; } // Limit
 * the amplitude just before the surface generator. int
 * setContactMasterGain(float g) { m_contactMasterGain = g; return 0; } int
 * setContactToOtherResGain(float g) { m_contactToOtherResGain = g; return 0; }
 * int setContactDirectGain(float g) { m_contactDirectGain = g; return 0; }
 * 
 * int setImpactAmpLimit(float g) { m_impactAmpLimit = g; return 0; } int
 * setImpactMasterGain(float g) { m_impactMasterGain = g; return 0; } int
 * setImpactToOtherResGain(float g) { m_impactToOtherResGain = g; return 0; }
 * int setImpactDirectGain(float g) { m_impactDirectGain = g; return 0; }
 * 
 * 
 * void getUserData() { return m_userData; } int setUserData(void d) {
 * m_userData = d; return 0; }
 * 
 * int enable() { m_isEnabled = true; return 0; } int disable() { m_isEnabled =
 * false; return 0; } bool isEnabled() { return m_isEnabled; }
 * 
 * };
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * #endif
 */