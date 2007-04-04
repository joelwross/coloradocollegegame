//GameElementBranch.java
//@author Joel Ross

import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import com.sun.j3d.utils.image.*;

/***
 A class that represents a GameElement as a branch of the Java3D tree.
 Contains references to stuff in the tree we may want to change so we can
 sync with the World.
***/

public class GameElementBranch implements ElementBranch //it doesn't like if we extend BranchGroup, so just make that a member variable and fetch it later
{
	//member variables - anything we'd want to change later (Element level)
	private TransformGroup coord; //transformed coordinates for this branch
	//private Appearance appear; //an appearance node reference--doesn't mean anything at the moment
	private BranchGroup broot; //the root of the branch.
	
	//constructor
	public GameElementBranch(GameElement e)
	{
		broot = new BranchGroup(); //the root of this branch
		broot.setCapability(BranchGroup.ALLOW_DETACH); //let us remove the branch at runtime

		Transform3D posi = new Transform3D(new Quat4f(e.getFacing()),new Vector3f(e.getPosition()),1); //make the new coordinate system
		posi.setScale(new Vector3d(new Vector3f(e.getScale())));
		coord = new TransformGroup(posi); //create the coordinate node
		coord.setCapability(TransformGroup.ALLOW_TRANSFORM_READ); //allow us to read the transformation at runtime
		coord.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); //allow us to change the transformation at runtime
		broot.addChild(coord); //add the transformation to the root.
		
		/**Set default appearance variables**/
		//color
		float[] defaultColor; //{r,g,b,a}
		if(e.attribute("color") != null) //if a color is specified
		{
			int c = (Integer)e.attribute("color");
			defaultColor = new float[] {	((c>>16)&0xff)/255f, 
							((c>>8)&0xff)/255f, 
							(c&0xff)/255f,
							((c>>24)&0xff)/255f};
		}
		else
			defaultColor = new float[] {0f,0f,1f,1f}; //blue and opaque for default
		
		//texture
		String defaultTexture = (String)e.attribute("texture");
		
		//transparency
		TransparencyAttributes elementTransparency;
		if(e.attribute("transparency") != null)
		{
			elementTransparency = new TransparencyAttributes(
							TransparencyAttributes.NICEST,
							(Float)e.attribute("transparency"));
		}
		else
			elementTransparency = null;


		BranchGroup sroot = new BranchGroup(); //a root for the shapes. In case we want to do other stuff to them (if it's redundant then J3D will get rid of it anyway)
		coord.addChild(sroot); //add sroot to the tree
		
		for(VirtualShape s : e.shapes) //run through the shapes!
		{
			Transform3D local = new Transform3D(new Quat4f(s.getFacing()), new Vector3f(s.getPosition()),1); //the local coordinates for the shape
			local.setScale(new Vector3d(new Vector3f(s.getScale()))); //the scale of the shape
			TransformGroup localg = new TransformGroup(local);
			
			Node p; //the shape to add--Node so we can have all kinds of geometry
			
			//create new Appearance object based on defaults (above) and Shape s
			Appearance shapeAppearance = createAppearance(s, defaultColor, defaultTexture, elementTransparency); 

			//set flags based on the element
			int sflags = Primitive.GENERATE_NORMALS; //always want these for shading
			if(shapeAppearance.getTexture() != null) //if we have a texture
				sflags = sflags | Primitive.GENERATE_TEXTURE_COORDS; //generate texture coords
			
			//now see what kind of shapes we're using. 
			//instanceof allows us to deal with extensions of the virtual primitives,
			//  but do we want that? If not, then change to be a getClassName (or whatever)
			if(s instanceof VirtualBox)
			{
				//make the primitive - convert full dimensions to half-dimensions
				p = new Box(.5f*((VirtualBox)s).getDimX(), .5f*((VirtualBox)s).getDimY(), .5f*((VirtualBox)s).getDimZ(), sflags, shapeAppearance);
			}
			else if(s instanceof VirtualSphere)
			{
				//make the primitive
				p = new Sphere(((VirtualSphere)s).getRadius(), sflags, shapeAppearance);
			}
			else if(s instanceof VirtualCylinder)
			{
				//make the primitive
				p = new Cylinder(((VirtualCylinder)s).getRadius(), ((VirtualCylinder)s).getHeight(), sflags, shapeAppearance);
			}
			else if(s instanceof VirtualCone)
			{
				//make the primitive
				p = new Cone(((VirtualCone)s).getRadius(), ((VirtualCone)s).getHeight(), sflags, shapeAppearance);
			}
			else
			{
				System.out.println("unrecognizable shape"); //print an error message (for testing--should really be logging this)
				//p = new ColorCube(.1f); //make a default "shape" to show
				p = createSpinningBehavior(new ColorCube(.1f)); //make an obnoxious default "shape" to show
			}

			localg.addChild(p); //add the primitive to the TransformGroup
			sroot.addChild(localg); //add the TransformGroup to the shape root node			
		}
		
