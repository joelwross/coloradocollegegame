//ViewElementBranch.java
//@author Joel Ross

import com.sun.j3d.utils.universe.*;
//import com.sun.j3d.utils.geometry.*;//needed?
import javax.media.j3d.*;
import javax.vecmath.*;

/***
 A type of ElementBranch that defines a J3D branch for a view--a camera.
 This class can define a Client's GameElement which is either a first-person
 camera, a third-person element with a following camera, or a third-person
 element with a static camera (or hopefully any kind of camera we want).
***/

public class ViewElementBranch implements ElementBranch
{
	public static final int STATIC_VIEW = 0;
	public static final int FIRST_PERSON_VIEW = 1;
	public static final int OFFSET_VIEW = 2;
	public static final int INDEPENDENT_VIEW = 3;
	public static final int FOLLOWING_VIEW = 4;

	public static final float[] OFFSET = new float[] {0.0f, 2.0f, 5.0f};

	//member variables
	private ViewingPlatform camera;
	private TransformGroup coord; //transformed coordinates for this branch
	private GameElementBranch avatar;
	private BranchGroup avatarRoot; //where the avatar branch is attached
	
	private int viewMode; //what kind of view we're using

	//constructor
	public ViewElementBranch(GameElement e, BranchGroup scene, int v)
	{
		viewMode = v;
		camera = new ViewingPlatform();
		coord = camera.getMultiTransformGroup().getTransformGroup(0); //coord is the VP's transformation
		coord.setCapability(TransformGroup.ALLOW_TRANSFORM_READ); //allow us to read the transformation at runtime
		coord.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); //allow us to change the transformation at runtime

		Transform3D posi;
		if(viewMode == FIRST_PERSON_VIEW)
		{
			posi = new Transform3D(new Quat4f(e.getFacing()), new Vector3f(e.getPosition()), 1); //set us to the element's transform
		}
		else if(viewMode == OFFSET_VIEW)
		{
			Vector3f p = new Vector3f(e.getPosition());
			p.add(new Vector3f(OFFSET));
			posi = new Transform3D(new Quat4f(e.getFacing()), p, 1);
		}
		else if(viewMode == INDEPENDENT_VIEW)
		{
			//start us where?
			posi = new Transform3D(new Quat4f(Constants.DEFAULT_FACING), new Vector3f(0f,0f,5f), 1);
		}
		else
		{
			posi = new Transform3D(new Quat4f(Constants.DEFAULT_FACING), new Vector3f(0f,0f,5f), 1);
		}
		coord.setTransform(posi); //set our transform group to the default position

