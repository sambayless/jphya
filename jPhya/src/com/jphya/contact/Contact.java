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

import java.util.Iterator;

import com.jphya.body.Body;
import com.jphya.resonator.Resonator;
import com.jphya.scene.Scene;
import com.jphya.surface.Surface;

public class Contact {

	// protected Block Tick(){return null;}; // Main audio tick function which

	//  updates the resonators and surface generators.
	 
	private Body body1;
	private Body body2;

	private Resonator resonator1;
	private Resonator resonator2;

	/**
	 * Used to provide a surface type for
	 *  freeing contactGens when the contact is freed.
	 */
	private Surface surface1;  
	private Surface surface2; 
	

	private ContactGenerator contactGen1;
	private ContactGenerator contactGen2;

	private ContactDynamicData dynamicData;

	 
	/**
	 * Contact couplings.
	* These are copied from the owning paBody, and can be altered.
	 */
	private float surface1ContactToRes2Gain;
	/**
	 * Contact couplings.
	* These are copied from the owning paBody, and can be altered.
	 */
	private float surface2ContactToRes1Gain;
	/**
	 * Contact couplings.
	* These are copied from the owning paBody, and can be altered.
	 */
	private float surface1ContactDirectGain;
	/**
	 * Contact couplings.
	* These are copied from the owning paBody, and can be altered.
	 */
	private float surface2ContactDirectGain;

	private boolean isReady; // Used to prevent the audiothread processing the
	// contact before any parameters have been set.
	// private int m_poolHandle; // Handle used by the scene contact manager to
	// release the contact.
	private boolean used; // Can be used to track when contacts are not used
	// and can be released.

	private float[] lastPosition = new float[3]; // Can be used to find
	// contact vel by numerical
	// diff.

	private Object userData;

	private final Scene scene;

	public boolean isActive() {
		return scene.getActiveContacts().contains(this);
	};

	// static int setAllUnused(); // Facility for deleting contacts that are not
	// referenced in contact callback.
	public int setUsed() {
		used = true;
		return 0;
	}; // Set in the contact callback.

	// static int deleteUnused(); // Run after dynamic step, to clear unused
	// contacts.

	/**
	 *  Signals that paTick() should fade out
	 *  excitations from this contact then
	 * delete.
	 */
	private boolean fadeAndDelete; 

	// 

	// 

	// int setBody1(Body);
	// int setBody2(Body);





	public Surface getSurface1() {
		return surface1;
	}

	public Surface getSurface2() {
		return surface2;
	}

	public ContactGenerator getGen1() {
		return getContactGen1();
	}

	public ContactGenerator getGen2() {
		return getContactGen2();
	}

	// Coupling set functions. Use straight after obtaining a new contact.

	public int setSurface1ContactMasterGain(float s) {
		assert (getContactGen1() != null);// (
		// "Body1 has not been assigned with 'setBody1()' "
		// , m_contactGen1) );
		getContactGen1().setGain(s);
		return 0;
	}

	public int setSurface2ContactMasterGain(float s) {
		assert (getContactGen2() != null);// (
		// "Body2 has not been assigned with 'setBody2()' "
		// , m_contactGen2) );
		getContactGen2().setGain(s);
		return 0;
	}

	public int setSurface1ContactPostMasterGainLimit(float s) {
		assert (getContactGen1() != null);// (
		// "Body1 has not been assigned with 'setBody1()' "
		// , m_contactGen1) );
		getContactGen1().setLimit(s);
		return 0;
	}

	public int setSurface2ContactPostMasterGainLimit(float s) {
		assert (getContactGen2() != null);// (
		// "Body2 has not been assigned with 'setBody2()' "
		// , m_contactGen2) );
		getContactGen2().setLimit(s);
		return 0;
	}

	public int setSurface1ContactToRes2Gain(float s) {
		surface1ContactToRes2Gain=s;
		return 0;
	}

	public int setSurface2ContactToRes1Gain(float m_surface2ContactToRes1Gain) {
		this.surface2ContactToRes1Gain = m_surface2ContactToRes1Gain;
		return 0;
	}

	public int setSurface1ContactDirectGain(float s) {
		surface1ContactDirectGain = s;
		return 0;
	}

	public int setSurface2ContactDirectGain(float s) {
		surface2ContactDirectGain=(s);
		return 0;
	}