		sroot.addChild(createBoundingBox(e)); //draw the bounding box for testing.
						      		
		broot.compile(); //let J3D optimize the branch
	}//constructor

	//defines the appearance node based on the given GameElement
	public Appearance createAppearance(VirtualShape s, float[] defaultColor, String defaultTexture, TransparencyAttributes elementTransparency)
	{
		int A = 3; //for readability with color decomposition
		int R = 0;
		int G = 1;
		int B = 2;
		
		Appearance a = new Appearance();
		Material mat = new Material(); //use defaults
		a.setMaterial(mat);
		a.setTransparencyAttributes(elementTransparency);

		//color
		int c = s.getColor();
		float[] shapeColor = new float[] {((c>>16)&0xff)/255f, ((c>>8)&0xff)/255f, (c&0xff)/255f, ((c>>24)&0xff)/255f};
		//float temp = (1-shapeColor[A])*(defaultColor[A]);
		float temp = (1-shapeColor[A]); //Currently treating Element-color as having alpha of 1.0f
		mat.setDiffuseColor(
			(shapeColor[A]*shapeColor[R]) + (temp*defaultColor[R]),
			(shapeColor[A]*shapeColor[G]) + (temp*defaultColor[G]),
			(shapeColor[A]*shapeColor[B]) + (temp*defaultColor[B])); //alpha blending!
	
		//texture
		if(defaultTexture != null) //first assign an element texture (if possible)
		{
			try
			{
				TextureLoader loader = new TextureLoader(defaultTexture, new Canvas()); //make a new Canvas for an image observer (or do we want to pass something else?)
				a.setTexture(loader.getTexture());
			}
			catch(Exception ex)
			{	
				//need better error reporting here
				System.out.println("Failed to load texture image: "+defaultTexture);
			}
		}
		if(s.getTexture() != null) //then overwrite with an attribute texture (if possible)
		{
			try
			{
				TextureLoader loader = new TextureLoader(s.getTexture(), new Canvas()); //make a new Canvas for an image observer (or do we want to pass something else?)
				a.setTexture(loader.getTexture());
			}
			catch(Exception ex)
			{	
				//need better error reporting here
				System.out.println("Failed to load texture image: "+s.getTexture());
			}
		}		

//		TextureAttributes texAttrib = new TextureAttributes();
//		texAttrib.setTextureMode(TextureAttributes.REPLACE); //blend in with specified color--do we want this?
//		a.setTextureAttributes(texAttrib);

		return a; //return the Appearance
	}

	//returns the root of this branch (so we can add it to a J3D tree)
	public BranchGroup getBranchScene()
	{
		return broot;
	}
	
	//an accessor for detaching (and thereby deleting) the branch
	public void detach()
	{
		broot.detach();
	}

	//sets the translation transform of this element to vector p
	public void setTranslation(float[] p)
	{
		Transform3D t = new Transform3D(); //a new Transform
		coord.getTransform(t); //fill the transform with our current settings
		t.setTranslation(new Vector3f(p)); //set the new translation
		coord.setTransform(t); //set as our new state
	}
		
	//sets the rotation transform of this element to Quaterion f
	public void setRotation(float[] f)
	{
		Transform3D t = new Transform3D(); //a new Transform
		coord.getTransform(t); //fill the transform with our current settings
		t.setRotation(new Quat4f(f)); //set our current rotation
		coord.setTransform(t);
	}
	
	//sets the scale transform of this element to Quaterion f
	public void setScale(float[] s)
	{
		Transform3D t = new Transform3D(); //a new Transform
		coord.getTransform(t); //fill the transform with our current settings
		t.setScale(new Vector3d((double)s[0], (double)s[1], (double)s[2])); //set our current scale
		coord.setTransform(t);
	}
	

	//sets the transform of this element to translation vector p and rotation Quaternion f	
	//--holding onto this for now. I'm not sure if I'm going to want to keep it or not (probably not)
/*	public void setTransform(float[] p, float[] f)
	{
		Transform3D t = new Transform3D(new Quat4f(f), new Vector3f(p), 1);
		//t.setScale(new Vector3d(1.0d, 0.5d, 1.0d));
		coord.setTransform(t);
	}
*/
	//sets the transform of this element to translation vector p and rotation Quaternion f	
	public void setTransform(float[] p, float[] f, float[] s)
	{
		Transform3D t = new Transform3D(new Quat4f(f), new Vector3f(p), 1);
		t.setScale(new Vector3d((double)s[0], (double)s[1], (double)s[2])); //set our current scale
		coord.setTransform(t);
	}

