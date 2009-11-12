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

package com.jphya.scene;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.jphya.audio.AudioOutputStream;
import com.jphya.contact.Contact;
import com.jphya.contact.ContactGenerator;
import com.jphya.impact.Impact;
import com.jphya.impact.ImpactGenerator;
import com.jphya.resonator.Resonator;
import com.jphya.signal.Block;
import com.jphya.signal.Limiter;
import com.jphya.surface.Surface;


public class Scene extends AbstractScene {

	//private float framesPerSecond = 44100;// change later
	private int nFrames = Block.INIT_NBLOCKFRAMES;
	private int nMaxFrames=Block.INIT_NBLOCKFRAMES ;
	

	private Block _output = null;
	private Block _tempBlock = null;

	public static int _maxTimeCost = 100000;
	static int _timeCost = 0;
	
	private MultipleOutputCallback _multipleOutputCallback = null;

	private int maxResonators = 128;

	public float getMaxResonators() {
		return maxResonators;
	}

	public void setMaxResonators(int maxResonators) {
		this.maxResonators = maxResonators;
	}


	private ReentrantLock criticalSection = new ReentrantLock();

	public MonoCallback _monoCallback = null;
	public Limiter limiter = null;
	
	public Limiter getLimiter() {
		return limiter;
	}

	public void setLimiter(Limiter limiter) {
		this.limiter = limiter;
	}


	private final AudioOutputStream outputStream;

	public AudioOutputStream getOutputStream() {
		return outputStream;
	}

	public Scene(AudioOutputStream outputStream) {
		this(outputStream,(Block.INIT_NBLOCKFRAMES * outputStream.getSampleRate())/44100);
	}

	public Scene(AudioOutputStream outputStream, int blockSize) {
		this.outputStream = outputStream;
		this.nFrames = blockSize;
		this.nMaxFrames = blockSize;
		init();

	}
	public int getNMaxFrames() {
		return nMaxFrames;
	}

	public void setNMaxFrames(int maxFrames) {
		nMaxFrames = maxFrames;
	}

	public int getNFrames() {
		return nFrames;
	}

	public void setNFrames(int frames) {
		nFrames = frames;
	}

	/*public void _audioThread() {
		Block output;
		while (audioThreadIsOn) {
			// EnterCriticalSection(_critSec); // Don't use paLock() because the
			criticalSection.lock(); // locked flag is just for use// by main
			// thread.
			try {
				output = Tick();
			} finally {
				criticalSection.unlock();
			}
			// LeaveCriticalSection(_critSec);

			if (_limiter != null)
				_limiter.tick(output);

			// Allow the user to pass audio generated by Tick() to their own
			// output stream.
			if (_monoCallback != null)
				_monoCallback.call(output.getStartBytes());

			if (outputStreamIsOpen)
				outputStream
						.writeSamples(output.getStartBytes(), getNFrames());
		}
		// Thread terminates on exit.
		System.out.printf("thread stops\n");
		// return 0;
	}
*/
	// 

	/**
	 * Convenience method.
	 * Pass limiter time parameters. Zeros remove limiter.
	 */
	public void setLimiter(float attackTime, float holdTime, float releaseTime) {
		if(attackTime == 0 ||holdTime == 0 || releaseTime == 0)
			setLimiter(null);
		else
			setLimiter(new Limiter(attackTime, holdTime,releaseTime,this));
	}

	private int init() {
		
		return tickInit();
	}


	
	/**
	 * Set a function to be called after each tick, with the audio data.
	 * @param cb
	 * @return
	 */
	public int setOutputCallback(MonoCallback cb) {
		if (cb == null)
			return -1;

		_monoCallback = cb;
		return 0;
	}

	/**
	 * Generate the next sound sample
	 * @return
	 */
	public FloatBuffer generate() {
		Block output = tick();

		if (output != null) {
			 if (limiter!=null)
				limiter.tick(output);
			output.getStartBytes().rewind();
			output.getStartBytes().limit(getNFrames()*(Float.SIZE/Byte.SIZE));
			outputStream.writeSamples(output.getStartBytes(), getNFrames());
		}

		// Allow the user to pass audio generated by Tick() to their own output
		// stream.
		if (_monoCallback != null)
			_monoCallback.call(output.getStartBytes());// pass in byte format,
		// in case that is what
		// they need

		return output.getStart();
	}

	public boolean isLocked() {
		return criticalSection.isHeldByCurrentThread();
	}

	// // Double threaded operation

	public void lock() {

	
		criticalSection.lock();
	
	}

