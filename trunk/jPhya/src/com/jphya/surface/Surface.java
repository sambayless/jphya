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

package com.jphya.surface;

import com.jphya.contact.ContactGenerator;
import com.jphya.impact.ImpactGenerator;


public abstract class Surface {
	

		public abstract  ContactGenerator newContactGen() ;			// Used in main tick() to create new contacts
		public abstract void deleteContactGen(ContactGenerator g);		// of the correct surface type.

		public abstract ImpactGenerator newImpactGen();
		public abstract void deleteImpactGen(ImpactGenerator g);


		public float m_maxImpulse;	// For impacts.
		public float m_maxForce;		// For contacts.
		public float m_hardness;						// Used to calculate 'combined hardness' of a collision.
													// From this the parameters of each impact generator are derived.
													// Generally m_hardness = 1/(timescale_of_impact).
													// Could possible be used in contact calculations.
		public float m_impulseToHardnessBreakpoint;	// Hardness is increased by the scale times the
		public float m_impulseToHardnessScale;			// amount the impulse exceeds the breakpoint.
													// Models nonlinearity.

		public float m_contactDamping;					// Damping added to resonators that become attached
													// to this surface.

		// Coupling parameters:
		public float m_contactAmpLimit;
		public float m_contactMasterGain;
		public float m_contactToOtherResGain;
		public float m_contactDirectGain;

		public float m_impactAmpLimit;
		public float m_impactMasterGain;
		public float m_impactToOtherResGain;
		public float m_impactDirectGain;


		public int setContactAmpLimit(float g) { m_contactAmpLimit = g; return 0; }

		public int setContactMasterGain(float g) { m_contactMasterGain = g; return 0; }
		public int setContactToOtherResGain(float g) { m_contactToOtherResGain = g; return 0; }
		public int setContactDirectGain(float g) { m_contactDirectGain = g; return 0; }
		
		public int setImpactAmpLimit(float g) { m_impactAmpLimit = g; return 0; }
		public int setImpactMasterGain(float g) { m_impactMasterGain = g; return 0; }
		public int setImpactToOtherResGain(float g) { m_impactToOtherResGain = g; return 0; }
		public int setImpactDirectGain(float g) { m_impactDirectGain = g; return 0; }

		// Hardness and contact damping are often related, but here they are
		// independently controllable.
		public int setHardness(float h) { m_hardness = h; return 0; }
		public int setImpulseToHardnessBreakpoint(float b) { m_impulseToHardnessBreakpoint = b; return 0; }
		public int setImpulseToHardnessScale(float s) { m_impulseToHardnessScale = s; return 0; }
		public int setContactDamping(float d) { m_contactDamping = d; return 0; }





	
	public Surface()
	{
		m_maxImpulse = -1.0f;	// No max limit.
		m_hardness = 1000.0f;	// Impulse width is about 1/1000 seconds.
		m_impulseToHardnessBreakpoint = 0.0f;
		m_impulseToHardnessScale = 0.0f;

		m_contactAmpLimit = (float)-1.0;
		m_contactMasterGain = (float)1.0;
		m_contactToOtherResGain = (float)1.0;
		m_contactDirectGain = (float)0.0;

		m_impactAmpLimit = (float)-1.0;
		m_impactMasterGain = (float)1.0;
		m_impactToOtherResGain = (float)1.0;
		m_impactDirectGain = (float)0.0;

		m_contactDamping = (float)1.0;
	}
}
