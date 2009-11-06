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

package com.jphya.scene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.jphya.contact.Contact;
import com.jphya.contact.ContactGenerator;
import com.jphya.impact.Impact;
import com.jphya.impact.ImpactGenerator;
import com.jphya.resonator.Resonator;
import com.jphya.signal.Block;
import com.jphya.utility.AllocationManager;


/**
 * This abstract scene manages scene resources.
 * @author Sam
 *
 */
public abstract class AbstractScene {
	
	protected final Collection<Contact> contacts = new HashSet<Contact>();
	protected final Collection<Resonator> resonators=new HashSet<Resonator>();
	protected final Collection<Impact> impacts=new HashSet<Impact>();
	protected final Collection<Block> blocks=new HashSet<Block>();
	protected final Collection<ContactGenerator> contactGenerators=new HashSet<ContactGenerator>();
	protected final Collection<ImpactGenerator> impactGenerators=new HashSet<ImpactGenerator>();

	private final Collection<Contact> immutableContacts = Collections.unmodifiableCollection(contacts);
	private final Collection<Resonator> immutableResonators = Collections.unmodifiableCollection(resonators);
	private final Collection<Impact> immutableImpacts = Collections.unmodifiableCollection(impacts);
	private final Collection<Block> immutableBlocks = Collections.unmodifiableCollection(blocks);
	private final Collection<ContactGenerator> immutableContactGenerators = Collections.unmodifiableCollection(contactGenerators);
	private final Collection<ImpactGenerator> immutableImpactGenerators = Collections.unmodifiableCollection(impactGenerators);
	
	private final AllocationManager allocationManager = new AllocationManager();
	
	/**
	 * Return a shared allocation manager that all elements of the scene may, optionally, use to reduce on instance creation/deletion.
	 * @return
	 */
	public AllocationManager getAllocationManager()
	{
		return allocationManager;
	}
	
	/**
	 * An immutable view of the scene's active contacts.
	 * Note: The user must manually lock the scene before iterating through this collection.
	 * @return
	 */
	public Collection<Contact> getActiveContacts() {
		return immutableContacts;
	}
	
	/**
	 * An immutable view of the scene's active resonators.
	 * Note: The user must manually lock the scene before iterating through this collection.
	 * @return
	 */
	public Collection<Resonator> getActiveResonators() {
		return immutableResonators;
	}
	
	/**
	 * An immutable view of the scene's active impacts.
	 * Note: The user must manually lock the scene before iterating through this collection.
	 * @return
	 */
	public Collection<Impact> getActiveImpacts() {
		return immutableImpacts;
	}
	
	/**
	 * An immutable view of the scene's active sound blocks.
	 * Note: The user must manually lock the scene before iterating through this collection.
	 * @return
	 */
	public Collection<Block> getActiveBlocks() {
		return immutableBlocks;
	}
	
	/**
	 * An immutable view of the scene's active contact generators.
	 * Note: The user must manually lock the scene before iterating through this collection.
	 * @return
	 */
	public Collection<ContactGenerator> getActiveContactGenerators() {
		return immutableContactGenerators;
	}
	
	/**
	 * An immutable view of the scene's active impact generators.
	 * Note: The user must manually lock the scene before iterating through this collection.
	 * @return
	 */
	public Collection<ImpactGenerator> getActiveImpactGenerators() {
		return immutableImpactGenerators;
	}
	
/*	protected void updateScene()
	{	
		  lock();
		  try{
			contacts_thread.clear();
			contacts_thread.addAll(contacts);
			
			impacts_thread.clear();
			impacts_thread.addAll(impacts);
			
			resonators_thread.clear();
			resonators_thread.addAll(resonators);
		  }finally{
			  unlock();
		  }
	
	}*/
	
	/**
	 * This thread safe method adds a contact to the scene.
	 */
	public void addContact(Contact c)
	{
		lock();
		try{
		this.contacts.add(c);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method adds a resonator to the scene.
	 */
	public void addResontaor(Resonator r)
	{
		lock();
		try{
		this.resonators.add(r);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method adds an impact to the scene.
	 */
	public void addImpact(Impact c)
	{
		
		lock();
		try{
		this.impacts.add(c);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method adds a block to the scene.
	 */
	public void addBlock(Block c)
	{
		lock();
		try{
		this.blocks.add(c);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method adds a contact generator to the scene.
	 */
	public void addContactGenerator(ContactGenerator c)
	{
		lock();
		try{
		this.contactGenerators.add(c);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method adds an impact generator to the scene.
	 */
	public void addImpactGenerator(ImpactGenerator c)
	{
		lock();
		try{
		this.impactGenerators.add(c);
		}finally{
			unlock();
		}
	}

	/**
	 * This thread safe method removes a contact from the scene.
	 */
	public void removeContact(Contact c)//could make this constant time if each item knew its position
	{
		lock();
		try{
			this.contacts.remove(c);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method removes a resonator from the scene.
	 */
	public void removeResontaor(Resonator r)
	{
		lock();
		try{
		this.resonators.remove(r);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method removes an impact from the scene.
	 */
	public void removeImpact(Impact c)
	{
		lock();
		try{
		this.impacts.remove(c);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method removes a sound block from the scene.
	 */
	public void removeBlock(Block c)
	{
		lock();
		try{
		this.blocks.remove(c);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method removes a contact generator from the scene.
	 */
	public void removeContactGenerator(ContactGenerator c)
	{
		lock();
		try{
		this.contactGenerators.remove(c);
		}finally{
			unlock();
		}
	}
	
	/**
	 * This thread safe method removes an impact generator from the scene.
	 */
	public void removeImpactGenerator(ImpactGenerator c)
	{
		lock();
		try{
		this.impactGenerators.remove(c);
		}finally{
			unlock();
		}
	}
	
	/**
	 * The user must manually lock the scene before iterating over sound objects.
	 * Add/Remove methods automatically lock the scene.
	 * This method will block until the lock becomes available.
	 */
	public abstract void lock();
	public abstract void unlock();
	
	/**
	 * The user must manually lock the scene before iterating over sound objects.
	 * Add/Remove methods automatically lock the scene.
	 * Returns true if the lock could be acquired without waiting; returns false if the lock
	 * could not be acquired.
	 */
	public abstract boolean tryLock();
	
}