	// Regular dynamic data update.

	// int setDynamicData(paContactDynamicData* d);
	// int setPhysCollisionData(paGeomCollisionData* d);

	public Object getUserData() {
		return userData;
	}

	int setUserData(Object d) {
		userData = d;
		return 0;
	}

	public float[] getLastPosition() {
		return lastPosition;
	}

	float setLastPostion(float[] p) {
		lastPosition[0] = p[0];
		lastPosition[1] = p[1];
		lastPosition[2] = p[2];
		return 0f;// ?
	}

	public int initialize() {
		setDynamicData(new ContactDynamicData());

		setResonator1(null);
		setResonator2(null);
		setContactGen1(null);
		setContactGen2(null);
		setSurface1(null);
		setSurface2(null);

		getDynamicData().speedContactRelBody1 = 0;
		getDynamicData().speedContactRelBody2 = 0;
		getDynamicData().speedBody1RelBody2 = 0;
		getDynamicData().contactForce = 0;

		this.used = false;
		setIsReady(false);
		setFadeAndDelete(false);

		return 0;
	}

	/**
	 * Use the static constructor for allocation management.
	 * 
	 * @param scene
	 */
	protected Contact(Scene scene) {
		// m_poolHandle = -1;
		this.scene = scene;
		initialize();
	}

	public int terminate() {
		if (getSurface1() != null)
			getSurface1().deleteContactGen(getContactGen1());
		if (getSurface2() != null)
			getSurface2().deleteContactGen(getContactGen2());
		return 0;
	}

	public static Contact newContact(Scene scene) {
		// paLOCKCHECK
		Contact c = scene.getAllocationManager().acquireObject(Contact.class);
		if (c == null) {
			c = new Contact(scene);
		}
		c.initialize();//important to do this here
		
		// scene.contactPool.newActiveObject();
		/*
		 * if (c == null) return c;
		 */
		// c.m_poolHandle = pool.getHandle();
		scene.addContact(c);
		// c.initialize();
		return c;
	}

	// static
	public static int deleteContact(Contact c) {
		// paLOCKCHECK // Can't use flag checking inside audio thread.

		// assert(("Contact not valid.", c));
		// if (c==0) return -1;
		//
		// assert(("Contact not in use.", c.m_poolHandle != -1));
		// if (c.m_poolHandle == -1) return -1; //error - contact not being
		// used.

		c.terminate();
		c.scene.removeContact(c);
		c.clear();
		c.scene.getAllocationManager().returnObject(c);
		// c.scene.contactPool.deleteActiveObject(c);
		// c.m_poolHandle = -1;

		return 0;
	}

	/**
	 *  Signals paTick() to fade out over one
	 block, then delete.
	 */
	public void fadeAndDelete() {
		setFadeAndDelete(true); //

	}

	// static
/*	public static void deleteRandomContact(Scene scene) {
		// paLOCKCHECK
		int pos = Rnd.random(0, scene.getActiveContacts().size() - 1);

		scene.getAllocationManager().returnObject(scene.removeContact(pos));
	}
	*/
	  public static void setAllUnused(Scene scene){
		  // Facility for deleting
		  scene.lock();
		  try{
			  for(Contact contact:scene.getActiveContacts())
				  contact.used = false;
		  }finally{
		  scene.unlock();
		  }
	  }

	  /**
	   * Delete all unused contacts, return them to the allocation manager.
	   */
	  public static void deleteUnused(Scene scene){
		  scene.lock();
		  try{
			  Iterator<Contact> cIt = scene.getActiveContacts().iterator();//this wont work!
			  while(cIt.hasNext())
			  {
				  Contact c= cIt.next();
				  if(!c.used)
				  {
					  cIt.remove();
					  scene.getAllocationManager().returnObject(cIt);
				  }
			  }
		  }finally{
			  scene.unlock();
			  }
	  }


