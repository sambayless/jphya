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
package com.jphya.test.lwjgl;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.input.MouseInput;
import com.jme.math.Vector3f;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.util.geom.BufferUtils;
import com.jmetest.physics.SimplePhysicsTest;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsNode;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.PhysicsUpdateCallback;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.contact.ContactCallback;
import com.jmex.physics.contact.PendingContact;
import com.jmex.physics.geometry.PhysicsSphere;
import com.jmex.physics.util.PhysicsPicker;
import com.jphya.body.Body;
import com.jphya.contact.Contact;
import com.jphya.contact.ContactDynamicData;
import com.jphya.impact.Impact;
import com.jphya.impact.ImpactDynamicData;
import com.jphya.lwjgl.LWGJLStream;
import com.jphya.resonator.ModalData;
import com.jphya.resonator.ModalResonator;
import com.jphya.scene.Scene;
import com.jphya.surface.FunctionSurface;
import com.jphya.surface.WhiteFunction;


public class TestLWJGLInteractive extends SimplePhysicsTest {

	public static final float MIN_IMPACT_TIME =200000000L;
	public static final float MAX_CONTACT_TIME =800000000L;
	private Map<PhysicsNode,Body> soundMap = new HashMap<PhysicsNode,Body>();
	private Map<ContactPair,ContactEvent> contactMap = new HashMap<ContactPair,ContactEvent>();

	 protected StaticPhysicsNode staticNode;
	
