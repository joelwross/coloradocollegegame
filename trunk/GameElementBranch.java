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
	private Appearance appear; //an appearance node reference
	private BranchGroup broot; //the root of the branch.
	
	//constructor
	public GameElementBranch(GameElement e)
	{
		broot = new BranchGroup(); //the root of this branch
		broot.setCapability(BranchGroup.ALLOW_DETACH); //let us remove the branch at runtime

		Transform3D posi = new Transform3D(new Quat4f(e.getFacing()),new Vector3f(e.getPosition()),1); //make the new coordinate system
		coord = new TransformGroup(posi); //create the coordinate node
		coord.setCapability(TransformGroup.ALLOW_TRANSFORM_READ); //allow us to read the transformation at runtime
		coord.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); //allow us to change the transformation at runtime
		broot.addChild(coord); //add the transformation to the root.
		
		appear = createAppearance(e);
		
		BranchGroup sroot = new BranchGroup(); //a root for the shapes. In case we want to do other stuff to them (if it's redundant then J3D will get rid of it anyway)
		coord.addChild(sroot); //add sroot to the tree
		
		for(VirtualShape s : e.shapes) //run through the shapes!
		{
			Transform3D local = new Transform3D(new Quat4f(s.getFacing()), new Vector3f(s.getPosition()),1); //the local coordinates for the shape
			TransformGroup localg = new TransformGroup(local);
			
			Node p; //the shape to add--Node so we can have all kinds of geometry
			
			//set flags based on the element
			int sflags = Primitive.GENERATE_NORMALS; //always want these for shading
			if(e.attribute("texture") != null) //if we have a texture
				sflags = sflags | Primitive.GENERATE_TEXTURE_COORDS; //generate texture coords
			
			//now see what kind of shapes we're using. 
			//instanceof allows us to deal with extensions of the virtual primitives,
			//  but do we want that? If not, then change to be a getClassName (or whatever)
			if(s instanceof VirtualBox)
			{
				//make the primitive - convert full dimensions to half-dimensions
				p = new Box(.5f*((VirtualBox)s).getDimX(), .5f*((VirtualBox)s).getDimY(), .5f*((VirtualBox)s).getDimZ(), sflags, appear);
			}
			else if(s instanceof VirtualSphere)
			{
				//make the primitive
				p = new Sphere(((VirtualSphere)s).getRadius(), sflags, appear);
			}
			else if(s instanceof VirtualCylinder)
			{
				//make the primitive
				p = new Cylinder(((VirtualCylinder)s).getRadius(), ((VirtualCylinder)s).getHeight(), sflags, appear);
			}
			else if(s instanceof VirtualCone)
			{
				//make the primitive
				p = new Cone(((VirtualCone)s).getRadius(), ((VirtualCone)s).getHeight(), sflags, appear);
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
						      //note that this draws where it should be, not where it is
		
		broot.compile(); //let J3D optimize the branch
	}//constructor

	//defines the appearance node based on the given GameElement
	public Appearance createAppearance(GameElement e)
	{
		Appearance a = new Appearance(); //will need to specify how to get this appearance from the Element
		a.setCapability(Appearance.ALLOW_MATERIAL_READ);
		a.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
		Material mat = new Material();
		mat.setCapability(Material.ALLOW_COMPONENT_WRITE);
		a.setMaterial(mat);

		//constant stuff
		mat.setSpecularColor(1.0f,1.0f,1.0f);
		mat.setShininess(64.0f); //I swear to god: "shininess - the material's shininess in the range [1.0, 128.0] with 1.0 being not shiny and 128.0 being very shiny."
		
		/**ATTRIBUTE VARIABLES**/

		//color
		if(e.attribute("color") != null) //if a color is specified
		{
			int c = (Integer)e.attribute("color");
			mat.setDiffuseColor(((c>>16)&0xff)/255f, ((c>>8)&0xff)/255f, (c&0xff)/255f); //, ((c>>24)&0xff)/255f);

			//System.out.println("alpha="+((c>>24)&0xff));
		}
		else //make blue by default
			mat.setDiffuseColor(0.0f,0.0f,1.0f);
		
		//texture
		if(e.attribute("texture") != null)
		{
			try
			{
				TextureLoader loader = new TextureLoader((String)e.attribute("texture"), new Canvas()); //make a new Canvas for an image observer (or do we want to pass something else?)
				a.setTexture(loader.getTexture());
					//ImageComponent2D texImage = loader.getImage(); 
					//Texture2D texture = new Texture2D(Texture.BASE_LEVEL,Texture.RGBA,texImage.getWidth(),texImage.getHeight());
					//texture.setImage(0,texImage); 
					//a.setTexture(texture);
					
				//TextureAttributes texAttrib = new TextureAttributes();
				//texAttrib.setTextureMode(TextureAttributes.DECAL); //blend in with specified color--do we want this?
				//a.setTextureAttributes(texAttrib);
			}
			catch(Exception ex)
			{	
				//need better error reporting here
				System.out.println("Failed to load texture image: "+e.attribute("texture"));
			}
		}

		//transparency
		if(e.attribute("transparency") != null)
		{
			a.setTransparencyAttributes(new TransparencyAttributes(
							TransparencyAttributes.NICEST,
							(Float)e.attribute("transparency")));
		}

		return a;	
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

	public void setTranslation(float[] p)
	{
		Transform3D t = new Transform3D(); //a new Transform
		coord.getTransform(t); //fill the transform with our current settings
		t.setTranslation(new Vector3f(p)); //set the new translation
		coord.setTransform(t); //set as our new state
	}
		
	public void setRotation(float[] f)
	{
		Transform3D t = new Transform3D(); //a new Transform
		coord.getTransform(t); //fill the transform with our current settings
		t.setRotation(new Quat4f(f)); //set our current rotation
		coord.setTransform(t);
	}
	
	public void setTransform(float[] p, float[] f)
	{
		Transform3D t = new Transform3D(new Quat4f(f), new Vector3f(p), 1);
		coord.setTransform(t);
	}

	public Appearance getAppearance()
	{
		return appear;
	}

	public void setAppearance(Appearance a)
	{
		appear = a;
	}

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