	public boolean tryLock() {

	
		try {
			return criticalSection.tryLock(1,TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {

		}
		return false;
	}
	
	public void unlock() {
		// assert(("Not locked.", isLocked() == true));
		criticalSection.unlock();

	}

	/** 
	* The time cost must be increased when tick starts using an object,
	* and decremented when tick stops using it.
	* ! It would be better if _timeCost were a static part of a class inherited
	* by costing objects,
	* ! so that the activate / deactivate methods encapsulate load management,
	* ! and probably block pull/push as well.
	*/
	int paSetMaxTimeCost(int c) {
		_maxTimeCost = c;
		return 0;
	}

	public int setMultipleOutputCallback(MultipleOutputCallback cb) {
		_multipleOutputCallback = cb;
		return 0;
	}

	/**
	 * Creation of blocks delayed until after user sets block size.
	 * @return
	 */
	private int tickInit() {
		if (_tempBlock == null)
			_tempBlock = Block.newBlock(this);
		else
			return -1;
		if (_output == null)
			_output = Block.newBlock(this);
		else
			return -1;
		return 0;
	}

	public boolean _tryActivateResIfInactive(Resonator res) {
		if (!res.isActive()) {
			int newCost;
			if ((newCost = _timeCost + res.getTimeCost()) <= _maxTimeCost) {
				_timeCost = newCost;
				res.setInput(Block.newBlock(this));
				// assert(("Block pool empty.", res.getInput()));
				res.getInput().zero(); // This resonator won't have been zeroed
				// at the start of paTick()
				res.resetContactDamping();
				res.activate();
				_tickResonators.add(res);
				return true;
			} else
				return false;
		} else
			return true;
	}

	private final ArrayList<Contact> _tickContacts = new ArrayList<Contact>();
	private final ArrayList<Impact> _tickImpacts = new ArrayList<Impact>();
	private final ArrayList<Resonator> _tickResonators = new ArrayList<Resonator>();
	public Block tick() {
		long curTime = System.nanoTime();
		float maxTime = (( 1000000000f * (((float) this.getNFrames())/this.getFPS())));
		long timePeriodConcacts=  (long) (maxTime*0.300f);
		long timePeriodImpacts=  (long) (maxTime*0.40f);
		long timePeriodResonators=  (long) (maxTime*0.85f);
		

	//	if(tryLock()){
		lock();
			try{
				/*
				 * Copy these to private lists for this method, so that the scene can be updated in a separate thread 
				 * without blocking for the sound to finish being processed.
				 */
				_tickContacts.clear();
				_tickContacts.addAll(super.getActiveContacts());
				
				_tickImpacts.clear();
				_tickImpacts.addAll(super.getActiveImpacts());
				
				_tickResonators.clear();
				_tickResonators.addAll(super.getActiveResonators());
			}finally{
				unlock();
			}
		
		
		//	updateScene();
		/*}finally{
			unlock();
		}*/
		// Overview:
		// Clear resonator inputs.
		// Tick contact generators where not quiet, add to resonator inputs.
		// (wake up affected resonators that were previously quiet.)
		// (Free up resources used by contacts that have been marked for
		// deletion.)
		// Tick resonators.

		// // Clear Res inputs.

		// Resonator res;
		// Resonator.activeResList.firstMember();
		// Iterator<Resonator> resList = Resonator.activeResList.getObjects();
		// while ((res = Resonator.activeResList.getNextMember()) != null) {
		for (Resonator res : _tickResonators) {
			res.getInput().zero();
			res.resetContactDamping();
			res.clearInContact();
		}

		// // Clear main output.
		// assert(("Main output not set. Try paInit ?", _output));
		_output.zero();
		
		//the initial sounds of impacts should be preserved.
		// // Tick contacts.

		// Contact c;
		
		// Contact.pool.firstActiveObject();
		// while ((c = Contact.pool.getNextActiveObject()) != null) {
		//Iterator<Contact> cit = getActiveContacts().iterator();
		// for(Impact im: Impact.pool.getObjects())
		//while (cit.hasNext())
		

		long maxTimeContacts = curTime + timePeriodConcacts;//dont let processing take more than 85% of actual play time
		int conNum = 0;
		for(Contact c: _tickContacts)
		{
			//how could c possibly be null?
			//Contact c = cit.next();
			if (c.isReady()) {
				// And generate surface excitations from valid surfaces.
	/*			if(conNum++ % 4 == 0)
				{
				 if(System.nanoTime()>maxTimeContacts)
				 {
					// System.out.println(timePeriodConcacts + " contact " + _tickContacts.size() + "\t" + _tickImpacts.size());
					 break;
				 }
				}*/
				Block surfaceOutput = _tempBlock;
				Resonator res1 = c.getResonator1();
				Resonator res2 = c.getResonator2();
				ContactGenerator gen1 = c.getContactGen1();
				ContactGenerator gen2 = c.getContactGen2();
				Surface surface1 = c.getSurface1();
				Surface surface2 = c.getSurface2();

				// Contact damping multiplicative 'accumulator'
				float contactDamping = (float) 1.0;

				if (res1 != null)
					res1.setInContact();
				if (res2 != null)
					res2.setInContact();

				if (gen1 != null) {
					// If a body is disabled it will stop generating contact
					// sound immediately.
					// (Valid contactGen implies a valid body)
					if (c.getBody1().isEnabled()) {
						contactDamping *= c.getSurface1().m_contactDamping;
						gen1
								.setSpeedBody1RelBody2(c.getDynamicData().speedBody1RelBody2);
						gen1
								.setSpeedContactRelBody(c.getDynamicData().speedContactRelBody1);
						gen1.setContactForce(c.getDynamicData().contactForce);

						// ( Otherwise we continue using previous data set. )

						// Test that surface generates non-zero output.
						// If it doesn't then we don't need to calculate it
						// explicitly.
						if (!gen1.isQuiet()) {
							gen1.setOutput(surfaceOutput);
							gen1.tick();
							if (c.isFadeAndDelete())
								surfaceOutput.fadeout();

							// Direct output.
							_output.addWithMultiply(surfaceOutput, c
									.getSurface1ContactDirectGain());

							if (res1 != null) {
								if (_tryActivateResIfInactive(res1)) // If res
									
									// active
									// , or
									// can
									// be
									// made
									// active
									// .
									// Direct addition onto resonator input:
									res1.getInput().add(surfaceOutput);
							}

							if (res2 != null) {
								if (_tryActivateResIfInactive(res2))
								{	// Cross coupling to the other resonator.
								
									res2.getInput().addWithMultiply(
											surfaceOutput,
											c.getSurface1ContactToRes2Gain());
								}
							}
						}
					}
				}

				if (gen2 != null) {
					// If a body is disabled it will stop generating contact
					// sound immediately.
					// (Valid contactGen implies a valid body)
					if (c.getBody2().isEnabled()) {
						contactDamping *= c.getSurface2().m_contactDamping;
						gen2
								.setSpeedBody1RelBody2(c.getDynamicData().speedBody1RelBody2);
						gen2
								.setSpeedContactRelBody(c.getDynamicData().speedContactRelBody2);
						gen2.setContactForce(c.getDynamicData().contactForce);

						if (!gen2.isQuiet()) {
							gen2.setOutput(surfaceOutput);
							gen2.tick();
							if (c.isFadeAndDelete())
								surfaceOutput.fadeout();

							// Direct output.
							_output.addWithMultiply(surfaceOutput, c
									.getSurface2ContactDirectGain());

							if (res2 != null) {
								if (_tryActivateResIfInactive(res2))
								{	
									res2.getInput().add(surfaceOutput);
								
								}
							}

							if (res1 != null) {
								if (_tryActivateResIfInactive(res1))
								{	
									res1.getInput().addWithMultiply(
											surfaceOutput,
											c.getSurface2ContactToRes1Gain());
									
								}
							}

						}
					}
				}

				if (res1 != null)
					res1.addContactDamping(contactDamping);
				if (res2 != null)
					res2.addContactDamping(contactDamping);

				if (c.isFadeAndDelete()) {
					//cit.remove();
					Contact.deleteContact(c);
				//	super.removeContact(c);
					// Contact.deleteContact(c);
				}

			}
		}

		// // Tick impacts.

		// Impact im = null;
		long maxTimeImpacts = curTime + timePeriodImpacts;//dont let processing take more than 85% of actual play time

		// Impact.pool.firstActiveObject();
		// while ((im = Impact.pool.getNextActiveObject()) != null) {
		//Iterator<Impact> it = getActiveImpacts().iterator();
		int imNum = 0;
		 for(Impact im: _tickImpacts)
		 {
		//while (it.hasNext()) { // If an impact is locked it cannot be ready -
			// you only write params
			// once.
			//Impact im = it.next();
			 if(imNum++ % 4 == 0)
			 {
				 //only check on every second one to reduce calls to System.nanoTime();
				 if(System.nanoTime()>maxTimeImpacts)
				 {
					// System.out.println(timePeriodImpacts  + " impact "  + _tickContacts.size() + "\t" + _tickImpacts.size());
					 break;
				 }
			 }
			if (im.isReady()) {// the is ready function is faked.
				// Pass impact data to the impact.
				// And generate surface excitations from valid surfaces.

				Block surfaceOutput = _tempBlock;
				Resonator res1 = im.getResonator1();
				Resonator res2 = im.getResonator2();
				ImpactGenerator gen1 = im.getImpactGen1();
				ImpactGenerator gen2 = im.getImpactGen2();
				Surface surface1 = im.getSurface1();
				Surface surface2 = im.getSurface2();

				boolean impactHasFinished = true; // Will be false if either
				// impact generator still
				// makes sound.
			
				if (surface1 != null && gen1 != null) {

					gen1
							.setRelTangentSpeedAtImpact(im.getDynamicData().relTangentSpeedAtImpact);
					gen1
							.setRelNormalSpeedAtImpact(im.getDynamicData().relNormalSpeedAtImpact);
					gen1.setImpactImpulse(im.getDynamicData().impactImpulse);

					gen1.setOutput(surfaceOutput);
					gen1.tick();

					// Direct output. (mostly for testing)
					_output.addWithMultiply(surfaceOutput, im
							.getSurface1ImpactDirectGain());

					if (res1 != null) {
						if (_tryActivateResIfInactive(res1))
						{	
							res1.getInput().add(surfaceOutput);
						
						}
					}

					if (res2 != null) {
						if (_tryActivateResIfInactive(res2))
						{	
							res2.getInput().addWithMultiply(surfaceOutput,
									(float) im.getSurface1ImpactToRes2Gain());
					
						}
					}

					if (!gen1.isQuiet())
						impactHasFinished = false;
					else { // Shutdown this generator.
						surface1.deleteImpactGen(im.getImpactGen1());
						im.setSurface1(null);
					}
				}

				if (surface2 != null && gen2 != null) {

					gen2
							.setRelTangentSpeedAtImpact(im.getDynamicData().relTangentSpeedAtImpact);
					gen2
							.setRelNormalSpeedAtImpact(im.getDynamicData().relNormalSpeedAtImpact);
					gen2.setImpactImpulse(im.getDynamicData().impactImpulse);

					gen2.setOutput(surfaceOutput);
					gen2.tick();

					// Direct output.
					_output.addWithMultiply(surfaceOutput, im.getSurface2ImpactDirectGain());

					if (res2 != null) {
						if (_tryActivateResIfInactive(res2))
						{
							res2.getInput().add(surfaceOutput);
							
						}
					}

					if (res1 != null) {
						if (_tryActivateResIfInactive(res1))
						{
							res1.getInput().addWithMultiply(surfaceOutput,	(float) im.getSurface2ImpactToRes1Gain());
						
						}
					}

					if (!gen2.isQuiet())
						impactHasFinished = false;
					else { // Shutdown this generator.
						surface2.deleteImpactGen(im.getImpactGen2());
						im.setSurface2(null);
					}

				}

				// Delete the impact when both impact generators are off.
				if (impactHasFinished) {
				//	it.remove();
					//super.removeImpact(im);
					Impact.deleteImpact(im);
					// Impact.pool.deleteActiveObject(im);
				}

			}
		}

		// // Tick resonators.

			// Resonator.activeResList.firstMember();
		Block resOutput = Block.newBlock(this); // Temp buffer
		assert (resOutput != null);

	//	Iterator<Resonator> rit = getActiveResonators().iterator();
		// for(Impact im: Impact.pool.getObjects())
		//while (rit.hasNext())
		int resNum = 0;
		long maxTimeResonators = curTime + timePeriodResonators;//dont let processing take more than 85% of actual play time

		 for(Resonator res: _tickResonators)
		{
			if(resNum++ %4 == 0)
			{
				 //only check on every second one to reduce calls to System.nanoTime();
				 if(System.nanoTime()>maxTimeResonators)
				 {
				//	 System.out.println(timePeriodResonators  + " " +   _tickContacts.size() + "\t" + _tickImpacts.size());
					 break;
				 }
			 }
		
			
		//	Resonator res = rit.next();
			// for(Resonator res:Resonator.activeResList.getObjects())
			// {
			// while ((res = Resonator.activeResList.getNextMember()) != null) {
			res.setOutput(resOutput);
			res.tick();
			
			if (res.isQuiet() && !res.isInContact()) {
				// ! Add a lower quiet level for contacts. This will prevent dsp
				// resource being used
				// ! when a contact is sitting without exciting resonators.
				// ! Need to then fix gen.isQuiet() to not be always false.
				resOutput.fadeout(); // Fade over one block to smooth out
				// stop-glitch. Important.
			//	rit.remove();
				//super.removeResontaor(res);
				//super.removeResontaor(res);
				 res.deactivate();
				_timeCost -= res.getTimeCost();
				// ! Potential bug if cost of res changes while res activated.
				// ! Would be better if load management was in res, with state
				// record.
				
				//Block.deleteBlock(res.getM_input()); // Free input block to the
				// pool.
			}

			if (_multipleOutputCallback != null)
				_multipleOutputCallback.call(res, resOutput.getStartBytes());
			else 
				_output.add(resOutput);
		}

		Block.deleteBlock(resOutput);

		return _output;
	}

	public int setMaxTimeCost(int c) {
		_maxTimeCost = c;
		return 0;
	}

	public float getFPS() {
		return this.outputStream.getSampleRate();
	}
	
	/**
	 * Convenience method
	 * @return
	 */
	public float getFPSFrames()
	{
		return this.getFPS()/((float)this.nFrames);
	}

	
	



}