    protected void simpleInitGame() {
    	
        getPhysicsSpace().setAutoRestThreshold( 0.2f );
        setPhysicsSpeed( 4 );
        
        try {
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			return;
		}
        AL10.alListenerf(AL10.AL_GAIN, 1f);//master volume control
        AL10.alDistanceModel(AL10.AL_INVERSE_DISTANCE);
		IntBuffer sourcePointerBuf = BufferUtils.createIntBuffer(1);
		AL10.alGenSources(sourcePointerBuf);//should add error catching later.
		int sourcePointer = sourcePointerBuf.get(0);

		AL10.alSourcePlay(sourcePointer);
        
        LWGJLStream stream = new LWGJLStream(sourcePointer);
        stream.setSamplerate(8000);
        final Scene scene = new Scene(stream);
        
    
        
        final  ModalData glass = new ModalData();
    	try {
    		glass.read("res/glass.md");
		} catch (IOException e) {
			e.printStackTrace();
		}
		  final  ModalData floor = new ModalData();
	    	try {
	    		floor.read("res/panlid.md");
			} catch (IOException e) {
				e.printStackTrace();
			}
        
     
        staticNode = getPhysicsSpace().createStaticNode();
        TriMesh trimesh = new Box( "trimesh", new Vector3f(), 15, 0.5f, 15 );
        trimesh.setModelBound( new BoundingBox() );
        trimesh.updateModelBound();

        staticNode.attachChild( trimesh );
        staticNode.generatePhysicsGeometry( false );

        Body staticBody = new Body();
        
        ModalResonator staticResonator = new ModalResonator(scene);
        staticResonator.setData(floor);
        staticResonator.setQuietLevel(1.0f); 
        staticResonator.setAuxAmpScale(1f);
      
        staticResonator.setAuxFreqScale(1f );//we can adjust the frequency of the sound so that small objects are high pitched, and big objects are low pitched
        //some artistry is required here, because modan doesn't normalize the volumes or frequencies of its modal resonance files.
        staticBody.setResonator(staticResonator);	    
        FunctionSurface staticSurface = new FunctionSurface(scene);
        WhiteFunction whitefun = new WhiteFunction(); // White noise

		staticSurface.setFun(whitefun); 
	
		// The following define the velocity to surface-filter map.
		// This rises linearly to a maximum frequency value.
		staticSurface.setCutoffFreqAtRoll(10.0f); // Adjust the rel body vel to
												// filter cutoff mapping.
		staticSurface.setCutoffFreqRate(1000.0f); // Rate of change of cutoff
												// freq with slip speed.
		staticSurface.setCutoffFreq2AtRoll(10.0f); // Beef up rolling with
												// optional extra filter
												// layer.
		staticSurface.setCutoffFreq2Rate(1000.0f);

		staticSurface.setContactMasterGain(1.0f);
        staticBody.setSurface(staticSurface);
        
        soundMap.put(staticNode, staticBody);
        
        staticNode.getLocalTranslation().set( 0, -5, 0 );
        rootNode.attachChild( staticNode );

        final DynamicPhysicsNode dynamicNode1 = getPhysicsSpace().createDynamicNode();
        TriMesh mesh1 = new Sphere( "meshsphere", 16, 16, 2 );
        mesh1.setModelBound( new BoundingSphere() );
        mesh1.updateModelBound();
       
       
        mesh1.getLocalTranslation().set( -1, 0, 0 );
   
        dynamicNode1.attachChild( mesh1 );
        final PhysicsSphere physicsSphere1 = dynamicNode1.createSphere( "sphere physics" );
        physicsSphere1.getLocalTranslation().set(mesh1.getLocalTranslation());
        physicsSphere1.getLocalScale().set(2,2,2);
        dynamicNode1.attachChild( physicsSphere1 );
        rootNode.attachChild( dynamicNode1 );
        dynamicNode1.computeMass();
        
        Body sphereBody = new Body();

		ModalResonator sphereResonator = new ModalResonator(scene);
		sphereResonator.setData(glass);
		sphereResonator.setQuietLevel(1.0f); 
		sphereResonator.setAuxAmpScale(0.1f);
		sphereResonator.setAuxFreqScale(0.5f );//we can adjust the frequency of the sound so that small objects are high pitched, and big objects are low pitched
		sphereBody.setResonator(sphereResonator);
		   FunctionSurface sphereSurface = new FunctionSurface(scene); 
		   sphereSurface.setFun(new WhiteFunction()); 
			// The following define the velocity to surface-filter map.
			// This rises linearly to a maximum frequency value.
		   sphereSurface.setCutoffFreqAtRoll(10.0f); // Adjust the rel body vel to
													// filter cutoff mapping.
		   sphereSurface.setCutoffFreqRate(1000.0f); // Rate of change of cutoff
													// freq with slip speed.
		   sphereSurface.setCutoffFreq2AtRoll(10.0f); // Beef up rolling with
													// optional extra filter
													// layer.
		   sphereSurface.setCutoffFreq2Rate(1000.0f);
			// sphereSurface.setBoostAtRoll(10.0f); // Boosts volume towards
			// rolling, simulates increased transfer.
			// sphereSurface.setBoostBreakSlipSpeed(0.1f);
		   sphereSurface.setContactDirectGain(0.0f); // We can tune the settings
													// for each type of surface.
		   sphereSurface.setContactMasterGain(10.0f);
		sphereBody.setSurface(sphereSurface);
		
		soundMap.put(dynamicNode1, sphereBody);
        
		
		
		   final DynamicPhysicsNode dynamicNode2 = getPhysicsSpace().createDynamicNode();
	       Sphere sphere2 = new Sphere( "sphere2", 16, 16, 1 );
	       sphere2.setModelBound( new BoundingSphere() );
	       sphere2.updateModelBound();

	       sphere2.getLocalTranslation().set( 3, 4, 0 );
	   
	        dynamicNode1.attachChild( mesh1 );
	        final PhysicsSphere physicsSphere2 = dynamicNode2.createSphere( "physicsSphere2" );
	        physicsSphere2.getLocalTranslation().set(sphere2.getLocalTranslation());
	        dynamicNode2.attachChild( sphere2 );
	        rootNode.attachChild( dynamicNode2 );
	        dynamicNode2.computeMass();
	        
	        Body sphere2Body = new Body();

			ModalResonator sphere2Resonator = new ModalResonator(scene);
			sphere2Resonator.setData(glass);
			sphere2Resonator.setQuietLevel(1.0f); 
			sphere2Resonator.setAuxAmpScale(0.1f); //We can adjust the relative volume of this resonator
			sphere2Resonator.setAuxFreqScale(0.7f );//we can adjust the frequency of the sound so that small objects are high pitched, and big objects are low pitched
			sphere2Body.setResonator(sphere2Resonator);
			   FunctionSurface sphere2Surface = new FunctionSurface(scene); 
			   
				// The following define the velocity to surface-filter map.
				// This rises linearly to a maximum frequency value.
			   sphere2Surface.setCutoffFreqAtRoll(10.0f); // Adjust the rel body vel to
														// filter cutoff mapping.
			   sphere2Surface.setCutoffFreqRate(1000.0f); // Rate of change of cutoff
														// freq with slip speed.
			   sphere2Surface.setCutoffFreq2AtRoll(10.0f); // Beef up rolling with
														// optional extra filter
														// layer.
			   sphere2Surface.setCutoffFreq2Rate(1000.0f);
				// sphere2Surface.setBoostAtRoll(10.0f); // Boosts volume towards
				// rolling, simulates increased transfer.
				// sphere2Surface.setBoostBreakSlipSpeed(0.1f);
			   sphere2Surface.setContactDirectGain(0.0f); // We can tune the settings
														// for each type of surface.
			   sphere2Surface.setContactMasterGain(10.0f);
			   
			   sphere2Surface.setFun(new WhiteFunction()); 

			   sphere2Body.setSurface(sphere2Surface);
			
			soundMap.put(dynamicNode2, sphere2Body);
		
		

        cameraInputHandler.setEnabled( false );
        new PhysicsPicker( input, rootNode, getPhysicsSpace() );
        MouseInput.get().setCursorVisible( true );

        
        
        /*
         * This is an extremely simple implementation of impact and collision detection.
         * A more sophisticated implementation will provide better estimates of the various
         * dynamics parameters.
         */
        getPhysicsSpace().getContactCallbacks().add(new ContactCallback(){

			public boolean adjustContact(PendingContact contactInfo) {
				Vector3f velocity = new Vector3f();
				Vector3f contactNormal = new Vector3f();
				Vector3f contactPosition = new Vector3f();
				contactInfo.getContactVelocity(velocity);
				contactInfo.getContactNormal(contactNormal);
				
				contactInfo.getContactPosition(contactPosition);
				float normalVelocity =  contactNormal.dot(velocity);
				//float tangentialSpeed =  (float) Math.sqrt(velocity.lengthSquared() - normalVelocity*normalVelocity);
				float tangentialSpeed = 0;
				Vector3f vel1 = new Vector3f();
				Vector3f vel2 = new Vector3f();
				
				if(!contactInfo.getNode1().isStatic())
				{
					vel1 = ( (DynamicPhysicsNode)contactInfo.getNode1()).getLinearVelocity(vel1);
				}
				if(!contactInfo.getNode2().isStatic())
				{
					vel2 = ( (DynamicPhysicsNode)contactInfo.getNode2()).getLinearVelocity(vel2);
				}
				
				Vector3f relative = vel1.subtract(vel2);
				float relNorm = contactNormal.dot(relative);
				tangentialSpeed =(float) Math.sqrt(relative.lengthSquared() - relNorm*relNorm);
				
				
			
				{
					Body body1 = soundMap.get(contactInfo.getNode1());
					Body body2 = soundMap.get(contactInfo.getNode2());
					
					
					scene.lock();
					try{
					
						boolean isContact = false;
					
						
						long impactTime = System.nanoTime();
						ContactPair contactPair = new ContactPair(body1,body2);
						if(contactMap.containsKey(contactPair))
						{
							ContactEvent lastContact = contactMap.get(contactPair);							
							
							if(impactTime-lastContact.impactTime < MIN_IMPACT_TIME)
								isContact = true;							
					
						}
						
						if(!isContact)
						{
						
											
							ImpactDynamicData data = new ImpactDynamicData();		
							
							data.relNormalSpeedAtImpact =normalVelocity;		
							data.relTangentSpeedAtImpact = tangentialSpeed;	
							data.impactImpulse =data.relNormalSpeedAtImpact*data.relNormalSpeedAtImpact; //this is incorrect, but good enough for our purposes
						
							Impact impact =Impact.newImpact(scene);
							impact.setDynamicData(data);
							
						
							impact.setBody1(body1);
							
							impact.setBody2(body2);
					
				
						}
						
						if(!contactMap.containsKey(contactPair)){
						//}else if(contactPair.getContact() == null){
							Contact c = Contact.newContact(scene);
							
							ContactDynamicData contactData = new ContactDynamicData();
							contactData.speedBody1RelBody2=normalVelocity;
							contactData.contactForce = tangentialSpeed;
							contactData.speedContactRelBody1 =tangentialSpeed;
							contactData.speedContactRelBody2 =tangentialSpeed;
							c.setDynamicData(contactData);
							
							c.setBody1(body1);
						
							c.setBody2(body2);
							ContactEvent newContactEvent = new ContactEvent();
							newContactEvent.contact = c;
							newContactEvent.impactTime = impactTime;
							contactMap.put(contactPair, newContactEvent);
						}else{
							
							ContactEvent lastContact = contactMap.get(contactPair);
							Contact c = lastContact.contact;
							lastContact.impactTime = impactTime;
							ContactDynamicData contactData = c.getDynamicData();
							contactData.speedBody1RelBody2=normalVelocity;
							contactData.contactForce = tangentialSpeed;
							contactData.speedContactRelBody1 =tangentialSpeed;
							contactData.speedContactRelBody2 = tangentialSpeed;
							
							c.setDynamicData(contactData);
							
							c.setBody1(body1);
						
							c.setBody2(body2);
					
						}
					}finally{
						scene.unlock();
					}
				}
				
			
				
				return false;
			}
        	
        });
        
        
        getPhysicsSpace().addToUpdateCallbacks(new PhysicsUpdateCallback(){

			public void afterStep(PhysicsSpace arg0, float arg1) {
				//Discard contacts that have died
				long time = System.nanoTime();
				Iterator<Entry<ContactPair, ContactEvent>> entries = contactMap.entrySet().iterator();
				while(entries.hasNext())
				{
					Entry<ContactPair, ContactEvent> entry = entries.next();
					if(time-entry.getValue().impactTime > MAX_CONTACT_TIME)
					{
						scene.lock();
						try{
						
							Contact.deleteContact( entry.getValue().contact);
							entries.remove();
						
							//scene.removeContact(entry.getValue().contact);
							
						}finally{
							scene.unlock();
						}
						
				
						
						
						
					}
				}
			}

			public void beforeStep(PhysicsSpace arg0, float arg1) {
				
			}
        	
        });
     
        /*
         * It is a good idea to put the sound generation in a separate thread from physics/rendering.
         * The LWJGLStream spends a relatively large amount of time sleeping, waiting for buffers
         * to empty. This is not a big draw on cpu resources, but it is slow, and may noticeably slow
         * down physics or rendering. 
         */
        Thread soundThread = new Thread(new Runnable(){

			public void run() {
				while(true){
					scene.generate();
				}
			}
        	
        },"Sound Thread");
        soundThread.start();
        
    }

    @Override
    protected void simpleUpdate() {
        cameraInputHandler.setEnabled( MouseInput.get().isButtonDown( 1 ) );
    }

    public static void main( String[] args ) {
        Logger.getLogger( "" ).setLevel( Level.WARNING ); // to see the important stuff
        // create a new thread to benefit from -Xss setting to increase stack size
        new Thread() {
            @Override
            public void run() {
                new TestLWJGLInteractive().start();
            }
        }.start();
    }


    private static class ContactPair{
    	 ContactPair(Body body1, Body body2) {
			super();
			this.body1 = body1;
			this.body2 = body2;
		}
    
		 Body body1;
		 Body body2;
		@Override
		public int hashCode() {
		
			int result = body1.hashCode() + body2.hashCode();
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ContactPair other = (ContactPair) obj;
			if(body1 == other.body1 && body2 == other.body2)
				return true;
			if(body1 == other.body2 && body2 == other.body1)
				return true;
			return false;
		}
    	
    }
    
    public static class ContactEvent
    {
    	 long impactTime = 0l;
    	 Contact contact = null;

    }
}