/*
	//returns the Appearance node of this element
	public Appearance getAppearance()
	{
		return appear;
	}

	//sets the Appearance node of this element
	public void setAppearance(Appearance a)
	{
		appear = a;
	}
*/
	//maybe also draw based on shapes?
	public Shape3D createBoundingBox(GameElement e)
	{
		float[] obb = e.getBoundingBox();

		//create the corners out of the obb half-dimensions
		Point3f c0 = new Point3f( obb[0], obb[1], obb[2]);
		Point3f c1 = new Point3f( obb[0], obb[1],-obb[2]);
		Point3f c2 = new Point3f(-obb[0], obb[1],-obb[2]);
		Point3f c3 = new Point3f(-obb[0], obb[1], obb[2]);
		Point3f c4 = new Point3f( obb[0],-obb[1], obb[2]);
		Point3f c5 = new Point3f( obb[0],-obb[1],-obb[2]);
		Point3f c6 = new Point3f(-obb[0],-obb[1],-obb[2]);
		Point3f c7 = new Point3f(-obb[0],-obb[1], obb[2]);

		LineArray box = new LineArray(24, LineArray.COORDINATES | LineArray.COLOR_3);
		//each pair is an edge of the box
		box.setCoordinate(0, c0);	box.setCoordinate(1, c1);
		box.setCoordinate(2, c1);	box.setCoordinate(3, c2);
		box.setCoordinate(4, c2);	box.setCoordinate(5, c3);
		box.setCoordinate(6, c3);	box.setCoordinate(7, c0);
		box.setCoordinate(8, c4);	box.setCoordinate(9, c5);
		box.setCoordinate(10, c5);	box.setCoordinate(11, c6);
		box.setCoordinate(12, c6);	box.setCoordinate(13, c7);
		box.setCoordinate(14, c7);	box.setCoordinate(15, c4);
		box.setCoordinate(16, c0);	box.setCoordinate(17, c4);
		box.setCoordinate(18, c1);	box.setCoordinate(19, c5);
		box.setCoordinate(20, c2);	box.setCoordinate(21, c6);
		box.setCoordinate(22, c3);	box.setCoordinate(23, c7);

		Color3f magenta = new Color3f(1.0f, 0.0f, 1.0f);
		for(int i=0; i<24; i++)
			box.setColor(i, magenta);	
		
		return new Shape3D(box);
	}

	//This is just for fun.
	//@param a J3D Node (could be anything) to attach to the end of the transform.
	public TransformGroup createSpinningBehavior(Node arg)
	{
		TransformGroup spinx = new TransformGroup(); //a node for a coordinate system transformation (also the root)
		spinx.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); //allow us to change the transformation at runtime
		TransformGroup spiny = new TransformGroup(); //and again
		spiny.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); //ditto
		spinx.addChild(spiny); //add one transform to the other	

		Transform3D yAxis = new Transform3D(); //a transformation-defined coordinate system. We don't touch it initially
		Transform3D xAxis = new Transform3D(); //another change in the coordinate system
		xAxis.rotZ(Math.PI/4.0d); //make xAxis actually change the y-axis (the default) into the x-axis by rotating around the z-axis
		Alpha rotationAlpha = new Alpha(-1, 6000); //create an Alpha (timer)
		Alpha rotationAlpha2 = new Alpha(-1, 9000); //create a slower Alpha (timer)
		//create rotation behaviors. Everytime Alpha fires, they rotate the given coordinate system around the give axis
		RotationInterpolator rotator = new RotationInterpolator(rotationAlpha, spinx, yAxis, 0.0f, (float)Math.PI*2.0f);
		RotationInterpolator rotator2 = new RotationInterpolator(rotationAlpha, spiny, xAxis, 0.0f, (float)Math.PI*2.0f);
		
		//yay bounding spheres!
		BoundingSphere bounds = new BoundingSphere(); //creating a bounding sphere (for clipping)
		rotator.setSchedulingBounds(bounds); //only perform the behavior for the sphere
		rotator2.setSchedulingBounds(bounds); //ditto
		spinx.addChild(rotator); //add the behavior to the tree
		spiny.addChild(rotator2); //ditto
		
		spiny.addChild(arg); //add our argument to this branch
		return spinx; //return the "root" of this branch
	}	
	
	//member functions
		//gets/sets??
			//If we make public, then Representation takes care of moving stuff. If we make private, then calls methods that move stuff.
			//I think I like doing it privately--make take an extra step, but hides implementation (so can have only a single TransformGroup, for example)
}