	/**
	 * Coupling set functions. Use straight after assigning bodies.
	 * These couplings overide the coupling setting in the assigned bodies.
	 * @param m_body1
	 */
	public int setBody1(Body b) {
		// paLOCKCHECK

		if (b == null)
			return -1;
		// Following is optional depending on whether you
		// want to be able to switch on response to contacts
		// that have already started.
		// if (!b.isEnabled()) return -1;

		assert (getContactGen1() == null);
		if (getContactGen1() != null)
			return -1;

		this.body1 = b;
		setResonator1(b.getResonator());
		setSurface1(b.getSurface());

		if (getSurface1() == null)
			return -1;

		setContactGen1(getSurface1().newContactGen());

		assert (getContactGen1() != null);
		if (getContactGen1() == null)
			return -1; // No point having a contact generator

		// Activate direct-mix output streamFeed

		// Copy couplings from the body.

		setSurface1ContactPostMasterGainLimit(b.getContactAmpLimit());
		setSurface1ContactMasterGain(b.getContactMasterGain());
		setSurface1ContactToRes2Gain(b.getContactToOtherResGain());
		setSurface1ContactDirectGain(b.getContactDirectGain());

		return 0;
	}
	/**
	 * Coupling set functions. Use straight after assigning bodies.
	 * These couplings overide the coupling setting in the assigned bodies.
	 * @param m_body1
	 */
	public int setBody2(Body b) {
		// paLOCKCHECK

		if (b == null)
			return -1;
		// Following is optional depending on whether you
		// want to be able to switch on response to contacts
		// that have already started.
		// if (!b.isEnabled()) return -1;

		assert (getContactGen2() == null);
		if (getContactGen2() != null)
			return -1;

		this.body2 = b;
		setResonator2(b.getResonator());
		setSurface2(b.getSurface());

		if (getSurface2() == null)
			return -1;

		setContactGen2(getSurface2().newContactGen());

		assert (getContactGen2() != null);
		if (getContactGen2() == null)
			return -1; // No point having a contact.

		// Copy couplings from the body.

		setSurface2ContactPostMasterGainLimit(b.getContactAmpLimit());
		setSurface2ContactMasterGain(b.getContactMasterGain());
		setSurface2ContactToRes1Gain(b.getContactToOtherResGain());
		setSurface2ContactDirectGain(b.getContactDirectGain());

		return 0;
	}

	public int setDynamicData(ContactDynamicData d) {
		// paLOCKCHECK

		this.dynamicData = d;
		setIsReady(true);
		return 0;
	}

	

	public Body getBody2() {
		return body2;
	}

	public void setSurface2(Surface m_surface2) {
		this.surface2 = m_surface2;
	}





	public ContactDynamicData getDynamicData() {
		return dynamicData;
	}

	public void setFadeAndDelete(boolean m_fadeAndDelete) {
		this.fadeAndDelete = m_fadeAndDelete;
	}

	public boolean isFadeAndDelete() {
		return fadeAndDelete;
	}



	public float getSurface2ContactDirectGain() {
		return surface2ContactDirectGain;
	}



	public float getSurface2ContactToRes1Gain() {
		return surface2ContactToRes1Gain;
	}

	public void setIsReady(boolean m_isReady) {
		this.isReady = m_isReady;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setResonator1(Resonator m_resonator1) {
		this.resonator1 = m_resonator1;
	}

	public Resonator getResonator1() {
		return resonator1;
	}

	public void setResonator2(Resonator m_resonator2) {
		this.resonator2 = m_resonator2;
	}

	public Resonator getResonator2() {
		return resonator2;
	}

	public void setSurface1(Surface m_surface1) {
		this.surface1 = m_surface1;
	}

	

	public void setContactGen2(ContactGenerator m_contactGen2) {
		this.contactGen2 = m_contactGen2;
	}

	public ContactGenerator getContactGen2() {
		return contactGen2;
	}

	public void setContactGen1(ContactGenerator m_contactGen1) {
		this.contactGen1 = m_contactGen1;
	}

	public ContactGenerator getContactGen1() {
		return contactGen1;
	}



	public float getSurface1ContactDirectGain() {
		return surface1ContactDirectGain;
	}


	public float getSurface1ContactToRes2Gain() {
		return surface1ContactToRes2Gain;
	}



	public Body getBody1() {
		return body1;
	}
	
	/**
	 * Clear outgoing references (except to scene) for returning to the allocation manager.
	 */
	public void clear()
	{
		this.body1 = null;
		this.body2 = null;
		this.contactGen1 = null;
		this.contactGen2 = null;
		this.dynamicData = null;
		this.resonator1 = null;
		this.resonator2 = null;
	}
}
