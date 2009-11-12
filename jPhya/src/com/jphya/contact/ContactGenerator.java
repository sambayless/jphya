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

package com.jphya.contact;

import com.jphya.scene.Scene;
import com.jphya.signal.Block;

public abstract class ContactGenerator {
	//
	// paContactGen.hpp
	//
	// Abstract base class for generating sound from a continuous contact.
	// Referenced by active paContact objects.
	//

	protected Block m_output;

	protected float m_quietSpeed;
	protected float m_gain;
	protected float m_limit;

	protected float m_speedContactRelBody;
	protected float m_speedBody1RelBody2;
	protected float m_contactForce;

	protected final Scene scene;

	public Scene getScene() {
		return scene;
	}

	public ContactGenerator(Scene scene) {
		this.scene = scene;

		m_poolHandle = -1;
		m_output = null;
		m_speedContactRelBody = 0;
		m_speedBody1RelBody2 = 0;
		m_contactForce = 0;

		// scene.addContactGenerator(this);
	}

	public abstract Block tick();

	public int m_poolHandle; // Handle used by the scene contact manager to
								// release the object.

	public abstract boolean isQuiet(); // Should only return true is output of
										// gen is zero (non-zero constant is no
										// good)

	// The following could possibly be replaced with automatic param update
	// in gen->tick(), using a refernence to the owning paContact.

	public void setSpeedContactRelBody(float s) {
		m_speedContactRelBody = s;
	};

	public void setSpeedBody1RelBody2(float s) {
		m_speedBody1RelBody2 = s;
	};

	public void setContactForce(float f) {
		m_contactForce = f;
	}

	public void setGain(float g) {
		m_gain = g;
	}

	public void setLimit(float l) {
		m_limit = l;
	}

	public abstract void setOutput(Block output); // Virtual so that internal
													// settings can be made.

	public Block getOutput() {
		return m_output;
	};

}
