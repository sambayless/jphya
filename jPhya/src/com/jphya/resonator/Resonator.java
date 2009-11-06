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

import com.jphya.body.Body;
import com.jphya.scene.Scene;
import com.jphya.signal.Block;

public abstract class Resonator {

	private Block m_input;
	protected Block m_output; // Output block currently in use.

	protected int m_activityHandle; // Used when deleting this resonator from
									// the
	// resonator activity list in paScene.
	protected boolean makeQuiet; // Force resonator to be quiet. Processed in
									// paTick();

	protected float contactDamping; // Used to accumulate damping from
										// different contacts in paTick();
	// Use these in each derived class:
	protected float contactDampingOld; // The final value of contactDamping at
											// last ->tick().
	protected float maxContactDamping; // Limit combined contact damping.

	protected boolean inContact; // True if resonator has any continuous
									// contacts.
	// Used to prevent premature quieting.

	protected Object m_userData;
	protected Body body; // Points back to parent body.

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	protected final Scene scene;

	
	
	
	
	public void setInput(Block input) {
		setM_input(input);
	};

	public Block getInput() {
		return getM_input();
	};

	public void setOutput(Block output) {
		m_output = output;
	};

	public Block getOutput() {
		return m_output;
	};

	public void zeroOutput() {
		m_output.zero();
	}

	public abstract void zero();

	// Used to determine when a resonator should be switched off.
	public abstract void setQuietLevel(float l);

	public abstract float estimateVolume();
	
	public abstract boolean isQuiet();

	// MakeQuiet is used to force a isQuiet() on when next used.
	public int makeQuiet() {
		makeQuiet = true;
		return 0;
	}

	/**
	 *  Process input, put result in output.
	 * @return
	 */
	public abstract Block tick();

	/**
	 * Add the results of a tick to the block, but dont zero it first.
	 * @return
	 */
	public abstract Block tickAdd();

	public Block tick(Block input) {
		setM_input(input);
		return tick();
	}

	public Block tickAdd(Block input) {
		setM_input(input);
		return tickAdd();
	}

	public Block tick(Block input, Block output) {
		setM_input(input);
		m_output = output;
		return tick();
	}

	public Block tickAdd(Block input, Block output) {
		setM_input(input);
		m_output = output;
		return tickAdd();
	}

	public boolean isActive() {
		return scene.getActiveResonators().contains(this);
		//return activeResList.isObjectActive(this);//(m_activityHandle != -1);
	}

	public Object getUserData() {
		return m_userData;
	}

	public int setUserData(Object d) {
		m_userData = d;
		return 0;
	}

	public abstract int getTimeCost();

	public abstract void setAuxAmpScale(float s);

	public abstract void setAuxFreqScale(float s);

	public abstract void setAuxDampScale(float s);

	public int resetContactDamping() {
		contactDamping = 1.0f;
		return 0;
	}

	public int addContactDamping(float d) {
		contactDamping *= d;
		return 0;
	}

	public void setMaxContactDamping(float d) {
		maxContactDamping = d;
	}

	public boolean isInContact() {
		return inContact;
	}

	public void setInContact() {
		inContact = true;
	}

	public void clearInContact() {
		inContact = false;
	}

	public Resonator(Scene scene) {
		this.scene = scene;
		m_activityHandle = -1; // Not active initially.
		m_output = null;
		makeQuiet = false;
		maxContactDamping = 100.0f; // Initial limit chosen to prevent
	//	scene.addResontaor(this);
		// coefficient calculation errors.
	}

	public void activate() {
		zero();
		if(!scene.getActiveResonators().contains(this))
		{
			scene.addResontaor(this);
		}
	//	activeResList.addMember(this);
		// assert(( m_activityHandle != -1));//"Res activity list full."
	
	}

	public int deactivate() {
		scene.removeResontaor(this);
		/*activeResList.deleteMember(this);
		m_activityHandle = -1;*/
		return 0;
	}

	public void setM_input(Block m_input) {
		this.m_input = m_input;
	}

	public Block getM_input() {
		return m_input;
	}
	

	

}