		//construct the avatar
		avatar = new GameElementBranch(e);
		avatarRoot = scene;
		if(viewMode != FIRST_PERSON_VIEW)
			avatarRoot.addChild(avatar.getBranchScene()); //add the avatar to the scene graph.
	}

	//cycles through the available view models.
	public void changeView()
	{
		System.out.println("Changing to mode #"+((viewMode%3)+1));
		changeView((viewMode%3)+1); //cycle between 1,2,3 only
	}

	//change to specified viewMode
	public void changeView(int to)
	{
		//add "from" checking here?

		if(to == FIRST_PERSON_VIEW)
		{
			viewMode = FIRST_PERSON_VIEW; //set the new mode
			if(avatar.getBranchScene().isLive()) //if is attached
				detach(); //remove the avatar

			Transform3D t = new Transform3D(); //a new Transform
			coord.getTransform(t); //fill the transform with our current settings
			t.setTranslation(avatar.getTranslation()); //set to our avatar's translation
			coord.setTransform(t); //set as our new state
		}
		else if(to == OFFSET_VIEW)
		{
			viewMode = OFFSET_VIEW; //set the new mode
			if(!avatar.getBranchScene().isLive()) //if is NOT attached
				attachAvatar();	//add the avatar

			Transform3D t = new Transform3D(); //a new Transform
			coord.getTransform(t); //fill the transform with our current settings
			Vector3f pp = avatar.getTranslation(); //get the avatar's current translation
			float[] f = new float[4];
			avatar.getRotation().get(f); //get a float[] of the avatar's rotation
			pp.add(new Vector3f(Quaternions.rotatePoint(OFFSET,f))); //add the spun offset to the translation
			t.setTranslation(pp); //set the new translation
			coord.setTransform(t); //set as our new state
		}
		else if(to == INDEPENDENT_VIEW)
		{
			viewMode = INDEPENDENT_VIEW;
			if(!avatar.getBranchScene().isLive()) //if is NOT attached
			{
				attachAvatar();	//add the avatar			
	
				Transform3D t = new Transform3D(); //a new Transform
				coord.getTransform(t); //fill the transform with our current settings

				Vector3f trans = new Vector3f();
				t.get(trans);
				trans.add(new Vector3f(0,0,5f)); //offset the old translation some (so camera isn't inside the element)
				
				t.setTranslation(trans); //reset the translation
				coord.setTransform(t); //set as our new state
			}
			//otherwise do nothing?
		}
		//add support for other views
	}

	//a method to fetch the ViewingPlatform in order to construct the view branch
	public ViewingPlatform getViewingPlatform()
	{
		return camera;
	}

	public int getViewMode()
	{
		return viewMode;
	}
	
	public void attachAvatar()
	{
		avatarRoot.addChild(avatar.getBranchScene()); //add the avatar to the scene graph.
	}
	
	public GameElementBranch getAvatar()
	{
		return avatar;	
	}

	//moves the element (avatar AND camera)
	public void setTransform(float[] p, float[] f, float[] s)
	{
		Transform3D avt = new Transform3D(new Quat4f(f), new Vector3f(p), 1);
		avt.setScale(new Vector3d((double)s[0], (double)s[1], (double)s[2])); //set our current scale
		
		Transform3D t = null;

		if(viewMode == FIRST_PERSON_VIEW)
		{
			t = new Transform3D(avt);
			//Transform3D t = new Transform3D(new Quat4f(f), new Vector3f(p), 1);
			//t.setScale(new Vector3d((double)s[0], (double)s[1], (double)s[2])); //set our current scale
			//coord.setTransform(t);
		}
		else if(viewMode == OFFSET_VIEW)
		{
			Vector3f pp = new Vector3f(VectorUtils.add(p,Quaternions.rotatePoint(OFFSET,f))); //could this line be our bottleneck?!
			t = new Transform3D(new Quat4f(f), pp, 1);
			t.setScale(new Vector3d((double)s[0], (double)s[1], (double)s[2])); //set our current scale
			//coord.setTransform(t);
		}
		else if(viewMode == INDEPENDENT_VIEW)
		{
			//do nothing. Camera is moved independently from the avatar
		}
		
		//now the least amount of time POSSIBLE between camera and avatar moves
		if(t != null)
			coord.setTransform(t);
		avatar.setTransform(avt);
		
		//avatar.setTransform(p,f,s); //always move the avatar Element
	}

	//moves the camera by the specified amounts
	public void transformCamera(float[] d, float[] q)
	{
		if(viewMode == INDEPENDENT_VIEW)
		{
			Transform3D t = new Transform3D(); //a new Transform
			coord.getTransform(t); //fill the transform with our current settings
			
			if(d != null)
			{
				Vector3f trans = new Vector3f();
				t.get(trans);
				trans.add(new Vector3f(d)); //move our translation by amount specified
				t.setTranslation(trans);
			}
			if(q != null)
			{
				Quat4f rot = new Quat4f();
				t.get(rot);
				rot.mul(new Quat4f(q)); //spin our rotation by amount specified
				t.setRotation(rot);
			}

			coord.setTransform(t);		
		}			
	}
	
	//sets the camera to the specified coordinates
	public void setCameraTransform(float[] d, float[] q)
	{
		if(viewMode == INDEPENDENT_VIEW)
		{
			Transform3D t = new Transform3D(new Quat4f(q), new Vector3f(d), 1);
			coord.setTransform(t);
		}
	}

	public Vector3f getTranslation()
	{
		Transform3D t = new Transform3D(); //a new Transform
		coord.getTransform(t); //fill the transform with our current settings
		Vector3f v = new Vector3f();
		t.get(v);
		return v;	
	}
	
	public Quat4f getRotation()
	{
		Transform3D t = new Transform3D(); //a new Transform
		coord.getTransform(t); //fill the transform with our current settings
		Quat4f q = new Quat4f();
		t.get(q);
		return q;	
	}

	/**
	 **Interface methods that get passed along to the avatar
	 **/

	public BranchGroup getBranchScene()
	{
		return avatar.getBranchScene();
	}
	
	public void detach()
	{
		avatar.detach();
	}

}